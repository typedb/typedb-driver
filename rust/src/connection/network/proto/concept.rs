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

use std::collections::HashMap;

use itertools::Itertools;
use typedb_protocol::{
    Attribute as AttributeProto,
    AttributeType as AttributeTypeProto,
    concept,
    Concept as ConceptProto,
    Entity as EntityProto, EntityType as EntityTypeProto, readable_concept_tree::{self, node::readable_concept::ReadableConcept as ReadableConceptProto},
    ReadableConceptTree as ReadableConceptTreeProto,
    Relation as RelationProto
    , RelationType as RelationTypeProto, RoleType as RoleTypeProto,
    thing, Thing as ThingProto, Value as ValueProto,
    value::Value as ValueProtoInner, value_type::ValueType as ValueTypeProto,
};

use crate::{
    answer::readable_concept,
    concept::{
        Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType,
        ScopedLabel, Thing, Value, ValueType,
    },
    error::{ConnectionError, InternalError},
    Result,
};

use super::{FromProto, TryFromProto};

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
                AttributeType::try_from_proto(attribute_type_proto).map(Self::AttributeType)
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

impl TryFromProto<ReadableConceptTreeProto> for readable_concept::Tree {
    fn try_from_proto(proto: ReadableConceptTreeProto) -> Result<Self> {
        let ReadableConceptTreeProto { root: root_proto } = proto;
        Ok(Self {
            root: HashMap::try_from_proto(root_proto.ok_or(ConnectionError::MissingResponseField { field: "root" })?)?,
        })
    }
}

impl TryFromProto<readable_concept_tree::Node> for readable_concept::Node {
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

impl TryFromProto<readable_concept_tree::node::Map> for HashMap<String, readable_concept::Node> {
    fn try_from_proto(proto: readable_concept_tree::node::Map) -> Result<Self> {
        let readable_concept_tree::node::Map { map } = proto;
        map.into_iter()
            .map(|(var, node_proto)| readable_concept::Node::try_from_proto(node_proto).map(|node| (var, node)))
            .try_collect()
    }
}

impl TryFromProto<readable_concept_tree::node::List> for Vec<readable_concept::Node> {
    fn try_from_proto(proto: readable_concept_tree::node::List) -> Result<Self> {
        let readable_concept_tree::node::List { list } = proto;
        list.into_iter().map(readable_concept::Node::try_from_proto).try_collect()
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
                AttributeType::try_from_proto(attribute_type_proto)
                    .map(|attr_type| Some(Concept::AttributeType(attr_type)))
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
        let EntityTypeProto { label, annotations } = proto;
        Self { label }
    }
}

impl FromProto<RelationTypeProto> for RelationType {
    fn from_proto(proto: RelationTypeProto) -> Self {
        let RelationTypeProto { label, annotations } = proto;
        Self { label }
    }
}

impl TryFromProto<AttributeTypeProto> for AttributeType {
    fn try_from_proto(proto: AttributeTypeProto) -> Result<Self> {
        let AttributeTypeProto { label, value_type, annotations } = proto;
        let value_type = match value_type {
            None => None,
            Some(proto) => Some(ValueType::try_from_proto(proto.value_type.unwrap())?)
        };
        Ok(Self { label, value_type })
    }
}

impl TryFromProto<ValueTypeProto> for ValueType {
    fn try_from_proto(proto: ValueTypeProto) -> Result<Self> {
        todo!()
        // match ValueTypeProto::from_i32(proto) {
        // Some(ValueTypeProto::Boolean) => Ok(Self::Boolean),
        // Some(ValueTypeProto::Long) => Ok(Self::Long),
        // Some(ValueTypeProto::Double) => Ok(Self::Double),
        // Some(ValueTypeProto::String) => Ok(Self::String),
        // Some(ValueTypeProto::Datetime) => Ok(Self::DateTime),
        // None => Err(InternalError::EnumOutOfBounds { value: proto, enum_name: "ValueType" }.into()),
        // }
    }
}

impl FromProto<RoleTypeProto> for RoleType {
    fn from_proto(proto: RoleTypeProto) -> Self {
        let RoleTypeProto { scope, name, annotations } = proto;
        Self { label: ScopedLabel { scope, name } }
    }
}

impl TryFromProto<ThingProto> for Thing {
    fn try_from_proto(proto: ThingProto) -> Result<Self> {
        match proto.thing {
            Some(thing::Thing::Entity(entity_proto)) => Entity::try_from_proto(entity_proto).map(Self::Entity),
            Some(thing::Thing::Relation(relation_proto)) => {
                Relation::try_from_proto(relation_proto).map(Self::Relation)
            }
            Some(thing::Thing::Attribute(attribute_proto)) => {
                Attribute::try_from_proto(attribute_proto).map(Self::Attribute)
            }
            None => Err(ConnectionError::MissingResponseField { field: "thing" }.into()),
        }
    }
}

impl TryFromProto<EntityProto> for Entity {
    fn try_from_proto(proto: EntityProto) -> Result<Self> {
        let EntityProto { iid, entity_type, } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: entity_type.map(|type_| EntityType::from_proto(type_)),
        })
    }
}

impl TryFromProto<RelationProto> for Relation {
    fn try_from_proto(proto: RelationProto) -> Result<Self> {
        let RelationProto { iid, relation_type, } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: relation_type.map(|type_| RelationType::from_proto(type_)),
        })
    }
}

impl TryFromProto<AttributeProto> for Attribute {
    fn try_from_proto(proto: AttributeProto) -> Result<Self> {
        let AttributeProto { iid, attribute_type, value, } = proto;
        let type_ = match attribute_type {
            None => None,
            Some(attribute_type) => Some(AttributeType::try_from_proto(attribute_type)?),
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
            Some(ValueProtoInner::Decimal(_)) => {
                todo!()
            }
            Some(ValueProtoInner::Date(_)) => {
                todo!()
            }
            Some(ValueProtoInner::Datetime(_)) => {
                todo!()
            }
            Some(ValueProtoInner::DatetimeTz(_)) => {
                todo!()
            }
            Some(ValueProtoInner::Duration(_)) => {
                todo!()
            }
            Some(ValueProtoInner::Struct(_)) => {
                todo!()
            }
            None => Err(ConnectionError::MissingResponseField { field: "value" }.into()),
        }
    }
}

