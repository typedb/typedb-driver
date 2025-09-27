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
    iter, mem,
    ops::Deref,
    path::{Path, PathBuf},
};

use chrono::{NaiveDate, NaiveDateTime, NaiveTime};
use cucumber::{
    gherkin::{Feature, Step},
    given, then, when, StatsWriter, World,
};
use futures::{
    future::{try_join_all, Either},
    stream::{self, StreamExt},
};
use itertools::Itertools;
use macro_rules_attribute::apply;
use tokio::time::{sleep, Duration};
use typedb_driver::{
    answer::{ConceptRow, JSON},
    concept::{Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Value},
    DatabaseManager, Error, Result as TypeDBResult,
};
use uuid::Uuid;

use crate::{assert_with_timeout, generic_step, params, params::check_boolean, Context};

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


#[cfg(debug_assertions)]
pub mod functor_encoding {
    use itertools::Itertools;

    type FunctorContext = PipelineStructure;

    pub trait FunctorEncoded {
        fn encode_as_functor(&self, context: &FunctorContext) -> String;
    }

    pub mod functor_macros {
        macro_rules! encode_args {
            ($context:ident, { $( $arg:ident, )* } )   => {
                {
                    let arr: Vec<&dyn FunctorEncoded> = vec![ $($arg,)* ];
                    arr.into_iter().map(|s| s.encode_as_functor($context)).join(", ")
                }
            }
        }
        macro_rules! encode_functor_impl {
            ($context:ident, $func:ident $args:tt) => {
                std::format!("{}({})", std::stringify!($func), functor_macros::encode_args!($context, $args))
            };
        }

        macro_rules! add_ignored_fields {
            ($qualified:path { $( $arg:ident, )* }) => {
                $qualified { $( $arg, )* .. }
            };
        }

        macro_rules! encode_functor {
            ($context:ident, $what:ident as struct $struct_name:ident  $fields:tt) => {
                functor_macros::encode_functor!($context, $what => [ $struct_name => $struct_name $fields, ])
            };
            ($context:ident, $what:ident as struct $struct_name:ident $fields:tt named $renamed:ident ) => {
                functor_macros::encode_functor!($context, $what => [ $struct_name => $renamed $fields, ])
            };
            ($context:ident, $what:ident as enum $enum_name:ident [ $($variant:ident $fields:tt |)* ]) => {
                functor_macros::encode_functor!($context, $what => [ $( $enum_name::$variant => $variant $fields ,)* ])
            };
            ($context:ident, $what:ident => [ $($qualified:path => $func:ident $fields:tt, )* ]) => {
                match $what {
                    $( functor_macros::add_ignored_fields!($qualified $fields) => {
                        functor_macros::encode_functor_impl!($context, $func $fields)
                    })*
                }
            };
        }

        macro_rules! impl_functor_for_impl {
            ($which:ident => |$self:ident, $context:ident| $block:block) => {
                impl FunctorEncoded for $which {
                    fn encode_as_functor($self: &Self, $context: &FunctorContext) -> String {
                        $block
                    }
                }
            };
        }

        macro_rules! impl_functor_for {
            (struct $struct_name:ident $fields:tt) => {
                functor_macros::impl_functor_for!(struct $struct_name $fields named $struct_name);
            };
            (struct $struct_name:ident $fields:tt named $renamed:ident) => {
                functor_macros::impl_functor_for_impl!($struct_name => |self, context| {
                    functor_macros::encode_functor!(context, self as struct $struct_name $fields named $renamed)
                });
            };
            (enum $enum_name:ident [ $($func:ident $fields:tt |)* ]) => {
                functor_macros::impl_functor_for_impl!($enum_name => |self, context| {
                    functor_macros::encode_functor!(context, self as enum $enum_name [ $($func $fields |)* ])
                });
            };
            (primitive $primitive:ident) => {
                functor_macros::impl_functor_for_impl!($primitive => |self, _context| { self.to_string() });
            };
        }
        pub(crate) use add_ignored_fields;
        pub(crate) use encode_args;
        pub(crate) use encode_functor;
        pub(crate) use encode_functor_impl;
        pub(crate) use impl_functor_for;
        pub(crate) use impl_functor_for_impl;
    }

