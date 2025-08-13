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

use std::{
    borrow::Cow,
    collections::HashMap,
    fmt::{self, Write},
};

use serde::{ser::SerializeMap, ser::SerializeSeq, Serialize, Serializer};

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

impl Serialize for JSON {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        match self {
            JSON::Object(object) => {
                let mut map = serializer.serialize_map(Some(object.len()))?;
                for (key, value) in object {
                    map.serialize_entry(key, value)?;
                }
                map.end()
            }
            JSON::Array(array) => {
                let mut seq = serializer.serialize_seq(Some(array.len()))?;
                for element in array {
                    seq.serialize_element(element)?;
                }
                seq.end()
            }
            JSON::String(string) => serializer.serialize_str(string),
            JSON::Number(number) => serializer.serialize_f64(*number),
            JSON::Boolean(boolean) => serializer.serialize_bool(*boolean),
            JSON::Null => serializer.serialize_unit(),
        }
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

    #[test]
    fn test_serialize_implementation() {
        use std::collections::HashMap;

        // Test Null
        let null_json = JSON::Null;
        assert_eq!(serde_json::to_string(&null_json).unwrap(), "null");

        // Test Boolean
        let bool_json = JSON::Boolean(true);
        assert_eq!(serde_json::to_string(&bool_json).unwrap(), "true");

        // Test Number
        let number_json = JSON::Number(42.5);
        assert_eq!(serde_json::to_string(&number_json).unwrap(), "42.5");

        // Test String
        let string_json = JSON::String(Cow::Borrowed("hello world"));
        assert_eq!(serde_json::to_string(&string_json).unwrap(), r#""hello world""#);

        // Test Array
        let array_json = JSON::Array(vec![
            JSON::Number(1.0),
            JSON::String(Cow::Borrowed("test")),
            JSON::Boolean(false),
            JSON::Null,
        ]);
        assert_eq!(serde_json::to_string(&array_json).unwrap(), r#"[1.0,"test",false,null]"#);

        // Test Object
        let mut object = HashMap::new();
        object.insert(Cow::Borrowed("key1"), JSON::String(Cow::Borrowed("value1")));
        object.insert(Cow::Borrowed("key2"), JSON::Number(123.0));
        let object_json = JSON::Object(object);
        let serialized = serde_json::to_string(&object_json).unwrap();
        
        // Since HashMap order is not guaranteed, check that both possible orders are valid
        assert!(
            serialized == r#"{"key1":"value1","key2":123.0}"# ||
            serialized == r#"{"key2":123.0,"key1":"value1"}"#
        );

        // Test complex nested structure
        let mut complex_object = HashMap::new();
        complex_object.insert(Cow::Borrowed("array"), JSON::Array(vec![
            JSON::Number(1.0),
            JSON::Number(2.0),
        ]));
        complex_object.insert(Cow::Borrowed("nested"), JSON::Object({
            let mut nested = HashMap::new();
            nested.insert(Cow::Borrowed("inner"), JSON::Boolean(true));
            nested
        }));
        let complex_json = JSON::Object(complex_object);
        let complex_serialized = serde_json::to_string(&complex_json).unwrap();
        
        // Verify the serialized JSON can be parsed back
        let _: serde_json::Value = serde_json::from_str(&complex_serialized).unwrap();
    }
}
