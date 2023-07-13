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

use typedb_protocol::Rule as RuleProto;
use typeql_lang::{
    parse_pattern, parse_variable,
    pattern::{Pattern, Variable},
};

use super::{IntoProto, TryFromProto};
use crate::{common::Result, error::ConnectionError, logic::Rule};

impl TryFromProto<RuleProto> for Rule {
    fn try_from_proto(proto: RuleProto) -> Result<Self> {
        let RuleProto { label: label_proto, when: when_proto, then: then_proto } = proto;
        let when = match parse_pattern(&when_proto)? {
            Pattern::Conjunction(conjunction) => conjunction,
            _ => return Err(ConnectionError::InvalidResponseField("when").into()),
        };
        let then = match parse_variable(&then_proto) {
            Ok(Variable::Thing(thing)) => thing,
            Ok(_) => return Err(ConnectionError::InvalidResponseField("then").into()),
            Err(error) => return Err(error.into()),
        };
        Ok(Self::new(label_proto, when, then))
    }
}

impl IntoProto<RuleProto> for Rule {
    fn into_proto(self) -> RuleProto {
        let Self { label, when, then } = self;
        RuleProto { label, when: when.to_string(), then: then.to_string() }
    }
}
