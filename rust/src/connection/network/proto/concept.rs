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

use std::{collections::HashMap, str::FromStr};

use chrono::{DateTime, NaiveDate, NaiveDateTime, TimeZone};
use chrono_tz::Tz;
use itertools::Itertools;
use typedb_protocol::{
    concept,
    readable_concept_tree::{self, node::readable_concept::ReadableConcept as ReadableConceptProto},
    row_entry::Entry,
    value::{datetime_tz::Timezone as TimezoneProto, Value as ValueProtoInner},
    value_type::ValueType as ValueTypeProto,
    Attribute as AttributeProto, AttributeType as AttributeTypeProto, Concept as ConceptProto,
    ConceptRow as ConceptRowProto, Entity as EntityProto, EntityType as EntityTypeProto,
    ReadableConceptTree as ReadableConceptTreeProto, Relation as RelationProto, RelationType as RelationTypeProto,
    RoleType as RoleTypeProto, Value as ValueProto,
};

use super::{FromProto, TryFromProto};
use crate::{
    answer::concept_tree,
    concept::{
        value::Decimal, Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Value,
        ValueType,
    },
    error::{
        ConnectionError,
        ConnectionError::{
            ListsNotImplemented, MissingResponseField, ValueStructNotImplemented, ValueTimeZoneNameNotRecognised,
            ValueTimeZoneOffsetNotImplemented,
        },
    },
    Error, Result,
};

impl TryFromProto<ConceptRowProto> for Vec<Option<Concept>> {
    fn try_from_proto(proto: ConceptRowProto) -> Result<Self> {
        let mut row = Vec::new();
        for entry in proto.row {
            match entry.entry.ok_or(Error::from(MissingResponseField { field: "ConceptRow.entry" }))? {
                Entry::Empty(_) => row.push(None),
                Entry::Concept(concept_proto) => row.push(Some(Concept::try_from_proto(concept_proto)?)),
                Entry::Value(value_proto) => row.push(Some(Concept::Value(Value::try_from_proto(value_proto)?))),
                Entry::ConceptList(_) | Entry::ValueList(_) => {
                    return Err(ListsNotImplemented.into());
                }
            }
        }
        Ok(row)
    }
}

impl TryFromProto<ConceptProto> for Concept {
    fn try_from_proto(proto: ConceptProto) -> Result<Self> {
        let ConceptProto { concept: concept_proto } = proto;
        match concept_proto {
            Some(concept::Concept::EntityType(entity_type_proto)) => {
                Ok(Self::EntityType(EntityType::from_proto(entity_type_proto)))
            }
            Some(concept::Concept::RelationType(relation_type_proto)) => {
                Ok(Self::RelationType(RelationType::from_proto(relation_type_proto)))
            }
            Some(concept::Concept::AttributeType(attribute_type_proto)) => {
                Ok(Self::AttributeType(AttributeType::from_proto(attribute_type_proto)))
            }

            Some(concept::Concept::RoleType(role_type_proto)) => {
                Ok(Self::RoleType(RoleType::from_proto(role_type_proto)))
            }

            Some(concept::Concept::Entity(entity_proto)) => Entity::try_from_proto(entity_proto).map(Self::Entity),
            Some(concept::Concept::Relation(relation_proto)) => {
                Relation::try_from_proto(relation_proto).map(Self::Relation)
            }
            Some(concept::Concept::Attribute(attribute_proto)) => {
                Attribute::try_from_proto(attribute_proto).map(Self::Attribute)
            }

            // Some(concept::Concept::Value(value_proto)) => Value::try_from_proto(value_proto).map(Self::Value),
            None => Err(ConnectionError::MissingResponseField { field: "concept" }.into()),
        }
    }
}

impl TryFromProto<ReadableConceptTreeProto> for concept_tree::ConceptTree {
    fn try_from_proto(proto: ReadableConceptTreeProto) -> Result<Self> {
        let ReadableConceptTreeProto { root: root_proto } = proto;
        Ok(Self {
            root: HashMap::try_from_proto(root_proto.ok_or(ConnectionError::MissingResponseField { field: "root" })?)?,
        })
    }
}

