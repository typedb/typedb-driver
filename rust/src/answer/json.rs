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
    iter,
};

use itertools::Itertools;
use serde::{
    ser::{SerializeMap, SerializeSeq},
    Deserialize, Serialize,
};

#[derive(Clone, Debug, PartialEq)]
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

impl Serialize for JSON {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        match self {
            Self::Object(object) => {
                let mut map = serializer.serialize_map(Some(object.len()))?;
                for (key, value) in object {
                    map.serialize_entry(key, value)?;
                }
                map.end()
            }
            Self::Array(array) => {
                let mut seq = serializer.serialize_seq(Some(array.len()))?;
                for item in array {
                    seq.serialize_element(item)?;
                }
                seq.end()
            }
            Self::String(string) => serializer.serialize_str(string),
            &Self::Number(number) => serializer.serialize_f64(number),
            &Self::Boolean(boolean) => serializer.serialize_bool(boolean),
            Self::Null => serializer.serialize_unit(),
        }
    }
}

impl<'de> Deserialize<'de> for JSON {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        struct Visitor;

        impl<'de> serde::de::Visitor<'de> for Visitor {
            type Value = JSON;

            fn expecting(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
                formatter.write_str("a valid JSON value")
            }

            fn visit_bool<E>(self, value: bool) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Boolean(value))
            }

            fn visit_i64<E>(self, value: i64) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Number(value as f64))
            }

            fn visit_i128<E>(self, value: i128) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Number(value as f64))
            }

            fn visit_u64<E>(self, value: u64) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Number(value as f64))
            }

            fn visit_u128<E>(self, value: u128) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Number(value as f64))
            }

            fn visit_f64<E>(self, value: f64) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Number(value))
            }

            fn visit_str<E>(self, value: &str) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::String(Cow::Owned(value.to_owned())))
            }

            fn visit_string<E>(self, value: String) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::String(Cow::Owned(value)))
            }

            fn visit_none<E>(self) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Null)
            }

            fn visit_some<D>(self, deserializer: D) -> Result<Self::Value, D::Error>
            where
                D: serde::Deserializer<'de>,
            {
                JSON::deserialize(deserializer)
            }

            fn visit_unit<E>(self) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                Ok(JSON::Null)
            }

            fn visit_seq<A>(self, mut seq: A) -> Result<Self::Value, A::Error>
            where
                A: serde::de::SeqAccess<'de>,
            {
                Ok(JSON::Array(iter::from_fn(|| seq.next_element().transpose()).try_collect()?))
            }

            fn visit_map<A>(self, mut map: A) -> Result<Self::Value, A::Error>
            where
                A: serde::de::MapAccess<'de>,
            {
                Ok(JSON::Object(iter::from_fn(|| map.next_entry().transpose()).try_collect()?))
            }
        }

        deserializer.deserialize_any(Visitor)
    }
}

#[cfg(test)]
mod test {
    use std::{borrow::Cow, collections::HashMap, iter};

    use rand::{
        distributions::{DistString, Distribution, Standard, WeightedIndex},
        rngs::ThreadRng,
        thread_rng, Rng,
    };
    use serde_json::json;

    use super::JSON;

    #[test]
    fn test_against_serde() {
        let string: String =
            (0u8..0x7fu8).map(|byte| byte as char).chain("lorem ипсум どぉる　سيتامعت".chars()).collect();
        let serde_json_value = serde_json::value::Value::String(string.clone());
        let json_string = JSON::String(Cow::Owned(string));
        assert_eq!(serde_json::to_string(&serde_json_value).unwrap(), json_string.to_string());
    }

    fn sample_json() -> JSON {
        JSON::Object(HashMap::from([
            ("array".into(), JSON::Array(vec![JSON::Boolean(true), JSON::String("string".into())])),
            ("number".into(), JSON::Number(123.4)),
        ]))
    }

    #[test]
    fn serialize() {
        let ser = serde_json::to_value(sample_json())
        .unwrap();
        let value = json!( { "array": [true, "string"], "number": 123.4 });
        assert_eq!(ser, value);
    }

    #[test]
    fn deserialize() {
        let deser: JSON = serde_json::from_str(r#"{ "array": [true, "string"], "number": 123.4 }"#).unwrap();
        let json = sample_json();
        assert_eq!(deser, json);
    }

    fn random_string(rng: &mut impl Rng) -> String {
        let len = rng.gen_range(0..64);
        Standard.sample_string(rng, len)
    }

    fn random_json<R: Rng>(rng: &mut R) -> JSON {
        let weights = [1, 1, 3, 3, 3, 3];
        let generators: &[fn(&mut R) -> JSON] = &[
            |rng| {
                let len = rng.gen_range(0..12);
                JSON::Object(
                    iter::from_fn(|| Some((Cow::Owned(random_string(rng)), random_json(rng)))).take(len).collect(),
                )
            },
            |rng| {
                let len = rng.gen_range(0..12);
                JSON::Array(iter::from_fn(|| Some(random_json(rng))).take(len).collect())
            },
            |rng| JSON::String(Cow::Owned(random_string(rng))),
            |rng| JSON::Number(rng.gen()),
            |rng| JSON::Boolean(rng.gen()),
            |_| JSON::Null,
        ];
        let dist = WeightedIndex::new(&weights).unwrap();
        generators[dist.sample(rng)](rng)
    }

    #[test]
    fn serde_roundtrip() {
        let mut rng = thread_rng();
        for _ in 0..1000 {
            let json = random_json(&mut rng);
            let deser = serde_json::from_value(serde_json::to_value(&json).unwrap()).unwrap();
            assert_eq!(json, deser);
        }
    }
}
