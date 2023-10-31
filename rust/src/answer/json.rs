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

use std::{
    borrow::Cow,
    collections::HashMap,
    fmt::{self, Write},
};

#[derive(Clone, Debug)]
pub enum JSON {
    Object(HashMap<Cow<'static, str>, JSON>),
    List(Vec<JSON>),
    String(Cow<'static, str>),
    Number(f64),
    Boolean(bool),
}

impl fmt::Display for JSON {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            JSON::Object(object) => {
                f.write_char('{')?;
                for (i, (k, v)) in object.iter().enumerate() {
                    if i > 0 {
                        f.write_str(", ")?;
                    }
                    write!(f, r#""{}": {}"#, k, v)?;
                }
                f.write_char('}')?;
            }
            JSON::List(list) => {
                f.write_char('[')?;
                for (i, v) in list.iter().enumerate() {
                    if i > 0 {
                        f.write_str(", ")?;
                    }
                    write!(f, "{}", v)?;
                }
                f.write_char(']')?;
            }
            JSON::String(string) => write!(f, r#""{string}""#)?,
            JSON::Number(number) => write!(f, "{number}")?,
            JSON::Boolean(boolean) => write!(f, "{boolean}")?,
        }
        Ok(())
    }
}
