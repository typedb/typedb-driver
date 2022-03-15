/*
 * Copyright (C) 2021 Vaticle
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

pub mod message;
pub mod messages;

use grpc::{Error as GrpcError, GrpcMessageError, GrpcStatus};
use std::error::Error as StdError;
use std::fmt::{Debug, Display, Formatter};

use crate::common::error::message::Message;
use crate::common::error::messages::ERRORS;

#[derive(Debug)]
pub enum Error {
    GrpcError(String, GrpcError),
    Other(String),
}

impl Display for Error {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let message = match self {
            Error::GrpcError(msg, _) => msg,
            Error::Other(msg) => msg
        };
        write!(f, "{}", message)
    }
}

impl StdError for Error {
    fn source(&self) -> Option<&(dyn StdError + 'static)> {
        match self {
            Error::GrpcError(_, source) => Some(source),
            Error::Other(_) => None
        }
    }
}

impl Error {
    pub fn new(msg: Message) -> Error {
        Error::Other(String::from(msg))
    }

    pub fn format(msg: Message, args: Vec<&str>) -> Error {
        Error::Other(msg.format(args))
    }

    pub fn from_grpc(source: GrpcError) -> Error {
        match source {
            GrpcError::Http(_) => Error::GrpcError(String::from(ERRORS.client.unable_to_connect), source),
            GrpcError::GrpcMessage(ref err) => {
                if Error::is_replica_not_primary(err) { Error::new(ERRORS.client.cluster_replica_not_primary) }
                else if Error::is_token_credential_invalid(err) { Error::new(ERRORS.client.cluster_token_credential_invalid) }
                else { Error::GrpcError(source.to_string().replacen("grpc message error: ", "", 1), source) }
            },
            _ => Error::GrpcError(source.to_string(), source)
        }
    }

    // TODO: propagate exception from the server in a less brittle way
    fn is_replica_not_primary(err: &GrpcMessageError) -> bool {
        err.grpc_status == GrpcStatus::Internal as i32 && err.grpc_message.contains("[RPL01]")
    }

    fn is_token_credential_invalid(err: &GrpcMessageError) -> bool {
        err.grpc_status == GrpcStatus::Unauthenticated as i32 && err.grpc_message.contains("[CLS08]")
    }
}