impl TryFromProto<readable_concept_tree::Node> for concept_tree::Node {
    fn try_from_proto(proto: readable_concept_tree::Node) -> Result<Self> {
        match proto.node {
            Some(readable_concept_tree::node::Node::Map(map)) => Ok(Self::Map(HashMap::try_from_proto(map)?)),
            Some(readable_concept_tree::node::Node::List(list)) => Ok(Self::List(Vec::try_from_proto(list)?)),
            Some(readable_concept_tree::node::Node::ReadableConcept(leaf)) => {
                let result: Result<Option<Concept>> = Option::try_from_proto(leaf);
                Ok(Self::Leaf(result?))
            }
            None => Err(ConnectionError::MissingResponseField { field: "node" }.into()),
        }
    }
}

impl TryFromProto<readable_concept_tree::node::Map> for HashMap<String, concept_tree::Node> {
    fn try_from_proto(proto: readable_concept_tree::node::Map) -> Result<Self> {
        let readable_concept_tree::node::Map { map } = proto;
        map.into_iter()
            .map(|(var, node_proto)| concept_tree::Node::try_from_proto(node_proto).map(|node| (var, node)))
            .try_collect()
    }
}

impl TryFromProto<readable_concept_tree::node::List> for Vec<concept_tree::Node> {
    fn try_from_proto(proto: readable_concept_tree::node::List) -> Result<Self> {
        let readable_concept_tree::node::List { list } = proto;
        list.into_iter().map(concept_tree::Node::try_from_proto).try_collect()
    }
}

impl TryFromProto<readable_concept_tree::node::ReadableConcept> for Option<Concept> {
    fn try_from_proto(proto: readable_concept_tree::node::ReadableConcept) -> Result<Self> {
        match proto.readable_concept {
            Some(ReadableConceptProto::EntityType(entity_type_proto)) => {
                Ok(Some(Concept::EntityType(EntityType::from_proto(entity_type_proto))))
            }
            Some(ReadableConceptProto::RelationType(relation_type_proto)) => {
                Ok(Some(Concept::RelationType(RelationType::from_proto(relation_type_proto))))
            }
            Some(ReadableConceptProto::AttributeType(attribute_type_proto)) => {
                Ok(Some(Concept::AttributeType(AttributeType::from_proto(attribute_type_proto))))
            }
            Some(ReadableConceptProto::RoleType(role_type_proto)) => {
                Ok(Some(Concept::RoleType(RoleType::from_proto(role_type_proto))))
            }
            Some(ReadableConceptProto::Attribute(attribute_proto)) => {
                Attribute::try_from_proto(attribute_proto).map(|attr| Some(Concept::Attribute(attr)))
            }
            Some(ReadableConceptProto::Value(value_proto)) => {
                Value::try_from_proto(value_proto).map(|value| Some(Concept::Value(value)))
            }
            None => Ok(None),
        }
    }
}

impl FromProto<EntityTypeProto> for EntityType {
    fn from_proto(proto: EntityTypeProto) -> Self {
        let EntityTypeProto { label } = proto;
        Self { label }
    }
}

impl FromProto<RelationTypeProto> for RelationType {
    fn from_proto(proto: RelationTypeProto) -> Self {
        let RelationTypeProto { label } = proto;
        Self { label }
    }
}

impl FromProto<AttributeTypeProto> for AttributeType {
    fn from_proto(proto: AttributeTypeProto) -> Self {
        let AttributeTypeProto { label, value_type } = proto;
        let value_type = match value_type {
            None => None,
            Some(proto) => Some(ValueType::from_proto(proto.value_type.unwrap())),
        };
        Self { label, value_type }
    }
}

impl FromProto<ValueTypeProto> for ValueType {
    fn from_proto(proto: ValueTypeProto) -> Self {
        match proto {
            ValueTypeProto::Boolean(_) => Self::Boolean,
            ValueTypeProto::Long(_) => Self::Long,
            ValueTypeProto::Double(_) => Self::Double,
            ValueTypeProto::String(_) => Self::String,
            ValueTypeProto::Decimal(_) => Self::Decimal,
            ValueTypeProto::Date(_) => Self::Date,
            ValueTypeProto::Datetime(_) => Self::Datetime,
            ValueTypeProto::DatetimeTz(_) => Self::DatetimeTZ,
            ValueTypeProto::Duration(_) => Self::Duration,
            ValueTypeProto::Struct(typedb_protocol::value_type::Struct { name }) => Self::Struct(name),
        }
    }
}

