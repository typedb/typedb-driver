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

use grpc::Error as GrpcError;
use std::error::Error as StdError;
use std::fmt::{Debug, Display, Formatter};

pub mod message;

use super::error::message::Message;

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

impl From<Message<'_>> for Error {
    fn from(msg: Message) -> Self {
        Error::new(msg)
    }
}

impl Error {
    pub fn new(msg: Message) -> Error {
        Error::Other(String::from(msg))
    }

    pub fn from_grpc(msg: Message, source: GrpcError) -> Error {
        Error::GrpcError(String::from(msg), source)
    }
}
