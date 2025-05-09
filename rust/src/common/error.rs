/*
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

use std::{collections::HashSet, error::Error as StdError, fmt};

use itertools::Itertools;
use tonic::{Code, Status};
use tonic_types::{ErrorDetails, ErrorInfo, StatusExt};

use super::{address::Address, RequestID};

macro_rules! error_messages {
    {
        $name:ident code: $code_pfx:literal, type: $message_pfx:literal,
        $($error_name:ident $({ $($field:ident : $inner:ty),+ $(,)? })? = $code:literal: $body:literal),+ $(,)?
    } => {
        #[derive(Clone, Eq, PartialEq)]
        pub enum $name {$(
            $error_name$( { $($field: $inner),+ })?,
        )*}

        impl $name {
            pub const PREFIX: &'static str = $code_pfx;

            pub const fn code(&self) -> usize {
                match self {$(
                    Self::$error_name $({ $($field: _),+ })? => $code,
                )*}
            }

            pub fn format_code(&self) -> String {
                format!(concat!("[", $code_pfx, "{}{}]"), self.padding(), self.code())
            }

            pub fn message(&self) -> String {
                match self {$(
                    Self::$error_name $({$($field),+})? => format!($body $($(, $field = $field)+)?),
                )*}
            }

            const fn max_code() -> usize {
                let mut max = usize::MIN;
                $(max = if $code > max { $code } else { max };)*
                max
            }

            const fn num_digits(x: usize) -> usize {
                if (x < 10) { 1 } else { 1 + Self::num_digits(x/10) }
            }

            const fn padding(&self) -> &'static str {
                match Self::num_digits(Self::max_code()) - Self::num_digits(self.code()) {
                    0 => "",
                    1 => "0",
                    2 => "00",
                    3 => "000",
                    _ => unreachable!(),
                }
            }

            const fn name(&self) -> &'static str {
                match self {$(
                    Self::$error_name $({ $($field: _),+ })? => concat!(stringify!($name), "::", stringify!($error_name)),
                )*}
            }
        }

        impl std::fmt::Display for $name {
            fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
                write!(
                    f,
                    concat!("[", $code_pfx, "{}{}] ", $message_pfx, ": {}"),
                    self.padding(),
                    self.code(),
                    self.message()
                )
            }
        }

        impl std::fmt::Debug for $name {
            fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
                let mut debug_struct = f.debug_struct(self.name());
                debug_struct.field("message", &format!("{}", self));
                $(
                    $(
                        if let Self::$error_name { $($field),+ } = &self {
                            $(debug_struct.field(stringify!($field), &$field);)+
                        }
                    )?
                )*
                debug_struct.finish()
            }
        }

        impl std::error::Error for $name {
            fn source(&self) -> Option<&(dyn std::error::Error + 'static)> {
                None
            }
        }
    };
}

error_messages! { ConnectionError
    code: "CXN", type: "Connection Error",
    RPCMethodUnavailable { message: String } =
        1: "The server does not support this method, please check the driver-server compatibility:\n'{message}'.",
    ServerConnectionFailed { addresses: Vec<Address> } =
        2: "Unable to connect to TypeDB server(s) at: \n{addresses:?}",
    ServerConnectionFailedWithError { error: String } =
        3: "Unable to connect to TypeDB server(s), received errors: \n{error}",
    ServerConnectionFailedStatusError { error: String } =
        4: "Unable to connect to TypeDB server(s), received network or protocol error: \n{error}",
    ServerConnectionIsClosed =
        5: "The connection has been closed and no further operation is allowed.",
    TransactionIsClosed =
        6: "The transaction is closed and no further operation is allowed.",
    TransactionIsClosedWithErrors { errors: String } =
        7: "The transaction is closed because of the error(s):\n{errors}",
    DatabaseNotFound { name: String } =
        8: "Database '{name}' not found.",
    MissingResponseField { field: &'static str } =
        9: "Missing field in message received from server: '{field}'. This is either a version compatibility issue or a bug.",
    UnknownRequestId { request_id: RequestID } =
        10: "Received a response with unknown request id '{request_id}'",
    UnexpectedResponse { response: String } =
        11: "Received unexpected response from server: '{response}'. This is either a version compatibility issue or a bug.",
    InvalidResponseField { name: &'static str } =
        12: "Invalid field in message received from server: '{name}'. This is either a version compatibility issue or a bug.",
    QueryStreamNoResponse =
        13: "Didn't receive any server responses for the query.",
    UnexpectedQueryType { query_type: i32 } =
        14: "Unexpected query type in message received from server: {query_type}. This is either a version compatibility issue or a bug.",
    ClusterReplicaNotPrimary =
        15: "The replica is not the primary replica.",
    ClusterAllNodesFailed { errors: String } =
        16: "Attempted connecting to all TypeDB Cluster servers, but the following errors occurred: \n{errors}.",
    TokenCredentialInvalid =
        17: "Invalid token credentials.",
    EncryptionSettingsMismatch =
        18: "Unable to connect to TypeDB: possible encryption settings mismatch.",
    SSLCertificateNotValidated =
        19: "SSL handshake with TypeDB failed: the server's identity could not be verified. Possible CA mismatch.",
    BrokenPipe =
        20: "Stream closed because of a broken pipe. This could happen if you are attempting to connect to an unencrypted TypeDB server using a TLS-enabled credentials.",
    ConnectionFailed =
        21: "Connection failed. Please check the server is running and the address is accessible. Encrypted TypeDB endpoints may also have misconfigured SSL certificates.",
    MissingPort { address: String } =
        22: "Invalid URL '{address}': missing port.",
    AddressTranslationMismatch { unknown: HashSet<Address>, unmapped: HashSet<Address> } =
        23: "Address translation map does not match the server's advertised address list. User-provided servers not in the advertised list: {unknown:?}. Advertised servers not mapped by user: {unmapped:?}.",
    ValueTimeZoneNameNotRecognised { time_zone: String } =
        24: "Time zone provided by the server has name '{time_zone}', which is not an officially recognized timezone.",
    ValueTimeZoneOffsetNotRecognised { offset: i32 } =
        25: "Time zone provided by the server has numerical offset '{offset}', which is not recognised as a valid value for offset in seconds.",
    ValueStructNotImplemented =
        26: "Struct valued responses are not yet supported by the driver.",
    ListsNotImplemented =
        27: "Lists are not yet supported by the driver.",
    UnexpectedKind { kind: i32 } =
        28: "Unexpected kind in message received from server: {kind}. This is either a version compatibility issue or a bug.",
    UnexpectedConnectionClose =
        29: "Connection closed unexpectedly.",
}

error_messages! { ConceptError
    code: "CPT", type: "Concept Error",
    UnavailableRowVariable { variable: String } =
        1: "Cannot get concept from a concept row by variable '{variable}'.",
    UnavailableRowIndex { index: usize } =
        2: "Cannot get concept from a concept row by index '{index}'.",
    CannotDecodeImportedConcept =
        3: "Cannot decode a concept from the provided import file. Make sure to pass a correct database file produced by a TypeDB export operation.",
    CannotEncodeExportedConcept =
        4: "Cannot encode a concept for export. It's either a version compatibility error or a bug."
}

error_messages! { InternalError
    code: "INT", type: "Internal Error",
    RecvError =
        1: "Channel is closed.",
    SendError =
        2: "Unable to send response over callback channel (receiver dropped).",
    UnexpectedRequestType { request_type: String } =
        3: "Unexpected request type for remote procedure call: {request_type}. This is either a version compatibility issue or a bug.",
    UnexpectedResponseType { response_type: String } =
        4: "Unexpected response type for remote procedure call: {response_type}. This is either a version compatibility issue or a bug.",
    UnknownServer { server: Address } =
        5: "Received replica at unrecognized server: {server}.",
    EnumOutOfBounds { value: i32, enum_name: &'static str } =
        6: "Value '{value}' is out of bounds for enum '{enum_name}'.",
}

#[derive(Clone, PartialEq, Eq)]
pub struct ServerError {
    error_code: String,
    error_domain: String,
    message: String,
    stack_trace: Vec<String>,
}

impl ServerError {
    pub(crate) fn new(error_code: String, error_domain: String, message: String, stack_trace: Vec<String>) -> Self {
        Self { error_code, error_domain, message, stack_trace }
    }

    pub(crate) fn format_code(&self) -> &str {
        &self.error_code
    }

    pub(crate) fn message(&self) -> String {
        self.to_string()
    }

    fn to_string(&self) -> String {
        if self.stack_trace.is_empty() {
            format!("[{}] {}. {}", self.error_code, self.error_domain, self.message)
        } else {
            format!("\n{}", self.stack_trace.join("\nCaused: "))
        }
    }
}

impl fmt::Display for ServerError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.to_string())
    }
}

impl fmt::Debug for ServerError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        fmt::Display::fmt(self, f)
    }
}

/// Represents errors encountered during operation.
#[derive(Clone, Debug, PartialEq, Eq)]
pub enum Error {
    Connection(ConnectionError),
    Concept(ConceptError),
    Internal(InternalError),
    Server(ServerError),
    Other(String),
}

impl Error {
    pub fn code(&self) -> String {
        match self {
            Self::Connection(error) => error.format_code(),
            Self::Concept(error) => error.format_code(),
            Self::Internal(error) => error.format_code(),
            Self::Server(error) => error.format_code().to_owned(),
            Self::Other(_error) => String::new(),
        }
    }

    pub fn message(&self) -> String {
        match self {
            Self::Connection(error) => error.message(),
            Self::Concept(error) => error.message(),
            Self::Internal(error) => error.message(),
            Self::Server(error) => error.message(),
            Self::Other(error) => error.clone(),
        }
    }

    fn try_extracting_connection_error(status: &Status, code: &str) -> Option<ConnectionError> {
        // TODO: We should probably catch more connection errors instead of wrapping them into
        // ServerErrors. However, the most valuable information even for connection is inside
        // stacktraces now.
        match code {
            "AUT2" | "AUT3" => Some(ConnectionError::TokenCredentialInvalid {}),
            _ => None,
        }
    }

    fn from_message(message: &str) -> Self {
        // TODO: Consider converting some of the messages to connection errors
        Self::Other(message.to_owned())
    }

    fn parse_unavailable(status_message: &str) -> Error {
        if status_message == "broken pipe" {
            Error::Connection(ConnectionError::BrokenPipe)
        } else if status_message.contains("received corrupt message") {
            Error::Connection(ConnectionError::EncryptionSettingsMismatch)
        } else if status_message.contains("UnknownIssuer") {
            Error::Connection(ConnectionError::SSLCertificateNotValidated)
        } else if status_message.contains("Connection refused") {
            Error::Connection(ConnectionError::ConnectionFailed)
        } else {
            Error::Connection(ConnectionError::ServerConnectionFailedStatusError { error: status_message.to_owned() })
        }
    }
}

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Connection(error) => write!(f, "{error}"),
            Self::Concept(error) => write!(f, "{error}"),
            Self::Internal(error) => write!(f, "{error}"),
            Self::Server(error) => write!(f, "{error}"),
            Self::Other(message) => write!(f, "{message}"),
        }
    }
}

impl StdError for Error {
    fn source(&self) -> Option<&(dyn StdError + 'static)> {
        match self {
            Self::Connection(error) => Some(error),
            Self::Concept(error) => Some(error),
            Self::Internal(error) => Some(error),
            Self::Server(_) => None,
            Self::Other(_) => None,
        }
    }
}

impl From<ConnectionError> for Error {
    fn from(error: ConnectionError) -> Self {
        Self::Connection(error)
    }
}

impl From<ConceptError> for Error {
    fn from(error: ConceptError) -> Self {
        Self::Concept(error)
    }
}

impl From<InternalError> for Error {
    fn from(error: InternalError) -> Self {
        Self::Internal(error)
    }
}

impl From<ServerError> for Error {
    fn from(error: ServerError) -> Self {
        Self::Server(error)
    }
}

impl From<Status> for Error {
    fn from(status: Status) -> Self {
        if let Ok(details) = status.check_error_details() {
            if let Some(bad_request) = details.bad_request() {
                Self::Connection(ConnectionError::ServerConnectionFailedWithError {
                    error: format!("{:?}", bad_request),
                })
            } else if let Some(error_info) = details.error_info() {
                let code = error_info.reason.clone();
                if let Some(connection_error) = Self::try_extracting_connection_error(&status, &code) {
                    return Self::Connection(connection_error);
                }
                let domain = error_info.domain.clone();
                let stack_trace =
                    if let Some(debug_info) = details.debug_info() { debug_info.stack_entries.clone() } else { vec![] };

                Self::Server(ServerError::new(code, domain, status.message().to_owned(), stack_trace))
            } else {
                Self::from_message(status.message())
            }
        } else {
            if status.code() == Code::Unavailable {
                Self::parse_unavailable(status.message())
            } else if status.code() == Code::Unknown
                || is_rst_stream(&status)
                || status.code() == Code::FailedPrecondition
                || status.code() == Code::AlreadyExists
            {
                Self::Connection(ConnectionError::ServerConnectionFailedStatusError {
                    error: status.message().to_owned(),
                })
            } else if status.code() == Code::Unimplemented {
                Self::Connection(ConnectionError::RPCMethodUnavailable { message: status.message().to_owned() })
            } else {
                Self::from_message(status.message())
            }
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
    fn from(_err: tokio::sync::mpsc::error::SendError<T>) -> Self {
        Self::Internal(InternalError::SendError)
    }
}

impl From<tokio::sync::oneshot::error::RecvError> for Error {
    fn from(_err: tokio::sync::oneshot::error::RecvError) -> Self {
        Self::Internal(InternalError::RecvError)
    }
}

impl From<crossbeam::channel::RecvError> for Error {
    fn from(_err: crossbeam::channel::RecvError) -> Self {
        Self::Internal(InternalError::RecvError)
    }
}

impl<T> From<crossbeam::channel::SendError<T>> for Error {
    fn from(_err: crossbeam::channel::SendError<T>) -> Self {
        Self::Internal(InternalError::SendError)
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
