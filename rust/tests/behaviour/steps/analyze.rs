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

use cucumber::gherkin::Step;
use itertools::Itertools;
use macro_rules_attribute::apply;
use typedb_driver::{analyze::AnalyzedQuery, Transaction, Result as TypeDBResult};
use crate::{Context, params, generic_step};
use crate::analyze::functor_encoding::{encode_fetch_annotations_as_functor, encode_query_annotations_as_functor, encode_query_structure_as_functor};
// use functor_encoding::{encode_fetch_annotations_as_functor, encode_query_annotations_as_functor, encode_query_structure_as_functor};

pub(crate) async fn run_analyze_query(
    transaction: &Transaction,
    query: impl AsRef<str>,
) -> TypeDBResult<AnalyzedQuery> {
    transaction.analyze_query(query).await
}

#[apply(generic_step)]
#[step(expr = r"get answers of typeql analyze query")]
pub async fn get_answers_of_typeql_analyze_query(context: &mut Context, step: &Step) {
    context.cleanup_answers().await;
    context.set_analyzed(run_analyze_query(context.transaction(), step.docstring().unwrap()).await).unwrap();
}

#[apply(generic_step)]
#[step(expr = r"typeql analyze query{may_error}")]
async fn typeql_analyze_query_may_error(context: &mut Context, may_error: params::MayError, step: &Step) {
    context.cleanup_answers().await;
    let result = run_analyze_query(context.transaction(), step.docstring().unwrap()).await;
    may_error.check(result);
}

#[apply(generic_step)]
#[step(expr = r"analyzed query pipeline structure is:")]
pub async fn analyzed_query_pipeline_structure_is(context: &mut Context, step: &Step) {
    let expected_functor = step.docstring().unwrap();
    let analyzed = context.get_analyzed().unwrap();
    let (actual_functor, _preamble) = encode_query_structure_as_functor(&analyzed);
    assert_eq!(normalize_functor_for_compare(&actual_functor), normalize_functor_for_compare(expected_functor));
}

#[apply(generic_step)]
#[step(expr = r"analyzed query preamble contains:")]
async fn analyzed_query_preamble_contains(context: &mut Context, step: &Step) {
    let expected_functor = step.docstring().unwrap();
    let analyzed = context.get_analyzed().unwrap();
    let (_pipeline, preamble_functors) = encode_query_structure_as_functor(&analyzed);

    assert!(
        preamble_functors.iter().any(|actual_functor| {
            normalize_functor_for_compare(actual_functor) == normalize_functor_for_compare(expected_functor)
        }),
        "Looking for\n\t{}\nin any of:\n\t{}",
        normalize_functor_for_compare(expected_functor),
        preamble_functors.iter().map(|s| normalize_functor_for_compare(s)).join("\n\t")
    );
}

#[apply(generic_step)]
#[step(expr = r"analyzed query pipeline annotations are:")]
async fn analyzed_query_annotations_is(context: &mut Context, step: &Step) {
    let expected_functor = step.docstring().unwrap();
    let analyzed = context.get_analyzed().unwrap();
    let (actual_functor, _preamble) = encode_query_annotations_as_functor(&analyzed);
    assert_eq!(normalize_functor_for_compare(&actual_functor), normalize_functor_for_compare(expected_functor));
}

#[apply(generic_step)]
#[step(expr = r"analyzed preamble annotations contains:")]
async fn analyzed_preamble_annotations_contains(context: &mut Context, step: &Step) {
    let expected_functor = step.docstring().unwrap();
    let analyzed = context.get_analyzed().unwrap();
    let (_pipeline, preamble_functors) = encode_query_annotations_as_functor(&analyzed);

    assert!(
        preamble_functors.iter().any(|actual_functor| {
            normalize_functor_for_compare(actual_functor) == normalize_functor_for_compare(expected_functor)
        }),
        "Looking for\n\t{}\nin any of:\n\t{}",
        normalize_functor_for_compare(expected_functor),
        preamble_functors.iter().map(|s| normalize_functor_for_compare(s)).join("\n\t")
    );
}

#[apply(generic_step)]
#[step(expr = r"analyzed fetch annotations are:")]
async fn analyzed_fetch_annotations_are(context: &mut Context, step: &Step) {
    let expected_functor = step.docstring().unwrap();
    let analyzed = context.get_analyzed().unwrap();
    let actual_functor = encode_fetch_annotations_as_functor(&analyzed);

    assert_eq!(
        normalize_functor_for_compare(&actual_functor),
        normalize_functor_for_compare(expected_functor)
    );
}

