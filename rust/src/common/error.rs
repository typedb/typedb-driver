/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

use std::{error::Error as StdError, fmt};

use tonic::{Code, Status};
use typeql_lang::error_messages;

use super::{address::Address, RequestID};

error_messages! { ConnectionError
    code: "CXN", type: "Connection Error",
    RPCMethodUnavailable(String) =
        1: "The server does not support this method, please check the client-server compatibility:\n'{}'.",
    ConnectionIsClosed() =
        2: "The connection has been closed and no further operation is allowed.",
    SessionIsClosed() =
        3: "The session is closed and no further operation is allowed.",
    TransactionIsClosed() =
        4: "The transaction is closed and no further operation is allowed.",
    TransactionIsClosedWithErrors(String) =
        5: "The transaction is closed because of the error(s):\n{}",
    UnableToConnect() =
        6: "Unable to connect to TypeDB server.",
    DatabaseDoesNotExist(String) =
        9: "The database '{}' does not exist.",
    MissingResponseField(&'static str) =
        10: "Missing field in message received from server: '{}'.",
    UnknownRequestId(RequestID) =
        11: "Received a response with unknown request id '{}'",
    InvalidResponseField(&'static str) =
        12: "Invalid field in message received from server: '{}'.",
    ClusterUnableToConnect(String) =
        13: "Unable to connect to TypeDB Cluster. Attempted connecting to the cluster members, but none are available: '{}'.",
    ClusterReplicaNotPrimary() =
        14: "The replica is not the primary replica.",
    ClusterAllNodesFailed(String) =
        15: "Attempted connecting to all cluster members, but the following errors occurred: \n{}.",
    ClusterTokenCredentialInvalid() =
        17: "Invalid token credential.",
    SessionCloseFailed() =
        18: "Failed to close session. It may still be open on the server: or it may already have been closed previously.",
    ClusterEndpointEncrypted() =
        19: "Unable to connect to TypeDB Cluster: attempting an unencrypted connection to an encrypted endpoint.",
    ClusterSSLCertificateNotValidated() =
        20: "SSL handshake with TypeDB Cluster failed: the server's identity could not be verified. Possible CA mismatch.",
    BrokenPipe() =
        21: "Stream closed because of a broken pipe. This could happen if you are attempting to connect to an unencrypted cluster instance using a TLS-enabled credential.",
    ConnectionRefused() =
        22: "Connection refused. This could happen because of a misconfigured server SSL certificate, or network failures.",
}

error_messages! { InternalError
    code: "INT", type: "Internal Error",
    RecvError() =
        1: "Channel is closed.",
    SendError() =
        2: "Channel is closed.",
    UnexpectedRequestType(String) =
        3: "Unexpected request type for remote procedure call: {}.",
    UnexpectedResponseType(String) =
        4: "Unexpected response type for remote procedure call: {}.",
    UnknownConnectionAddress(Address) =
        5: "Received unrecognized address from the server: {}.",
    EnumOutOfBounds(i32, &'static str) =
        6: "Value '{}' is out of bounds for enum '{}'.",
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub enum Error {
    Connection(ConnectionError),
    Internal(InternalError),
    TypeQL(typeql_lang::common::Error),
    Other(String),
}

impl Error {
    pub fn code(&self) -> String {
        match self {
            Self::Connection(error) => error.format_code(),
            Self::Internal(error) => error.format_code(),
            Self::TypeQL(_error) => String::new(),
            Self::Other(_error) => String::new(),
        }
    }

    pub fn message(&self) -> String {
        match self {
            Self::Connection(error) => error.message(),
            Self::Internal(error) => error.message(),
            Self::TypeQL(error) => error.to_string(),
            Self::Other(error) => error.clone(),
        }
    }

    fn from_message(message: &str) -> Self {
        match message.split_ascii_whitespace().next() {
            Some("[RPL01]") => Self::Connection(ConnectionError::ClusterReplicaNotPrimary()),
            Some("[CLS08]") => Self::Connection(ConnectionError::ClusterTokenCredentialInvalid()),
            Some("[DBS06]") => Self::Connection(ConnectionError::DatabaseDoesNotExist(
                message.split('\'').nth(1).unwrap_or("{unknown}").to_owned(),
            )),
            _ => Self::Other(message.to_owned()),
        }
    }

    fn parse_unavailable(status_message: &str) -> Error {
        if status_message == "broken pipe" {
            Error::Connection(ConnectionError::BrokenPipe())
        } else if status_message.contains("received corrupt message") {
            Error::Connection(ConnectionError::ClusterEndpointEncrypted())
        } else if status_message.contains("UnknownIssuer") {
            Error::Connection(ConnectionError::ClusterSSLCertificateNotValidated())
        } else if status_message.contains("Connection refused") {
            Error::Connection(ConnectionError::ConnectionRefused())
        } else {
            Error::Connection(ConnectionError::UnableToConnect())
        }
    }
}

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Connection(error) => write!(f, "{error}"),
            Self::Internal(error) => write!(f, "{error}"),
            Self::TypeQL(error) => write!(f, "{error}"),
            Self::Other(message) => write!(f, "{message}"),
        }
    }
}

impl StdError for Error {
    fn source(&self) -> Option<&(dyn StdError + 'static)> {
        match self {
            Self::Connection(error) => Some(error),
            Self::Internal(error) => Some(error),
            Self::TypeQL(error) => Some(error),
            Self::Other(_) => None,
        }
    }
}

impl From<ConnectionError> for Error {
    fn from(error: ConnectionError) -> Self {
        Self::Connection(error)
    }
}

impl From<InternalError> for Error {
    fn from(error: InternalError) -> Self {
        Self::Internal(error)
    }
}

impl From<typeql_lang::common::Error> for Error {
    fn from(err: typeql_lang::common::Error) -> Self {
        Self::TypeQL(err)
    }
}

impl From<Status> for Error {
    fn from(status: Status) -> Self {
        if status.code() == Code::Unavailable {
            Self::parse_unavailable(status.message())
        } else if status.code() == Code::Unknown || is_rst_stream(&status) {
            Self::Connection(ConnectionError::UnableToConnect())
        } else if status.code() == Code::Unimplemented {
            Self::Connection(ConnectionError::RPCMethodUnavailable(status.message().to_owned()))
        } else {
            Self::from_message(status.message())
        }
    }
}

fn is_rst_stream(status: &Status) -> bool {
    // "Received Rst Stream" occurs if the server is in the process of shutting down.
    status.message().contains("Received Rst Stream")
}

impl From<http::uri::InvalidUri> for Error {
    fn from(err: http::uri::InvalidUri) -> Self {
        Self::Other(err.to_string())
    }
}

impl From<tonic::transport::Error> for Error {
    fn from(err: tonic::transport::Error) -> Self {
        Self::Other(err.to_string())
    }
}

impl<T> From<tokio::sync::mpsc::error::SendError<T>> for Error {
    fn from(err: tokio::sync::mpsc::error::SendError<T>) -> Self {
        Self::Other(err.to_string())
    }
}

impl From<tokio::sync::oneshot::error::RecvError> for Error {
    fn from(_err: tokio::sync::oneshot::error::RecvError) -> Self {
        Self::Internal(InternalError::RecvError())
    }
}

impl From<crossbeam::channel::RecvError> for Error {
    fn from(_err: crossbeam::channel::RecvError) -> Self {
        Self::Internal(InternalError::RecvError())
    }
}

impl<T> From<crossbeam::channel::SendError<T>> for Error {
    fn from(_err: crossbeam::channel::SendError<T>) -> Self {
        Self::Internal(InternalError::SendError())
    }
}

impl From<String> for Error {
    fn from(err: String) -> Self {
        Self::Other(err)
    }
}

impl From<std::io::Error> for Error {
    fn from(err: std::io::Error) -> Self {
        Self::Other(err.to_string())
    }
}
