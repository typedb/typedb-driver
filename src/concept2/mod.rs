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
use typedb_protocol::concept::{Attribute_Value_oneof_value, Concept_oneof_concept, Type_Encoding};
use crate::common::error::MESSAGES;
use crate::common::Result;
use crate::transaction::Transaction;

pub trait CastConcept {
    // not applicable: the size for values of type `Self` cannot be known at compilation time
    // fn as_concept(self: Box<Self>) -> Box<dyn Concept> { self }
    fn as_concept(self: Box<Self>) -> Box<dyn Concept>;

    fn as_type(self: Box<Self>) -> Result<Box<dyn Type>>;

    fn as_thing(self: Box<Self>) -> Result<Box<dyn Thing>>;

    fn as_entity(self: Box<Self>) -> Result<Box<dyn Entity>>;

    fn as_attribute(self: Box<Self>) -> Result<Box<dyn Attribute>>;
}

pub trait CastAttribute {
    fn as_long(self: Box<Self>) -> Result<Box<dyn LongAttribute>>;

    fn as_string(self: Box<Self>) -> Result<Box<dyn StringAttribute>>;
}

pub trait Concept: CastConcept + Debug {
    // fn is_entity_type(&self) -> bool;

    // fn is_relation_type(&self) -> bool;

    // fn is_thing_type(&self) -> bool;

    // can't write this, because it ends up being used by every implementation of Concept (including EntityImpl)
    // fn is_entity(&self) -> bool {
    //     println!("called Concept.is_entity on {:?}", self);
    //     false
    // }

    fn is_thing(&self) -> bool;

    fn is_entity(&self) -> bool;

    fn is_attribute(&self) -> bool;
}

impl dyn Concept {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Concept) -> Result<Box<dyn Concept>> {
        let concept = proto.concept.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["concept"]))?;
        match concept {
            Concept_oneof_concept::thing(thing) => { <dyn Thing>::from_proto(thing).map(|thing| thing.as_concept()) }
            // TODO: throws "trait upcasting is experimental"; see #![feature(trait_upcasting)
            // Concept_oneof_concept::field_type(type_concept) => { Ok(<dyn Type>::from_proto(type_concept)) }
            Concept_oneof_concept::field_type(type_concept) => { Ok(<dyn Type>::from_proto(type_concept).as_concept()) }
        }
    }
}

pub trait Type: Concept {
    fn label(&self) -> &Label;

    // this is not useful to write - we still need to implement is_thing from Concept in all enums
    // fn is_thing(&self) -> bool { false }

    fn is_entity(&self) -> bool { false }
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

pub trait ThingType: Type {
    // fn is_thing_type(&self) -> bool {
    //     true
    // }

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
    // fn is_entity_type(&self) -> bool {
    //     true
    // }
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
    // fn is_relation_type(&self) -> bool {
    //     true
    // }

    fn get_instances(&self) -> Result<Vec<Box<dyn Relation>>>;
}

pub trait RoleType: Type {}

pub trait Thing: Concept {
    fn iid(&self) -> String {
        todo!()
    }

    fn get_type(&self) -> Box<dyn ThingType>;

    fn is_thing(&self) -> bool { true }
}

impl dyn Thing {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Thing) -> Result<Box<dyn Thing>> {
        match proto.get_field_type().encoding {
            Type_Encoding::ENTITY_TYPE => Ok(Box::new(EntityImpl { iid: iid_from_bytes(proto.get_iid()), type_: *<dyn EntityType>::from_proto(proto.get_field_type().clone()) })),
            Type_Encoding::RELATION_TYPE => { todo!() }
            Type_Encoding::ATTRIBUTE_TYPE => {
                match <dyn Attribute>::from_proto(proto) {
                    Ok(attr) => { attr.as_thing() }
                    Err(err) => { Err(err) }
                }
            }
            _ => { todo!() }
        }
    }
}

