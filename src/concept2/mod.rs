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

use crate::common::Result;

pub trait Concept {
    fn as_thing_type(&self) -> Result<Box<dyn ThingType>> {
        todo!()
    }

    fn is_entity_type(&self) -> bool {
        false
    }

    fn is_relation_type(&self) -> bool {
        false
    }

    fn is_thing_type(&self) -> bool {
        false
    }
}

pub trait RemoteConcept: Concept {
    fn is_deleted(&self) -> Result<bool> {
        todo!()
    }
}

pub trait Type: Concept {
    fn get_label(&self) -> Label {
        todo!()
    }
}

pub trait RemoteType: Type + RemoteConcept {
    fn get_supertype(&self) -> Result<Option<Box<dyn Type>>> {
        todo!()
    }
}

pub trait ThingType: Type {
    fn is_thing_type(&self) -> bool {
        true
    }

    fn get_instances(&self) -> Result<Vec<Box<dyn Thing>>> {
        todo!()
    }
}

pub trait RemoteThingType: ThingType + RemoteType {
    fn get_supertype(&self) -> Result<Option<Box<dyn ThingType>>>;

    fn get_plays(&self) -> Result<Vec<Box<dyn RoleType>>> {
        todo!()
    }
}

pub trait EntityType: ThingType {
    fn is_entity_type(&self) -> bool {
        true
    }

    fn get_instances(&self) -> Result<Vec<Box<dyn Entity>>>;
}

pub trait RemoteEntityType: EntityType + RemoteThingType {
    fn get_supertype(&self) -> Result<Option<Box<dyn EntityType>>>;
}

pub trait RelationType: ThingType {
    fn is_relation_type(&self) -> bool {
        true
    }

    fn get_instances(&self) -> Result<Vec<Box<dyn Relation>>>;
}

pub trait RemoteRelationType: RelationType + RemoteThingType {
    fn get_supertype(&self) -> Result<Option<Box<dyn RelationType>>>;

    fn get_relates(&self) -> Result<Vec<Box<dyn RoleType>>> {
        todo!()
    }
}

pub trait RoleType: Type {}

pub trait RemoteRoleType: RoleType + RemoteType {}

pub trait Thing: Concept {
    fn iid(&self) -> String {
        todo!()
    }

    fn get_type(&self) -> Box<dyn ThingType>;
}

pub trait RemoteThing: Thing + RemoteConcept {
    fn get_playing(&self) -> Result<Vec<Box<dyn RoleType>>> {
        todo!()
    }
}

pub trait Entity: Thing {
    fn get_type(&self) -> Box<dyn EntityType> {
        todo!()
    }
}

pub trait RemoteEntity: Entity + RemoteThing {}

pub trait Relation: Thing {
    fn get_type(&self) -> Box<dyn RelationType> {
        todo!()
    }
}

pub trait RemoteRelation: Relation + RemoteThing {
    fn add_player(&self, role_type: Box<dyn RoleType>, player: Box<dyn Thing>) -> Result {
        todo!()
    }
}

pub struct Label {
    scope: String,
    name: String
}