fn normalize_functor_for_compare(functor: &String) -> String {
    let mut normalized = functor.to_lowercase();
    normalized.retain(|c| !c.is_whitespace());
    normalized
}



#[cfg(debug_assertions)]
pub mod functor_encoding {
    use std::collections::HashMap;
    use itertools::Itertools;

    struct FunctorContext<'a> {
        structure: &'a PipelineStructure,
        annotations: &'a PipelineAnnotations,
    }

    pub trait FunctorEncoded {
        fn encode_as_functor<'a>(&self, context: &FunctorContext<'a>) -> String;
    }

    pub mod functor_macros {
        macro_rules! encode_args {
            ($context:ident, { $( $arg:ident, )* } )   => {
                {
                    let arr: Vec<&dyn FunctorEncoded> = vec![ $($arg,)* ];
                    arr.into_iter().map(|s| s.encode_as_functor($context)).join(", ")
                }
            };
            ($context:ident, ( $( $arg:ident, )* ) ) => {
                functor_macros::encode_args!($context, { $( $arg, )* } )
            };
        }
        macro_rules! encode_functor_impl {
            ($context:ident, $func:ident $args:tt) => {
                std::format!("{}({})", std::stringify!($func), functor_macros::encode_args!($context, $args))
            };
        }

        macro_rules! add_ignored_fields {
            ($qualified:path : { $( $arg:ident, )* }) => { $qualified { $( $arg, )* .. } };
            ($qualified:path : ($( $arg:ident, )*)) => { $qualified ( $( $arg, )* .. ) };
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
                    $( functor_macros::add_ignored_fields!($qualified : $fields) => {
                        functor_macros::encode_functor_impl!($context, $func $fields)
                    })*
                }
            };
        }

        macro_rules! impl_functor_for_impl {
            ($which:ident => |$self:ident, $context:ident| $block:block) => {
                impl FunctorEncoded for $which {
                    fn encode_as_functor<'a>($self: &Self, $context: &FunctorContext<'a>) -> String {
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

    impl_functor_for!(primitive String);
    impl_functor_for!(primitive u64);

    impl<K: FunctorEncoded, V: FunctorEncoded> FunctorEncoded for HashMap<K, V> {
        fn encode_as_functor<'a>(&self, context: &FunctorContext<'a>) -> String {
            std::format!("{{ {} }}", self.iter().map(|(k, v)| {
                std::format!("{}: {}", k.encode_as_functor(context), v.encode_as_functor(context))
            }).sorted_by(|a,b| a.cmp(b)).join(", "))
        }
    }

    impl<T: FunctorEncoded> FunctorEncoded for Vec<T> {
        fn encode_as_functor<'a>(&self, context: &FunctorContext<'a>) -> String {
            std::format!("[{}]", self.iter().map(|v| v.encode_as_functor(context)).join(", "))
        }
    }

    impl<T: FunctorEncoded> FunctorEncoded for Option<T> {
        fn encode_as_functor<'a>(&self, context: &FunctorContext<'a>) -> String {
            self.as_ref().map(|inner| inner.encode_as_functor(context)).unwrap_or("<NONE>".to_owned())
        }
    }

    use typedb_driver::analyze::{AnalyzedQuery, conjunction::{Constraint, Conjunction, ConjunctionID, ConstraintVertex, Reducer, Variable}, FunctionStructure, pipeline::{PipelineStructure, PipelineStage, SortVariable, SortOrder, ReduceAssign}, ReturnOperation};
    use typedb_driver::analyze::annotations::{FetchAnnotations, FunctionAnnotations, FunctionReturnAnnotations, PipelineAnnotations, VariableAnnotations};
    use typedb_driver::analyze::conjunction::{Comparator, ConstraintExactness, NamedRole};
    use typedb_driver::concept::type_::Type;
    use typedb_driver::concept::{Value, ValueType};
    use functor_macros::encode_functor_impl;
    use crate::analyze::functor_encoding::functor_macros::{impl_functor_for, impl_functor_for_impl};

    impl_functor_for!(struct ReduceAssign { assigned, reducer,  } named ReduceAssign);
    impl_functor_for!(struct Reducer { reducer, arguments, } named Reducer);
    impl_functor_for!(enum PipelineStage [
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
        fn encode_as_functor<'a>(self: &Self, context: &FunctorContext<'a>) -> String {
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

    impl_functor_for_impl!(ConstraintVertex => |self, context| {
        match self {
            ConstraintVertex::Variable(id) => { id.encode_as_functor(context) }
            ConstraintVertex::Label(type_) => { type_.encode_as_functor(context) }
            ConstraintVertex::Unresolved(label) => { label.encode_as_functor(context) }
            ConstraintVertex::NamedRole(NamedRole { name,.. }) => { name.encode_as_functor(context) }
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
            $ (impl_functor_for_impl!($type_name => |$self, $context| $block); )*
        };
    }
    impl_functor_for_multi!(|self, context| [
        Variable =>  { format!("${}", context.structure.variable_names.get(self).as_ref().map(|v| v.as_str()).unwrap_or("_")) }
        Type => { self.label().to_owned().encode_as_functor(context) }
        Comparator =>  { format!("{}", self.symbol()) }
        ConjunctionID => { context.structure.conjunctions[self.0].encode_as_functor(context) }
        Conjunction => { let Conjunction { constraints } = self; constraints.encode_as_functor(context) }
        PipelineStructure => { let pipeline = &self.stages; encode_functor_impl!(context, Pipeline { pipeline, }) }
        FunctionStructure => {
            let FunctionStructure { arguments, returns, body } = self;
            encode_functor_impl!(context, Function { arguments, returns, body, })
        }
        ValueType => {
            match self {
                ValueType::Struct(name) => encode_functor_impl!(context, Struct { name, }),
                other => other.name().to_owned().encode_as_functor(context),
            }
        }
        SortVariable => {
            let Self { order, variable } = self;
            match order {
                SortOrder::Ascending => encode_functor_impl!(context, Asc { variable, }),
                SortOrder::Descending => encode_functor_impl!(context, Desc { variable, }),
            }
        }
    ]);
    //
    impl_functor_for!(enum ReturnOperation [
        Stream { variables, } |
        Single { selector, variables, }  |
        Check { }  |
        Reduce {} |
    ]);

    pub fn encode_query_structure_as_functor(analyzed: &AnalyzedQuery) -> (String, Vec<String>) {
        let context = FunctorContext { structure: &analyzed.structure.query, annotations: &analyzed.annotations.query };
        let query = analyzed.structure.query.encode_as_functor(&context);
        let preamble = analyzed.structure.preamble.iter().zip(analyzed.annotations.preamble.iter()).map(|(func, anno)| {
            let context = FunctorContext { structure: &func.body, annotations: &anno.body };
            func.encode_as_functor(&context)
        }).collect();
        (query, preamble)
    }

    // annotations

    pub fn encode_query_annotations_as_functor(analyzed: &AnalyzedQuery) -> (String, Vec<String>) {
        let context = FunctorContext { structure: &analyzed.structure.query, annotations: &analyzed.annotations.query };
        let query = analyzed.annotations.query.encode_as_functor(&context);
        let preamble = analyzed.annotations.preamble.iter().zip(analyzed.structure.preamble.iter())
            .map(|(annotations, structure)| {
                let context = FunctorContext { structure: &structure.body, annotations: &annotations.body };
                annotations.encode_as_functor(&context)
            }).collect();
        (query, preamble)
    }

    impl FunctorEncoded for PipelineAnnotations {
        fn encode_as_functor<'a>(&self, context: &FunctorContext<'a>) -> String {
            let encoded_stages = context.structure.stages.iter().map(|stage| {
                match stage {
                    PipelineStage::Match { block } => {
                        let block = &BlockAnnotationToEncode(block.0);
                        encode_functor_impl!(context, Match { block, })
                    },
                    PipelineStage::Insert { block } => {
                        let block = &BlockAnnotationToEncode(block.0);
                        encode_functor_impl!(context, Insert { block, })
                    }
                    PipelineStage::Delete { block, .. } => {
                        let block = &BlockAnnotationToEncode(block.0);
                        encode_functor_impl!(context, Delete { block, })
                    }
                    PipelineStage::Put { block }  => {
                        let block = &BlockAnnotationToEncode(block.0);
                        encode_functor_impl!(context, Put { block, })
                    },
                    PipelineStage::Update { block } =>  {
                        let block = &BlockAnnotationToEncode(block.0);
                        encode_functor_impl!(context, Update { block, })
                    }
                    PipelineStage::Select { .. } => encode_functor_impl!(context, Select { }),
                    PipelineStage::Sort { .. } => encode_functor_impl!(context, Sort { }),
                    PipelineStage::Offset { .. }  => encode_functor_impl!(context, Offset { }),
                    PipelineStage::Limit { .. } => encode_functor_impl!(context, Limit { }),
                    PipelineStage::Require { .. } => encode_functor_impl!(context, Require { }),
                    PipelineStage::Distinct => encode_functor_impl!(context, Select { }),
                    PipelineStage::Reduce { .. } => encode_functor_impl!(context, Reduce { }),
                }
            }).collect::<Vec<_>>();
            let encoded_stages_ref = &encoded_stages;
            encode_functor_impl!(context, Pipeline { encoded_stages_ref, })// Not ideal to encode the elements again
        }
    }

    impl_functor_for!(struct FunctionAnnotations { arguments, returns, body, } named Function);
    impl_functor_for!(enum FunctionReturnAnnotations [ Single(annotations,) | Stream(annotations,) | ]);

    #[derive(Debug, Clone, Copy)]
    struct TrunkAnnotationToEncode(usize);

    #[derive(Debug, Clone, Copy)]
    struct BlockAnnotationToEncode(usize);
    impl From<ConjunctionID> for BlockAnnotationToEncode {
        fn from(value: ConjunctionID) -> Self {
            Self(value.0)
        }
    }

    enum SubBlockAnnotation {
        Trunk { conjunction: TrunkAnnotationToEncode },
        Or { branches: Vec<BlockAnnotationToEncode> },
        Not { conjunction: BlockAnnotationToEncode },
        Try { conjunction: BlockAnnotationToEncode },
    }

    impl FunctorEncoded for BlockAnnotationToEncode {
        fn encode_as_functor<'a>(&self, context: &FunctorContext<'a>) -> String {
            let mut elements = vec![SubBlockAnnotation::Trunk { conjunction: TrunkAnnotationToEncode(self.0) }];
            elements.extend(context.structure.conjunctions[self.0].constraints.iter()
                .filter_map(|c| match c {
                    Constraint::Or { branches } => {
                        let branches = branches.iter().copied().map_into().collect();
                        Some(SubBlockAnnotation::Or { branches })
                    },
                    Constraint::Not { conjunction } => {
                        Some(SubBlockAnnotation::Not { conjunction: (*conjunction).into() })
                    },
                    Constraint::Try { conjunction } => {
                        Some(SubBlockAnnotation::Try { conjunction: (*conjunction).into() })
                    },
                    _ => None,
                }));
            elements.encode_as_functor(context)
        }
    }

    impl_functor_for!(enum SubBlockAnnotation [ Trunk { conjunction, } | Or { branches, } | Not { conjunction, } | Try { conjunction, } | ]);
    impl_functor_for!(enum VariableAnnotations [ Thing (annotations,) | Type (annotations,) | Value (value_types,) | ]);
    impl_functor_for_multi!(|self, context| [
        TrunkAnnotationToEncode => {
            context.annotations.conjunction_annotations[self.0].variable_annotations.encode_as_functor(context)
        }
    ]);

    // Fetch
    pub fn encode_fetch_annotations_as_functor(analyzed: &AnalyzedQuery) -> String {
        let context = FunctorContext {
            structure: &analyzed.structure.query,
            annotations: &analyzed.annotations.query
        };
        analyzed.annotations.fetch.encode_as_functor(&context)
    }

    impl FunctorEncoded for FetchAnnotations {
        fn encode_as_functor<'a>(&self, context: &FunctorContext<'a>) -> String {
            match self {
                FetchAnnotations::Leaf(value_types) => {
                    value_types.encode_as_functor(context)
                }
                FetchAnnotations::Object(possible_fields) => {
                    possible_fields.encode_as_functor(context)
                }
                FetchAnnotations::List(elements) => {
                    let elements_as_ref = elements.as_ref();
                    encode_functor_impl!(context, List { elements_as_ref, })
                }
            }
        }
    }
}