fn iid_from_bytes(bytes: &[u8]) -> String {
    format!("{:02x?}", bytes)
    // String::from(std::str::from_utf8(bytes).unwrap())
    // Uuid::from_slice(bytes).unwrap().to_string()
}

pub trait Entity: Thing {
    fn get_type(&self) -> Box<dyn EntityType>;

    // it's useless to write this, because EntityImpl still needs to implement Concept, which itself mandates is_entity
    // fn is_entity(&self) -> bool { true }
}

pub trait Relation: Thing {
    fn get_type(&self) -> Box<dyn RelationType>;
}

pub trait Attribute: Thing + CastAttribute {
    // fn get_type(&self) -> Box<dyn AttributeType>;

    fn is_long(&self) -> bool;

    fn is_string(&self) -> bool;
}

impl dyn Attribute {
    pub(crate) fn from_proto(mut proto: typedb_protocol::concept::Thing) -> Result<Box<dyn Attribute>> {
        let value = proto.take_value().value.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["concept"]))?;
        match value {
            Attribute_Value_oneof_value::string(str_value) => { Ok(Box::new(StringAttributeImpl { iid: iid_from_bytes(proto.get_iid()), value: str_value })) }
            Attribute_Value_oneof_value::long(long_value) => { Ok(Box::new(LongAttributeImpl { iid: iid_from_bytes(proto.get_iid()), value: long_value })) }
            _ => { todo!() }
        }
    }
}

pub trait LongAttribute: Attribute {
    fn get_value(&self) -> i64;
}

pub trait StringAttribute: Attribute {
    fn get_value(&self) -> &str;
}

#[derive(Clone, Debug)]
pub struct Label {
    pub scope: Option<String>,
    pub name: String
}

#[derive(Debug)]
pub struct ThingTypeImpl {
    label: Label
}

impl Type for ThingTypeImpl {
    fn label(&self) -> &Label {
        &self.label
    }
}

impl CastConcept for ThingTypeImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }

    fn as_type(self: Box<Self>) -> Result<Box<dyn Type>> {
        Ok(self)
    }

    fn as_thing(self: Box<Self>) -> Result<Box<dyn Thing>> {
        todo!()
    }

    fn as_entity(self: Box<Self>) -> Result<Box<dyn Entity>> {
        todo!()
    }

    fn as_attribute(self: Box<Self>) -> Result<Box<dyn Attribute>> {
        todo!()
    }
}

impl Concept for ThingTypeImpl {
    fn is_thing(&self) -> bool {
        false
    }

    fn is_entity(&self) -> bool {
        false
    }

    fn is_attribute(&self) -> bool {
        false
    }
}

impl ThingType for ThingTypeImpl {}

#[derive(Clone, Debug)]
pub struct EntityTypeImpl {
    label: Label
}

impl ThingType for EntityTypeImpl {}

impl Type for EntityTypeImpl {
    fn label(&self) -> &Label {
        &self.label
    }
}

impl CastConcept for EntityTypeImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }

    fn as_type(self: Box<Self>) -> Result<Box<dyn Type>> {
        Ok(self)
    }

    fn as_thing(self: Box<Self>) -> Result<Box<dyn Thing>> {
        todo!()
    }

    fn as_entity(self: Box<Self>) -> Result<Box<dyn Entity>> {
        todo!()
    }

    fn as_attribute(self: Box<Self>) -> Result<Box<dyn Attribute>> {
        todo!()
    }
}

impl Concept for EntityTypeImpl {
    fn is_thing(&self) -> bool {
        false
    }

    fn is_entity(&self) -> bool {
        false
    }

    fn is_attribute(&self) -> bool {
        false
    }
}

impl EntityType for EntityTypeImpl {}

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

