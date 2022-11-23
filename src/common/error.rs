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

// use grpc::{Error as GrpcError, GrpcMessageError, GrpcStatus};
use std::error::Error as StdError;
use std::fmt::{Debug, Display, Formatter};
use tonic::Status;

// TODO: try refactoring out the lifetime by storing String instead of &str
struct MessageTemplate<'a> {
    code_prefix: &'a str,
    msg_prefix: &'a str,
}

impl MessageTemplate<'_> {
    const fn new<'a>(code_prefix: &'a str, msg_prefix: &'a str) -> MessageTemplate<'a> {
        MessageTemplate {
            code_prefix,
            msg_prefix
        }
    }
}

// TODO: try introducing generic type parameter representing the template variable list
pub struct Message<'a> {
    code_prefix: &'a str,
    code_number: u8,
    msg_prefix: &'a str,
    msg_body: &'a str
}

impl Message<'_> {
    const fn new<'a>(template: MessageTemplate<'a>, code_number: u8, msg_body: &'a str) -> Message<'a> {
        Message {
            code_prefix: template.code_prefix,
            code_number,
            msg_prefix: template.msg_prefix,
            msg_body
        }
    }

    fn format(&self, args: Vec<&str>) -> String {
        let expected_arg_count = self.msg_body.matches("{}").count();
        assert_eq!(
            expected_arg_count, args.len(),
            "Message template `{}` takes `{}` args but `{}` were provided",
            self.msg_body, expected_arg_count, args.len()
        );
        format!("[{}{:0>2}] {}: {}", self.code_prefix, self.code_number, self.msg_prefix, self.expand_msg(args))
    }

    pub(crate) fn to_err(&self, args: Vec<&str>) -> Error {
        Error::new(self.format(args))
    }

    fn expand_msg(&self, args: Vec<&str>) -> String {
        let arg_count = args.len();
        let msg_split_indexed = self.msg_body.split("{}").enumerate();
        let mut formatted_msg = String::new();
        for (idx, fragment) in msg_split_indexed {
            formatted_msg.push_str(fragment);
            if idx < arg_count { formatted_msg.push_str(args[idx]) }
        }
        formatted_msg
    }
}

impl From<Message<'_>> for String {
    fn from(msg: Message) -> Self {
        assert!(!msg.msg_body.contains("{}"));
        String::from(msg.msg_body)
    }
}

struct MessageTemplates<'a> {
    client: MessageTemplate<'a>,
    concept: MessageTemplate<'a>
}

impl MessageTemplates<'_> {
    const fn new() -> MessageTemplates<'static> {
        MessageTemplates {
            client: MessageTemplate::new("CLI", "Client Error"),
            concept: MessageTemplate::new("CON", "Concept Error")
        }
    }
}

const TEMPLATES: MessageTemplates = MessageTemplates::new();

pub struct ClientMessages<'a> {
    pub session_is_closed: Message<'a>,
    pub transaction_is_closed: Message<'a>,
    pub transaction_is_closed_with_errors: Message<'a>,
    pub unable_to_connect: Message<'a>,
    pub db_does_not_exist: Message<'a>,
    pub missing_response_field: Message<'a>,
    pub unknown_request_id: Message<'a>,
    pub cluster_replica_not_primary: Message<'a>,
    pub cluster_token_credential_invalid: Message<'a>,
    pub session_close_failed: Message<'a>,
    pub session_was_never_closed: Message<'a>,
}

pub struct ConceptMessages<'a> {
    pub invalid_concept_casting: Message<'a>,
}

pub struct Messages<'a> {
    pub client: ClientMessages<'a>,
    pub concept: ConceptMessages<'a>,
}