    functor_macros::impl_functor_for!(primitive String);
    functor_macros::impl_functor_for!(primitive u64);
    impl<T: FunctorEncoded> FunctorEncoded for Vec<T> {
        fn encode_as_functor(&self, context: &FunctorContext) -> String {
            std::format!("[{}]", self.iter().map(|v| v.encode_as_functor(context)).join(", "))
        }
    }

    impl<T: FunctorEncoded> FunctorEncoded for Option<T> {
        fn encode_as_functor(&self, context: &FunctorContext) -> String {
            self.as_ref().map(|inner| inner.encode_as_functor(context)).unwrap_or("<NONE>".to_owned())
        }
    }

    use typedb_driver::analyze::{conjunction::{Constraint, Conjunction, ConjunctionID, ConstraintVertex, LabelVertex, Reducer, Variable}, FunctionStructure, pipeline::{PipelineStructure, PipelineStage, SortVariable, SortOrder, ReduceAssign}, QueryStructure, ReturnOperation};
    use typedb_driver::analyze::conjunction::{Comparator, ConstraintExactness};
    use typedb_driver::concept::Value;
    use crate::util::functor_encoding::functor_macros::{encode_functor, encode_functor_impl};

    functor_macros::impl_functor_for!(struct ReduceAssign { assigned, reducer,  } named ReduceAssign);
    functor_macros::impl_functor_for!(struct Reducer { reducer, arguments, } named Reducer);
    functor_macros::impl_functor_for!(enum PipelineStage [
        Match { block, } |
        Insert { block, } |
        Delete { deleted_variables, block, } |
        Put { block, } |
        Update { block, } |
        Select { variables, } |
        Sort { variables, } |
        Offset { offset, } |
        Limit { limit, } |
        Require { variables, } |
        Distinct { } |
        Reduce { reducers, groupby, } | // TODO
    ]);