impl CastConcept for EntityImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }

    fn as_type(self: Box<Self>) -> Result<Box<dyn Type>> {
        todo!()
    }

    fn as_thing(self: Box<Self>) -> Result<Box<dyn Thing>> {
        Ok(self)
    }

    fn as_entity(self: Box<Self>) -> Result<Box<dyn Entity>> {
        Ok(self)
    }

    fn as_attribute(self: Box<Self>) -> Result<Box<dyn Attribute>> {
        todo!()
    }
}

impl Concept for EntityImpl {
    fn is_thing(&self) -> bool { true }

    fn is_entity(&self) -> bool { true }

    fn is_attribute(&self) -> bool { false }
}

impl Entity for EntityImpl {
    fn get_type(&self) -> Box<dyn EntityType> {
        Box::new(self.type_.clone())
    }
}

#[derive(Debug)]
pub struct LongAttributeImpl {
    pub iid: String,
    pub value: i64,
    // type_: LongAttributeTypeImpl
}

impl Attribute for LongAttributeImpl {
    fn is_long(&self) -> bool {
        true
    }

    fn is_string(&self) -> bool {
        false
    }
}

impl Thing for LongAttributeImpl {
    fn get_type(&self) -> Box<dyn ThingType> {
        todo!()
    }
}

impl Concept for LongAttributeImpl {
    fn is_thing(&self) -> bool {
        true
    }

    fn is_entity(&self) -> bool {
        false
    }

    fn is_attribute(&self) -> bool {
        true
    }
}

impl CastConcept for LongAttributeImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }

    fn as_type(self: Box<Self>) -> Result<Box<dyn Type>> {
        todo!()
    }

    fn as_thing(self: Box<Self>) -> Result<Box<dyn Thing>> {
        Ok(self)
    }

    fn as_entity(self: Box<Self>) -> Result<Box<dyn Entity>> {
        todo!()
    }

    fn as_attribute(self: Box<Self>) -> Result<Box<dyn Attribute>> {
        Ok(self)
    }
}

impl CastAttribute for LongAttributeImpl {
    fn as_long(self: Box<Self>) -> Result<Box<dyn LongAttribute>> {
        Ok(self)
    }

    fn as_string(self: Box<Self>) -> Result<Box<dyn StringAttribute>> {
        todo!()
    }
}

impl LongAttribute for LongAttributeImpl {
    fn get_value(&self) -> i64 {
        self.value
    }
}

#[derive(Debug)]
pub struct StringAttributeImpl {
    iid: String,
    value: String,
    // type_: StringAttributeTypeImpl
}

impl Attribute for StringAttributeImpl {
    fn is_long(&self) -> bool {
        false
    }

    fn is_string(&self) -> bool {
        true
    }
}

impl Thing for StringAttributeImpl {
    fn get_type(&self) -> Box<dyn ThingType> {
        todo!()
    }
}

impl Concept for StringAttributeImpl {
    fn is_thing(&self) -> bool {
        true
    }

    fn is_entity(&self) -> bool {
        false
    }

    fn is_attribute(&self) -> bool {
        true
    }
}

impl CastConcept for StringAttributeImpl {
    fn as_concept(self: Box<Self>) -> Box<dyn Concept> {
        self
    }

    fn as_type(self: Box<Self>) -> Result<Box<dyn Type>> {
        todo!()
    }

    fn as_thing(self: Box<Self>) -> Result<Box<dyn Thing>> {
        Ok(self)
    }

    fn as_entity(self: Box<Self>) -> Result<Box<dyn Entity>> {
        todo!()
    }

    fn as_attribute(self: Box<Self>) -> Result<Box<dyn Attribute>> {
        Ok(self)
    }
}

impl CastAttribute for StringAttributeImpl {
    fn as_long(self: Box<Self>) -> Result<Box<dyn LongAttribute>> {
        todo!()
    }

    fn as_string(self: Box<Self>) -> Result<Box<dyn StringAttribute>> {
        Ok(self)
    }
}

impl StringAttribute for StringAttributeImpl {
    fn get_value(&self) -> &str {
        self.value.as_str()
    }
}
