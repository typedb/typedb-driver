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

macro_rules! error {
    ($fn_name:ident, $number:literal, $text:literal) => {
        pub(crate) fn $fn_name() -> String {
            format!("[CLI{:0>2}]: {}", $number, $text)
        }
    };
}
macro_rules! error_args_1 {
    ($fn_name:ident, $number:literal, $text:literal) => {
        pub(crate) fn $fn_name(arg0: &str) -> String {
            format!("[CLI{:0>2}]: {}", $number, format!($text, arg0))
        }
    };
}
macro_rules! error_args_2 {
    ($fn_name:ident, $number:literal, $text:literal) => {
        pub(crate) fn $fn_name(arg0: &str, arg1: &str) -> String {
            format!("[CLI{:0>2}]: {}", $number, format!($text, arg0, arg1))
        }
    };
}

#[derive(Default)]
pub struct MessageTemplate<'a> {
    code_prefix: &'a str,
    code_number: u8,
    msg_prefix: &'a str,
    msg_body: &'a str
}

enum Message {
    Args0(MessageTemplate<'_>),
    Args1(MessageTemplate<'_>),
    Args2(MessageTemplate<'_>)
}

impl Message::Args0 {
    fn get() -> String {
        format!("[{}{:0>2}] {}: {}", msg.code_prefix, msg.code_number, msg.msg_prefix, msg.msg_body)
    }
}

impl Message::Args1 {
    fn format(arg0: &str) -> String {
        format!("[{}{:0>2}] {}: ", msg.code_prefix, msg.code_number, msg.msg_prefix) + msg.msg_body
    }
}

// impl From<Message> for String {
//     fn from(msg: Message) -> Self {
//         format!("[{}{:0>2}] {}: {}", msg.code_prefix, msg.code_number, msg.msg_prefix, msg.msg_body)
//     }
// }

pub(crate) mod client {
    use super::Message;

    const PREFIX: Message = Message { code_prefix: "CLI", msg_prefix: "Client Error", ..Default::default() };

    pub const UNABLE_TO_CONNECT: Message = Message { code_number: 5, msg_body: "Unable to connect to TypeDB server.", ..PREFIX };

    // client_error_args_1!(transaction_closed_with_errors, 4, "The transaction has been closed with error(s): \n{}");
    // client_error!(unable_to_connect, 5, "Unable to connect to TypeDB server.");

    pub fn session_id_exists(id: &str) -> String {
        format!("The newly opened session id '{}' already exists.", id)
    }

    pub fn session_closed() -> &'static str {
        "The session has been closed and no further operation is allowed."
    }
}
