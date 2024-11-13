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

use chrono::{DateTime, FixedOffset, NaiveDate, NaiveDateTime, TimeZone as ChronoTimeZone};
use chrono_tz::Tz;
use futures::TryFutureExt;
use itertools::Itertools;
use typedb_protocol::{
    concept,
    concept_document::{self, node::leaf::Leaf as LeafProto},
    row_entry::Entry,
    value::{datetime_tz::Timezone as TimezoneProto, Value as ValueProtoInner},
    value_type::ValueType as ValueTypeProto,
    Attribute as AttributeProto, AttributeType as AttributeTypeProto, Concept as ConceptProto,
    ConceptDocument as ConceptDocumentProto, ConceptRow as ConceptRowProto, Entity as EntityProto,
    EntityType as EntityTypeProto, Relation as RelationProto, RelationType as RelationTypeProto,
    RoleType as RoleTypeProto, Value as ValueProto, ValueType as ValueTypeStructProto,
};

use super::{FromProto, TryFromProto};
use crate::{
    answer::concept_document::{Leaf, Node},
    concept::{
        value::{Decimal, TimeZone},
        Attribute, AttributeType, Concept, Entity, EntityType, Kind, Relation, RelationType, RoleType, Value,
        ValueType,
    },
    error::{
        ConnectionError,
        ConnectionError::{
            ListsNotImplemented, MissingResponseField, ValueStructNotImplemented, ValueTimeZoneNameNotRecognised,
            ValueTimeZoneOffsetNotRecognised,
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
            None => Err(MissingResponseField { field: "concept" }.into()),
        }
    }
}

impl TryFromProto<concept_document::Node> for Node {
    fn try_from_proto(proto: concept_document::Node) -> Result<Self> {
        match proto.node {
            Some(concept_document::node::Node::Map(map)) => Ok(Self::Map(HashMap::try_from_proto(map)?)),
            Some(concept_document::node::Node::List(list)) => Ok(Self::List(Vec::try_from_proto(list)?)),
            Some(concept_document::node::Node::Leaf(leaf)) => {
                let result: Result<Option<Leaf>> = Option::try_from_proto(leaf);
                Ok(Self::Leaf(result?))
            }
            None => Err(MissingResponseField { field: "node" }.into()),
        }
    }
}

impl TryFromProto<ConceptDocumentProto> for Option<Node> {
    fn try_from_proto(proto: ConceptDocumentProto) -> Result<Self> {
        let ConceptDocumentProto { root: root_proto } = proto;
        match root_proto {
            None => Ok(None),
            Some(node_proto) => Node::try_from_proto(node_proto).map(Some),
        }
    }
}

impl TryFromProto<concept_document::node::Map> for HashMap<String, Node> {
    fn try_from_proto(proto: concept_document::node::Map) -> Result<Self> {
        let concept_document::node::Map { map } = proto;
        map.into_iter().map(|(var, node_proto)| Node::try_from_proto(node_proto).map(|node| (var, node))).try_collect()
    }
}

impl TryFromProto<concept_document::node::List> for Vec<Node> {
    fn try_from_proto(proto: concept_document::node::List) -> Result<Self> {
        let concept_document::node::List { list } = proto;
        list.into_iter().map(Node::try_from_proto).try_collect()
    }
}

