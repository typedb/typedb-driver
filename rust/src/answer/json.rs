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
    Array(Vec<JSON>),
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
            JSON::Array(list) => {
                f.write_char('[')?;
                for (i, v) in list.iter().enumerate() {
                    if i > 0 {
                        f.write_str(", ")?;
                    }
                    write!(f, "{}", v)?;
                }
                f.write_char(']')?;
            }
            JSON::String(string) => write_escaped_string(string, f)?,
            JSON::Number(number) => write!(f, "{number}")?,
            JSON::Boolean(boolean) => write!(f, "{boolean}")?,
        }
        Ok(())
    }
}

fn write_escaped_string(string: &str, f: &mut fmt::Formatter<'_>) -> fmt::Result {
    const HEX: u8 = b'u';
    const RAW: u8 = 0;

    const BSP: u8 = b'b';
    const TAB: u8 = b't';
    const LF_: u8 = b'n';
    const FF_: u8 = b'f';
    const CR_: u8 = b'r';

    const ASCII_CONTROL_MAX: usize = 0x1F;

    const CTRL: [u8; ASCII_CONTROL_MAX + 1] = [
        HEX, HEX, HEX, HEX, HEX, HEX, HEX, HEX, //
        BSP, TAB, LF_, HEX, FF_, CR_, HEX, HEX, //
        HEX, HEX, HEX, HEX, HEX, HEX, HEX, HEX, //
        HEX, HEX, HEX, HEX, HEX, HEX, HEX, HEX, //
    ];

    f.write_char('"')?;
    for char in string.chars() {
        if char as usize <= ASCII_CONTROL_MAX {
            match CTRL[char as usize] {
                RAW => f.write_char(char)?,
                HEX => write!(f, "\\u{:04x}", char as u8)?,
                special => write!(f, "\\{}", special as char)?,
            }
        } else {
            match char {
                '"' | '\\' => write!(f, "\\{}", char)?,
                _ => f.write_char(char)?,
            }
        }
    }
    f.write_char('"')
}
