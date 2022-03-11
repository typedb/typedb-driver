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
//
// macro_rules! error {
//     ($fn_name:ident, $number:literal, $text:literal) => {
//         pub(crate) fn $fn_name() -> String {
//             format!("[CLI{:0>2}]: {}", $number, $text)
//         }
//     };
// }
// macro_rules! error_args_1 {
//     ($fn_name:ident, $number:literal, $text:literal) => {
//         pub(crate) fn $fn_name(arg0: &str) -> String {
//             format!("[CLI{:0>2}]: {}", $number, format!($text, arg0))
//         }
//     };
// }
// macro_rules! error_args_2 {
//     ($fn_name:ident, $number:literal, $text:literal) => {
//         pub(crate) fn $fn_name(arg0: &str, arg1: &str) -> String {
//             format!("[CLI{:0>2}]: {}", $number, format!($text, arg0, arg1))
//         }
//     };
// }

pub(super) struct MessageTemplate<'a> {
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

    pub(crate) fn message(&self, args: Vec<&str>) -> String {
        if self.msg_body.contains("{}") {
            format!("[{}{:0>2}] {}: {}", self.code_prefix, self.code_number, self.msg_prefix,
                    self.msg_body.clone().replace("{}", args[0]))
        } else {
            format!("[{}{:0>2}] {}: {}", self.code_prefix, self.code_number, self.msg_prefix, self.msg_body.clone())
        }
    }
}

// impl Message::Args0 {
//     fn get() -> String {
//         format!("[{}{:0>2}] {}: {}", msg.code_prefix, msg.code_number, msg.msg_prefix, msg.msg_body)
//     }
// }
//
// impl Message::Args1 {
//     fn format(arg0: &str) -> String {
//         format!("[{}{:0>2}] {}: ", msg.code_prefix, msg.code_number, msg.msg_prefix) + msg.msg_body
//     }
// }

// impl From<Message> for String {
//     fn from(msg: Message) -> Self {
//         format!("[{}{:0>2}] {}: {}", msg.code_prefix, msg.code_number, msg.msg_prefix, msg.msg_body)
//     }
// }

struct Templates<'a> {
    client: MessageTemplate<'a>
}

impl Templates<'_> {
    const fn new() -> Templates<'static> {
        Templates {
            client: MessageTemplate::new("CLI", "Client Error")
        }
    }
}

const TEMPLATES: Templates = Templates::new();

pub struct ErrorMessages<'a> {
    pub transaction_closed_with_errors: Message<'a>,
    pub unable_to_connect: Message<'a>
}

impl ErrorMessages<'_> {
    const fn new() -> ErrorMessages<'static> {
        ErrorMessages {
            transaction_closed_with_errors: Message::new(TEMPLATES.client, 4, "The transaction has been closed with error(s): \n{}"),
            unable_to_connect: Message::new(TEMPLATES.client, 5, "Unable to connect to TypeDB server.")
        }
    }
}

pub const ERRORS: ErrorMessages = ErrorMessages::new();

// pub(crate) mod client {
//     use super::Message;
//
//     const PREFIX: Message = Message { code_prefix: "CLI", msg_prefix: "Client Error", ..Default::default() };
//
//     pub const UNABLE_TO_CONNECT: Message = Message { code_number: 5, msg_body: "Unable to connect to TypeDB server.", ..PREFIX };
//
//     // client_error_args_1!(transaction_closed_with_errors, 4, "The transaction has been closed with error(s): \n{}");
//     // client_error!(unable_to_connect, 5, "Unable to connect to TypeDB server.");
//
//     pub fn session_id_exists(id: &str) -> String {
//         format!("The newly opened session id '{}' already exists.", id)
//     }
//
//     pub fn session_closed() -> &'static str {
//         "The session has been closed and no further operation is allowed."
//     }
// }