impl TryFromProto<concept_document::node::Leaf> for Option<Leaf> {
    fn try_from_proto(proto: concept_document::node::Leaf) -> Result<Self> {
        match proto.leaf {
            Some(LeafProto::Empty(_)) => Ok(None),
            Some(LeafProto::EntityType(entity_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::EntityType(EntityType::from_proto(entity_type_proto)))))
            }
            Some(LeafProto::RelationType(relation_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::RelationType(RelationType::from_proto(relation_type_proto)))))
            }
            Some(LeafProto::AttributeType(attribute_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::AttributeType(AttributeType::from_proto(attribute_type_proto)))))
            }
            Some(LeafProto::RoleType(role_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::RoleType(RoleType::from_proto(role_type_proto)))))
            }
            Some(LeafProto::ValueType(value_type_proto)) => {
                ValueType::try_from_proto(value_type_proto).map(|value_type| Some(Leaf::ValueType(value_type)))
            }
            Some(LeafProto::Attribute(attribute_proto)) => {
                Attribute::try_from_proto(attribute_proto).map(|attr| Some(Leaf::Concept(Concept::Attribute(attr))))
            }
            Some(LeafProto::Value(value_proto)) => {
                Value::try_from_proto(value_proto).map(|value| Some(Leaf::Concept(Concept::Value(value))))
            }
            Some(LeafProto::Kind(kind_proto)) => Kind::try_from_proto(kind_proto).map(|kind| Some(Leaf::Kind(kind))),
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

impl FromProto<RoleTypeProto> for RoleType {
    fn from_proto(proto: RoleTypeProto) -> Self {
        let RoleTypeProto { label } = proto;
        Self { label }
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

impl TryFromProto<ValueTypeStructProto> for ValueType {
    fn try_from_proto(proto: ValueTypeStructProto) -> Result<Self> {
        let ValueTypeStructProto { value_type: value_type_proto } = proto;
        Ok(ValueType::from_proto(value_type_proto.ok_or(MissingResponseField { field: "value_type" })?))
    }
}

impl TryFromProto<EntityProto> for Entity {
    fn try_from_proto(proto: EntityProto) -> Result<Self> {
        let EntityProto { iid, entity_type } = proto;
        Ok(Self { iid: iid.into(), type_: entity_type.map(EntityType::from_proto) })
    }
}

impl TryFromProto<RelationProto> for Relation {
    fn try_from_proto(proto: RelationProto) -> Result<Self> {
        let RelationProto { iid, relation_type } = proto;
        Ok(Self { iid: iid.into(), type_: relation_type.map(RelationType::from_proto) })
    }
}

impl TryFromProto<AttributeProto> for Attribute {
    fn try_from_proto(proto: AttributeProto) -> Result<Self> {
        let AttributeProto { iid, attribute_type, value } = proto;
        let type_ = attribute_type.map(AttributeType::from_proto);
        Ok(Self {
            iid: iid.into(),
            type_,
            value: Value::try_from_proto(value.ok_or(MissingResponseField { field: "value" })?)?,
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
                let naive_datetime = naive_datetime_from_timestamp(datetime.seconds, datetime.nanos);
                let timezone = datetime_tz
                    .timezone
                    .ok_or(Error::from(MissingResponseField { field: "Value.datetime_tz.timezone" }))?;

                let time_zone = match timezone {
                    TimezoneProto::Named(name) => {
                        let tz = Tz::from_str(&name)
                            .map_err(|_| Error::from(ValueTimeZoneNameNotRecognised { time_zone: name.to_owned() }))?;
                        TimeZone::IANA(tz)
                    }
                    TimezoneProto::Offset(offset_seconds) => {
                        let fixed_offset = if offset_seconds >= 0 {
                            FixedOffset::east_opt(offset_seconds)
                        } else {
                            FixedOffset::west_opt(-offset_seconds)
                        }
                        .ok_or(Error::from(ValueTimeZoneOffsetNotRecognised { offset: offset_seconds }))?;
                        TimeZone::Fixed(fixed_offset)
                    }
                };
                Ok(Self::DatetimeTZ(time_zone.from_utc_datetime(&naive_datetime)))
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

impl TryFromProto<i32> for Kind {
    fn try_from_proto(kind: i32) -> Result<Self> {
        match kind {
            0 => Ok(Self::Entity),
            1 => Ok(Self::Relation),
            2 => Ok(Self::Attribute),
            3 => Ok(Self::Role),
            _ => Err(ConnectionError::UnexpectedKind { kind }.into()),
        }
    }
}

fn naive_datetime_from_timestamp(seconds: i64, nanos: u32) -> NaiveDateTime {
    DateTime::from_timestamp(seconds, nanos).unwrap().naive_utc()
}