impl Messages<'_> {
    const fn new() -> Messages<'static> {
        Messages {
            client: ClientMessages {
                session_is_closed: Message::new(TEMPLATES.client, 2, "The session is closed and no further operation is allowed."),
                transaction_is_closed: Message::new(TEMPLATES.client, 3, "The transaction is closed and no further operation is allowed."),
                transaction_is_closed_with_errors: Message::new(TEMPLATES.client, 4, "The transaction is closed because of the error(s):\n{}"),
                unable_to_connect: Message::new(TEMPLATES.client, 5, "Unable to connect to TypeDB server."),
                db_does_not_exist: Message::new(TEMPLATES.client, 8, "The database '{}' does not exist."),
                missing_response_field: Message::new(TEMPLATES.client, 9, "Missing field in message received from server: '{}'."),
                unknown_request_id: Message::new(TEMPLATES.client, 10, "Received a response with unknown request id '{}'"),
                cluster_replica_not_primary: Message::new(TEMPLATES.client, 13, "The replica is not the primary replica."),
                cluster_token_credential_invalid: Message::new(TEMPLATES.client, 16, "Invalid token credential."),
                session_close_failed: Message::new(TEMPLATES.client, 17, "Failed to close session. It may still be open on the server, or it may already have been closed previously."),
                session_was_never_closed: Message::new(TEMPLATES.client, 18, "A session went out of scope without being closed. Sessions should always be closed by awaiting Session::close."),
            },
            concept: ConceptMessages {
                invalid_concept_casting: Message::new(TEMPLATES.concept, 1, "Invalid concept conversion from '{}' to '{}'"),
            },
        }
    }
}

pub const MESSAGES: Messages = Messages::new();

#[derive(Debug)]
pub enum Error {
    // GrpcError(String, GrpcError),
    Other(String),
}

impl Error {
    pub(crate) fn new(msg: String) -> Self {
        Error::Other(msg)
    }

    // TODO: rewrite this to work with Tonic (and error library from 'typeql')
    // pub(crate) fn from_grpc(source: GrpcError) -> Self {
    //     match source {
    //         GrpcError::Http(_) => Error::GrpcError(String::from(MESSAGES.client.unable_to_connect), source),
    //         GrpcError::GrpcMessage(ref err) => {
    //             // TODO: this is awkward because we use gRPC errors to represent some user errors too
    //             if Error::is_replica_not_primary(err) { Error::new(MESSAGES.client.cluster_replica_not_primary.format(vec![])) }
    //             else if Error::is_token_credential_invalid(err) { Error::new(MESSAGES.client.cluster_token_credential_invalid.format(vec![])) }
    //             else { Error::GrpcError(source.to_string().replacen("grpc message error: ", "", 1), source) }
    //         },
    //         _ => Error::GrpcError(source.to_string(), source)
    //     }
    // }

    // fn is_replica_not_primary(err: &GrpcMessageError) -> bool {
    //     err.grpc_status == GrpcStatus::Internal as i32 && err.grpc_message.contains("[RPL01]")
    // }
    //
    // fn is_token_credential_invalid(err: &GrpcMessageError) -> bool {
    //     err.grpc_status == GrpcStatus::Unauthenticated as i32 && err.grpc_message.contains("[CLS08]")
    // }
}

impl Display for Error {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let message = match self {
            // Error::GrpcError(msg, _) => msg,
            Error::Other(msg) => msg
        };
        write!(f, "{}", message)
    }
}

impl StdError for Error {
    fn source(&self) -> Option<&(dyn StdError + 'static)> {
        match self {
            // Error::GrpcError(_, source) => Some(source),
            Error::Other(_) => None
        }
    }
}

impl From<Status> for Error {
    fn from(status: Status) -> Self {
        Error::Other(status.to_string())
    }
}

impl From<futures::channel::mpsc::SendError> for Error {
    fn from(err: futures::channel::mpsc::SendError) -> Self {
        Error::Other(err.to_string())
    }
}

impl<T> From<tokio::sync::mpsc::error::SendError<T>> for Error {
    fn from(err: tokio::sync::mpsc::error::SendError<T>) -> Self {
        Error::Other(err.to_string())
    }
}
