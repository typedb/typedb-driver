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
    Null,
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
            JSON::Null => write!(f, "null")?,
        }
        Ok(())
    }
}

fn write_escaped_string(string: &str, f: &mut fmt::Formatter<'_>) -> fmt::Result {
    const HEX: u8 = 0;
    const BSP: u8 = b'b';
    const TAB: u8 = b't';
    const LF_: u8 = b'n';
    const FF_: u8 = b'f';
    const CR_: u8 = b'r';

    const ASCII_CONTROL: usize = 0x20;

    const ESCAPE: [u8; ASCII_CONTROL] = [
        HEX, HEX, HEX, HEX, HEX, HEX, HEX, HEX, //
        BSP, TAB, LF_, HEX, FF_, CR_, HEX, HEX, //
        HEX, HEX, HEX, HEX, HEX, HEX, HEX, HEX, //
        HEX, HEX, HEX, HEX, HEX, HEX, HEX, HEX, //
    ];

    const HEX_DIGITS: &[u8; 0x10] = b"0123456789abcdef";

    let mut buf = Vec::with_capacity(string.len());

    for byte in string.bytes() {
        if (byte as usize) < ASCII_CONTROL {
            match ESCAPE[byte as usize] {
                HEX => {
                    buf.extend_from_slice(&[
                        b'\\',
                        b'u',
                        b'0',
                        b'0',
                        HEX_DIGITS[(byte as usize & 0xF0) >> 4],
                        HEX_DIGITS[byte as usize & 0x0F],
                    ]);
                }
                special => buf.extend_from_slice(&[b'\\', special]),
            }
        } else {
            match byte {
                b'"' | b'\\' => buf.extend_from_slice(&[b'\\', byte]),
                _ => buf.push(byte),
            }
        }
    }

    write!(f, r#""{}""#, unsafe { String::from_utf8_unchecked(buf) })
}

#[cfg(test)]
mod test {
    use std::borrow::Cow;

    use super::JSON;

    #[test]
    fn test_against_serde() {
        let string: String =
            (0u8..0x7fu8).map(|byte| byte as char).chain("lorem ипсум どぉる　سيتامعت".chars()).collect();
        let serde_json_value = serde_json::value::Value::String(string.clone());
        let json_string = JSON::String(Cow::Owned(string));
        assert_eq!(serde_json::to_string(&serde_json_value).unwrap(), json_string.to_string());
    }
}
