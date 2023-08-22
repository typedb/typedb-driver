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

use std::{fmt, str::FromStr};

use http::Uri;

use crate::common::{Error, Result};

#[derive(Clone, Debug, Hash, PartialEq, Eq)]
pub struct Address {
    uri: Uri,
}

impl Address {
    pub(crate) fn into_uri(self) -> Uri {
        self.uri
    }
}

impl FromStr for Address {
    type Err = Error;

    fn from_str(address: &str) -> Result<Self> {
        let uri = if address.contains("://") {
            address.parse::<Uri>()?
        } else {
            format!("http://{address}").parse::<Uri>()?
        };
        Ok(Self { uri })
    }
}

impl fmt::Display for Address {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.uri.authority().unwrap())
    }
}
