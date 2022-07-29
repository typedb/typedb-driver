/*
 * Copyright (C) 2021 Vaticle
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

#![allow(dead_code)]
#![allow(unused)]

use std::fmt::{Debug, Formatter};
use typedb_protocol::concept::{Concept_oneof_concept, Type_Encoding};
use crate::common::error::MESSAGES;
use crate::common::Result;
use crate::transaction::Transaction;

pub trait AsConcept {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept>;
}

pub trait AsType {
    fn as_type(self: Box<Self>) -> Box<dyn Type>;
}

pub trait AsThing: AsConcept {
    fn as_thing(self: Box<Self>) -> Box<dyn Thing>;
}

pub trait AsEntity: AsThing {
    fn as_entity(self: Box<Self>) -> Box<dyn Entity>;
}

pub trait Concept: AsEntity + Debug {
    fn as_thing_type(&self) -> Result<Box<dyn ThingType>> { todo!() }

    fn as_entity_type(&self) -> Result<Box<dyn EntityType>> { todo!() }

    fn is_entity_type(&self) -> bool { false }

    fn is_relation_type(&self) -> bool { false }

    fn is_thing_type(&self) -> bool { false }

    fn is_entity(&self) -> bool { false }
}

impl dyn Concept {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Concept) -> Result<Box<dyn Concept>> {
        let concept = proto.concept.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["concept"]))?;
        match concept {
            Concept_oneof_concept::thing(thing) => { Ok(<dyn Thing>::from_proto(thing).as_concept()) }
            // TODO: throws "trait upcasting is experimental"; see #![feature(trait_upcasting)
            // Concept_oneof_concept::field_type(type_concept) => { Ok(<dyn Type>::from_proto(type_concept)) }
            Concept_oneof_concept::field_type(type_concept) => { Ok(<dyn Type>::from_proto(type_concept).as_concept()) }
        }
    }
}

// pub trait RemoteConcept: Concept {
//     fn transaction(&self) -> &Transaction;
//
//     fn is_deleted(&self) -> Result<bool> {
//         todo!()
//     }
// }

pub trait Type: Concept {
    fn label(&self) -> &Label;
}

impl dyn Type {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Type) -> Box<dyn Type> {
        match proto.encoding {
            Type_Encoding::THING_TYPE => Box::new(ThingTypeImpl { label: Label { scope: None, name: proto.label } }),
            Type_Encoding::ENTITY_TYPE => <dyn EntityType>::from_proto(proto),
            Type_Encoding::RELATION_TYPE => { todo!() }
            Type_Encoding::ATTRIBUTE_TYPE => { todo!() }
            Type_Encoding::ROLE_TYPE => { todo!() }
        }
    }
}

// pub trait RemoteType: Type + RemoteConcept {
//     fn get_supertype(&self) -> Result<Option<Box<dyn Type>>> {
//         todo!()
//     }
// }

pub trait ThingType: Type {
    fn is_thing_type(&self) -> bool {
        true
    }

    fn get_instances(&self) -> Result<Vec<Box<dyn Thing>>> {
        todo!()
    }
}

// pub trait RemoteThingType: ThingType + RemoteType {
//     fn get_supertype(&self) -> Result<Option<Box<dyn ThingType>>> {
//         Ok(match RemoteType::get_supertype(self)? {
//             Some(supertype) => Some(supertype.as_thing_type()?),
//             None => None
//         })
//     }
//
//     fn get_plays(&self) -> Result<Vec<Box<dyn RoleType>>> {
//         todo!()
//     }
// }

pub trait EntityType: ThingType {
    fn is_entity_type(&self) -> bool {
        true
    }
}

impl dyn EntityType {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Type) -> Box<EntityTypeImpl> {
        Box::new(EntityTypeImpl { label: Label { scope: None, name: proto.label } })
    }
}

// pub trait RemoteEntityType: EntityType + RemoteThingType {
//     fn get_supertype(&self) -> Result<Option<Box<dyn EntityType>>> {
//         Ok(match RemoteType::get_supertype(self)? {
//             Some(supertype) => Some(supertype.as_entity_type()?),
//             None => None
//         })
//     }
//
//     fn get_instances(&self) -> Result<Vec<Box<dyn Entity>>> {
//         todo!()
//     }
// }

pub trait RelationType: ThingType {
    fn is_relation_type(&self) -> bool {
        true
    }

    fn get_instances(&self) -> Result<Vec<Box<dyn Relation>>>;
}

// pub trait RemoteRelationType: RelationType + RemoteThingType {
//     fn get_supertype(&self) -> Result<Option<Box<dyn RelationType>>>;
//
//     fn get_relates(&self) -> Result<Vec<Box<dyn RoleType>>> {
//         todo!()
//     }
// }

pub trait RoleType: Type {}

// pub trait RemoteRoleType: RoleType + RemoteType {}

pub trait Thing: Concept {
    fn iid(&self) -> String {
        todo!()
    }

    fn get_type(&self) -> Box<dyn ThingType>;
}

impl dyn Thing {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Thing) -> Box<dyn Thing> {
        match proto.get_field_type().encoding {
            Type_Encoding::ENTITY_TYPE => Box::new(EntityImpl { iid: iid_from_bytes(proto.get_iid()), type_: *<dyn EntityType>::from_proto(proto.get_field_type().clone()) }),
            Type_Encoding::RELATION_TYPE => { todo!() }
            Type_Encoding::ATTRIBUTE_TYPE => { todo!() }
            _ => { todo!() }
        }
    }
}

fn iid_from_bytes(bytes: &[u8]) -> String {
    format!("{:02x?}", bytes)
    // String::from(std::str::from_utf8(bytes).unwrap())
    // Uuid::from_slice(bytes).unwrap().to_string()
}

// pub trait RemoteThing: Thing + RemoteConcept {
//     fn get_playing(&self) -> Result<Vec<Box<dyn RoleType>>> {
//         todo!()
//     }
// }

pub trait Entity: Thing {
    fn get_type(&self) -> Box<dyn EntityType>;
}

// pub trait RemoteEntity: Entity + RemoteThing {}

pub trait Relation: Thing {
    fn get_type(&self) -> Box<dyn RelationType> {
        todo!()
    }
}

// pub trait RemoteRelation: Relation + RemoteThing {
//     fn add_player(&self, role_type: Box<dyn RoleType>, player: Box<dyn Thing>) -> Result {
//         todo!()
//     }
// }

#[derive(Clone, Debug)]
pub struct Label {
    scope: Option<String>,
    name: String
}

#[derive(Debug)]
pub struct ThingTypeImpl {
    label: Label
}

impl AsType for ThingTypeImpl {
    fn as_type(self: Box<Self>) -> Box<dyn Type> {
        self
    }
}

impl Type for ThingTypeImpl {
    fn label(&self) -> &Label {
        &self.label
    }
}

impl AsEntity for ThingTypeImpl {
    fn as_entity(self: Box<Self>) -> Box<dyn Entity> {
        todo!()
    }
}

impl AsThing for ThingTypeImpl {
    fn as_thing(self: Box<Self>) -> Box<dyn Thing> {
        todo!()
    }
}

impl AsConcept for ThingTypeImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }
}

impl Concept for ThingTypeImpl {}

impl ThingType for ThingTypeImpl {}

// #[derive(Debug)]
// pub struct RemoteThingTypeImpl {
//     label: Label,
//     tx: Transaction
// }
//
// impl ThingType for RemoteThingTypeImpl {}
//
// impl AsType for RemoteThingTypeImpl {
//     fn as_type(self: Box<Self>) -> Box<dyn Type> {
//         self
//     }
// }
//
// impl Type for RemoteThingTypeImpl {
//     fn label(&self) -> &Label {
//         &self.label
//     }
// }
//
// impl AsConcept for RemoteThingTypeImpl {
//     fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
//         self
//     }
// }
//
// impl AsEntity for RemoteThingTypeImpl {
//     fn as_entity(self: Box<Self>) -> Result<Box<dyn Entity>> { Ok(self) }
// }
//
// impl AsThing for RemoteThingTypeImpl {
//     fn as_thing(self: Box<Self>) -> Box<dyn Thing> {
//         todo!()
//     }
// }
//
// impl Concept for RemoteThingTypeImpl {}
//
// impl RemoteType for RemoteThingTypeImpl {}
//
// impl RemoteConcept for RemoteThingTypeImpl {
//     fn transaction(&self) -> &Transaction {
//         &self.tx
//     }
// }
//
// impl RemoteThingType for RemoteThingTypeImpl {}

#[derive(Clone, Debug)]
pub struct EntityTypeImpl {
    label: Label
}

impl ThingType for EntityTypeImpl {}

impl AsType for EntityTypeImpl {
    fn as_type(self: Box<Self>) -> Box<dyn Type> {
        self
    }
}

impl Type for EntityTypeImpl {
    fn label(&self) -> &Label {
        &self.label
    }
}

impl AsConcept for EntityTypeImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }
}

impl AsEntity for EntityTypeImpl {
    fn as_entity(self: Box<Self>) -> Box<dyn Entity> {
        todo!()
    }
}

impl AsThing for EntityTypeImpl {
    fn as_thing(self: Box<Self>) -> Box<dyn Thing> {
        todo!()
    }
}

impl Concept for EntityTypeImpl {}

impl EntityType for EntityTypeImpl {}

// #[derive(Debug)]
// pub struct RemoteEntityTypeImpl {
//     label: Label,
//     tx: Transaction
// }
//
// impl EntityType for RemoteEntityTypeImpl {}
//
// impl ThingType for RemoteEntityTypeImpl {}
//
// impl AsType for RemoteEntityTypeImpl {
//     fn as_type(self: Box<Self>) -> Box<dyn Type> {
//         self
//     }
// }
//
// impl Type for RemoteEntityTypeImpl {
//     fn label(&self) -> &Label {
//         &self.label
//     }
// }
//
// impl AsConcept for RemoteEntityTypeImpl {
//     fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
//         self
//     }
// }
//
// impl Concept for RemoteEntityTypeImpl {}
//
// impl RemoteThingType for RemoteEntityTypeImpl {}
//
// impl RemoteType for RemoteEntityTypeImpl {}
//
// impl RemoteConcept for RemoteEntityTypeImpl {
//     fn transaction(&self) -> &Transaction {
//         &self.tx
//     }
// }
//
// impl RemoteEntityType for RemoteEntityTypeImpl {}

#[derive(Debug)]
pub struct EntityImpl {
    iid: String,
    type_: EntityTypeImpl
}

impl Thing for EntityImpl {
    fn get_type(&self) -> Box<dyn ThingType> {
        todo!()
    }
}

impl AsEntity for EntityImpl {
    fn as_entity(self: Box<Self>) -> Box<dyn Entity> {
        self
    }
}

impl AsThing for EntityImpl {
    fn as_thing(self: Box<Self>) -> Box<dyn Thing> {
        self
    }
}

impl AsConcept for EntityImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }
}

impl Concept for EntityImpl {}

impl Entity for EntityImpl {
    fn get_type(&self) -> Box<dyn EntityType> {
        Box::new(self.type_.clone())
    }
}
