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
    collections::{HashMap, HashSet},
    env, fs,
    fs::File,
    io::{Read, Write},
    ops::Deref,
    path::{Path, PathBuf},
};

use cucumber::gherkin::Step;
use macro_rules_attribute::apply;
use tokio::time::{sleep, Duration};
use typedb_driver::{answer::JSON, Result as TypeDBResult};
use uuid::Uuid;

use crate::{generic_step, params, Context};

pub fn iter_table(step: &Step) -> impl Iterator<Item = &str> {
    step.table().unwrap().rows.iter().flatten().map(String::as_str)
}

pub fn iter_table_map(step: &Step) -> impl Iterator<Item = HashMap<&str, &str>> {
    let (keys, rows) = step.table().unwrap().rows.split_first().unwrap();
    rows.iter().map(|row| keys.iter().zip(row).map(|(k, v)| (k.as_str(), v.as_str())).collect())
}

pub fn list_contains_json(list: &Vec<JSON>, json: &JSON) -> bool {
    list.iter().any(|list_json| jsons_equal_up_to_reorder(list_json, json))
}

pub(crate) fn parse_json(json: &str) -> TypeDBResult<JSON> {
    fn serde_json_into_fetch_answer(json: serde_json::Value) -> JSON {
        match json {
            serde_json::Value::Null => JSON::Null,
            serde_json::Value::Bool(bool) => JSON::Boolean(bool),
            serde_json::Value::Number(number) => JSON::Number(number.as_f64().unwrap()),
            serde_json::Value::String(string) => JSON::String(Cow::Owned(string)),
            serde_json::Value::Array(array) => {
                JSON::Array(array.into_iter().map(serde_json_into_fetch_answer).collect())
            }
            serde_json::Value::Object(object) => JSON::Object(
                object.into_iter().map(|(k, v)| (Cow::Owned(k), serde_json_into_fetch_answer(v))).collect(),
            ),
        }
    }

    serde_json::from_str(json)
        .map(serde_json_into_fetch_answer)
        .map_err(|e| format!("Could not parse expected fetch answer: {e:?}").into())
}

fn jsons_equal_up_to_reorder(lhs: &JSON, rhs: &JSON) -> bool {
    match (lhs, rhs) {
        (JSON::Object(lhs), JSON::Object(rhs)) => {
            if lhs.len() != rhs.len() {
                return false;
            }
            lhs.iter().all(|(key, lhs_value)| match rhs.get(key) {
                Some(rhs_value) => jsons_equal_up_to_reorder(lhs_value, rhs_value),
                None => false,
            })
        }
        (JSON::Array(lhs), JSON::Array(rhs)) => {
            if lhs.len() != rhs.len() {
                return false;
            }
            let mut rhs_matches = HashSet::new();
            for item in lhs {
                match rhs
                    .iter()
                    .enumerate()
                    .filter(|(i, _)| !rhs_matches.contains(i))
                    .find_map(|(i, rhs_item)| jsons_equal_up_to_reorder(item, rhs_item).then_some(i))
                {
                    Some(idx) => {
                        rhs_matches.insert(idx);
                    }
                    None => return false,
                }
            }
            true
        }
        (JSON::String(lhs), JSON::String(rhs)) => lhs == rhs,
        (&JSON::Number(lhs), &JSON::Number(rhs)) => equals_approximate(lhs, rhs),
        (JSON::Boolean(lhs), JSON::Boolean(rhs)) => lhs == rhs,
        (JSON::Null, JSON::Null) => true,
        _ => false,
    }
}

pub fn equals_approximate(first: f64, second: f64) -> bool {
    const EPS: f64 = 1e-10;
    (first - second).abs() < EPS
}

#[macro_export]
macro_rules! assert_with_timeout {
    ($expr:expr, $message:expr $(, $arg:expr)* $(,)?) => {{
        't: {
            for _ in 0..Context::STEP_REATTEMPT_LIMIT {
                if $expr {
                    break 't;
                }
                sleep(Context::STEP_REATTEMPT_SLEEP).await;
            }
            panic!($message $(, $arg)*);
        }
    }};
}

#[apply(generic_step)]
#[step(expr = "set time-zone: {word}")]
async fn set_time_zone(_context: &mut Context, timezone: String) {
    env::set_var("TZ", timezone);
}

#[apply(generic_step)]
#[step(expr = "wait {word} seconds")]
async fn wait_seconds(_context: &mut Context, seconds: String) {
    sleep(Duration::from_secs(seconds.parse().unwrap())).await
}

#[derive(Debug)]
pub struct TempDir(PathBuf);

impl Drop for TempDir {
    fn drop(&mut self) {
        fs::remove_dir_all(&self.0).ok();
    }
}

impl AsRef<Path> for TempDir {
    fn as_ref(&self) -> &Path {
        self
    }
}

impl Deref for TempDir {
    type Target = Path;

    fn deref(&self) -> &Self::Target {
        &self.0
    }
}

pub fn create_temp_dir() -> TempDir {
    let dir_name = format!("temp-{}", Uuid::new_v4());
    let dir = env::temp_dir().join(Path::new(&dir_name));
    fs::create_dir_all(&dir).expect("Expected to create a temporary dir");
    TempDir(dir)
}

pub(crate) fn read_file_to_string(path: PathBuf) -> String {
    let mut content = String::new();
    let _ = File::open(path).expect("Expected file").read_to_string(&mut content);
    content
}

pub(crate) fn read_file(path: PathBuf) -> Vec<u8> {
    let mut buffer = Vec::new();
    let _ = File::open(path).expect("Expected file").read_to_end(&mut buffer);
    buffer
}

pub(crate) fn write_file(path: PathBuf, data: &[u8]) {
    let mut file = fs::OpenOptions::new().write(true).create(true).open(path).expect("Expected file open to write");
    file.write_all(data).expect("Expected file write");
    file.flush().expect("Expected flush");
}

#[apply(generic_step)]
#[step(expr = r"file\({word}\) {exists_or_doesnt}")]
async fn file_exists(context: &mut Context, file_name: String, exists_or_doesnt: params::ExistsOrDoesnt) {
    let path = context.get_full_file_path(&file_name);
    exists_or_doesnt.check_bool(path.exists(), &format!("path: '{path:?}'"));
}

#[apply(generic_step)]
#[step(expr = r"file\({word}\) {is_or_not} empty")]
async fn file_is_empty(context: &mut Context, file_name: String, is_or_not: params::IsOrNot) {
    let path = context.get_full_file_path(&file_name);
    is_or_not.check(read_file(path).is_empty());
}

#[apply(generic_step)]
#[step(expr = r"file\({word}\) write:")]
async fn file_write(context: &mut Context, file_name: String, step: &Step) {
    let data = step.docstring.as_ref().unwrap().trim().to_string();
    let path = context.get_full_file_path(&file_name);
    write_file(path, data.as_bytes());
}