impl FromProto<RoleTypeProto> for RoleType {
    fn from_proto(proto: RoleTypeProto) -> Self {
        let RoleTypeProto { label } = proto;
        Self { label }
    }
}

impl TryFromProto<EntityProto> for Entity {
    fn try_from_proto(proto: EntityProto) -> Result<Self> {
        let EntityProto { iid, entity_type } = proto;
        Ok(Self { iid: iid.into(), type_: entity_type.map(|type_| EntityType::from_proto(type_)) })
    }
}

impl TryFromProto<RelationProto> for Relation {
    fn try_from_proto(proto: RelationProto) -> Result<Self> {
        let RelationProto { iid, relation_type } = proto;
        Ok(Self { iid: iid.into(), type_: relation_type.map(|type_| RelationType::from_proto(type_)) })
    }
}

impl TryFromProto<AttributeProto> for Attribute {
    fn try_from_proto(proto: AttributeProto) -> Result<Self> {
        let AttributeProto { iid, attribute_type, value } = proto;
        let type_ = match attribute_type {
            None => None,
            Some(attribute_type) => Some(AttributeType::from_proto(attribute_type)),
        };
        Ok(Self {
            iid: iid.into(),
            type_,
            value: Value::try_from_proto(value.ok_or(ConnectionError::MissingResponseField { field: "value" })?)?,
        })
    }
}

impl TryFromProto<ValueProto> for Value {
    fn try_from_proto(proto: ValueProto) -> Result<Self> {
        match proto.value {
            Some(ValueProtoInner::Boolean(boolean)) => Ok(Self::Boolean(boolean)),
            Some(ValueProtoInner::Long(long)) => Ok(Self::Long(long)),
            Some(ValueProtoInner::Double(double)) => Ok(Self::Double(double)),
            Some(ValueProtoInner::String(string)) => Ok(Self::String(string)),
            Some(ValueProtoInner::Decimal(decimal)) => {
                Ok(Self::Decimal(Decimal::new(decimal.integer, decimal.fractional)))
            }
            Some(ValueProtoInner::Date(date)) => {
                Ok(Self::Date(NaiveDate::from_num_days_from_ce_opt(date.num_days_since_ce).unwrap()))
            }
            Some(ValueProtoInner::Datetime(datetime)) => {
                Ok(Self::Datetime(naive_datetime_from_timestamp(datetime.seconds, datetime.nanos)))
            }
            Some(ValueProtoInner::DatetimeTz(datetime_tz)) => {
                let datetime = datetime_tz
                    .datetime
                    .ok_or(Error::from(MissingResponseField { field: "Value.datetime_tz.datetime" }))?;
                let timezone = datetime_tz
                    .timezone
                    .ok_or(Error::from(MissingResponseField { field: "Value.datetime_tz.timezone" }))?;

                let tz = match timezone {
                    TimezoneProto::Named(name) => Tz::from_str(&name)
                        .map_err(|_| Error::from(ValueTimeZoneNameNotRecognised { time_zone: name.to_owned() }))?,
                    TimezoneProto::Offset(offset) => {
                        Err(Error::from(ValueTimeZoneOffsetNotImplemented { offset }))?
                        // TODO: not implemented yet
                    }
                };
                Ok(Self::DatetimeTZ(
                    tz.from_utc_datetime(&naive_datetime_from_timestamp(datetime.seconds, datetime.nanos)),
                ))
            }
            Some(ValueProtoInner::Duration(duration)) => {
                Ok(Self::Duration(crate::concept::value::Duration::new(duration.months, duration.days, duration.nanos)))
            }
            Some(ValueProtoInner::Struct(_struct)) => {
                Err(ValueStructNotImplemented.into())
                // TODO: not implemented yet
            }
            None => Err(MissingResponseField { field: "Value.value" }.into()),
        }
    }
}

fn naive_datetime_from_timestamp(seconds: i64, nanos: u32) -> NaiveDateTime {
    DateTime::from_timestamp(seconds, nanos).unwrap().naive_utc()
}