    macro_rules! encode_functor_impl_exactness {
        ($context:ident, $exactness:ident, $variant:ident $exactVariant:ident $fields:tt ) => {
            match $exactness {
                ConstraintExactness::Exact => encode_functor_impl!($context, $exactVariant $fields),
                ConstraintExactness::Subtypes => encode_functor_impl!($context, $variant $fields),
            }
        };
    }
    impl FunctorEncoded for Constraint {
        fn encode_as_functor(self: &Self, context: &FunctorContext) -> String {
            match self {
                Self::Isa  { instance, r#type, exactness }  => {
                    encode_functor_impl_exactness!(context, exactness, Isa IsaExact { instance, r#type, })
                }
                Self::Has { owner, attribute, exactness }  => {
                    encode_functor_impl_exactness!(context, exactness, Has HasExact { owner, attribute, })
                }
                Self::Links  { relation, player, role, exactness }  => {
                    encode_functor_impl_exactness!(context, exactness, Links LinksExact { relation, player, role, } )
                }
                Self::Sub  { subtype, supertype, exactness }  => {
                    encode_functor_impl_exactness!(context, exactness, Sub SubExact { subtype, supertype, }  )
                }
                Self::Owns  { owner, attribute, exactness }  => {
                    encode_functor_impl_exactness!(context, exactness, Owns OwnsExact { owner, attribute, }  )
                }
                Self::Relates  { relation, role, exactness }  => {
                    encode_functor_impl_exactness!(context, exactness, Relates RelatesExact { relation, role, } )
                }
                Self::Plays  { player, role, exactness }  => {
                    encode_functor_impl_exactness!(context, exactness, Plays PlaysExact { player, role, } )
                }
                Self::FunctionCall  { name, assigned, arguments, }  => {
                    encode_functor_impl!(context, FunctionCall { name, assigned, arguments, })
                }
                Self::Expression  { text, assigned, arguments, }  => {
                    encode_functor_impl!(context, Expression { text, assigned, arguments, })
                }
                Self::Is  { lhs, rhs, }  => {
                    encode_functor_impl!(context, Is { lhs, rhs, })
                }
                Self::Iid  { concept, iid, }  => {
                    let iid_str = format!("0x{}", iid.iter().map(|x| format!("{:02X}", x)).join(""));
                    let iid_ref = &iid_str;
                    encode_functor_impl!(context, Iid { concept, iid_ref, })
                }
                Self::Comparison  { lhs, rhs, comparator, }  => {
                    encode_functor_impl!(context, Comparison { lhs, rhs, comparator, })
                }
                Self::Kind  { kind, r#type, }  => {
                    let kind_str = kind.name().to_owned();
                    let kind_ref = &kind_str;
                    encode_functor_impl!(context, Kind { kind_ref, r#type, })
                }
                Self::Label  { r#type, label, }  => {
                    encode_functor_impl!(context, Label { r#type, label, })
                }
                Self::Value  { attribute_type, value_type, }  => {
                    let value_type_sr = value_type.name().to_owned();
                    let value_type_ref = &value_type_sr;
                    encode_functor_impl!(context, Value { attribute_type, value_type_ref, })
                }
                Self::Or  { branches, }  => {
                    encode_functor_impl!(context, Or { branches, })
                }
                Self::Not  { conjunction, }  => {
                    encode_functor_impl!(context, Not { conjunction, })
                }
                Self::Try  { conjunction, }  => {
                    encode_functor_impl!(context, Try { conjunction, })
                }
            }
        }
    }

    functor_macros::impl_functor_for_impl!(ConstraintVertex => |self, context| {
        match self {
            ConstraintVertex::Variable(id) => { id.encode_as_functor(context) }
            ConstraintVertex::Label(LabelVertex::Resolved(r#type)) => { r#type.label().to_owned().encode_as_functor(context) }
            ConstraintVertex::Label(LabelVertex::Unresolved(label))=> { label.encode_as_functor(context) }
            ConstraintVertex::Value(v) => {
                match v {
                    Value::String(s) => std::format!("\"{}\"", s.to_string()),
                    other => other.to_string(),
                }
            }
        }
    });
    //
    macro_rules! impl_functor_for_multi {
        (|$self:ident, $context:ident| [ $( $type_name:ident => $block:block )* ]) => {
            $ (functor_macros::impl_functor_for_impl!($type_name => |$self, $context| $block); )*
        };
    }
    impl_functor_for_multi!(|self, context| [
        Variable =>  { format!("${}", context.variable_names.get(self).as_ref().map(|v| v.as_str()).unwrap_or("_")) }
        Comparator =>  { format!("{}", self.symbol()) }
        ConjunctionID => { context.conjunctions[self.0 as usize].encode_as_functor(context) }
        Conjunction => { let Conjunction { constraints } = self; constraints.encode_as_functor(context) }
        PipelineStructure => { let pipeline = &self.stages; functor_macros::encode_functor_impl!(self, Pipeline { pipeline, }) }
        FunctionStructure => {
            let FunctionStructure { arguments, returns, body } = self;
            let context = body;
            functor_macros::encode_functor_impl!(context, Function { arguments, returns, body, })
        }
        SortVariable => {
            let Self { order, variable } = self;
            match order {
                SortOrder::Ascending => functor_macros::encode_functor_impl!(context, Asc { variable, }),
                SortOrder::Descending => functor_macros::encode_functor_impl!(context, Desc { variable, }),
            }
        }
    ]);
    //
    functor_macros::impl_functor_for!(enum ReturnOperation [
        Stream { variables, } |
        Single { selector, variables, }  |
        Check { }  |
        Reduce {} |
    ]);

    pub fn encode_query_structure_as_functor(structure: &QueryStructure) -> (String, Vec<String>) {
        let pipeline = &structure.query;
        let query = pipeline.encode_as_functor(pipeline);
        let preamble = structure.preamble.iter().map(|func| func.encode_as_functor(&func.body)).collect();
        (query, preamble)
    }
}
