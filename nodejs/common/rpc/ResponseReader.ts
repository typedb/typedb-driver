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

import {Concept as ConceptProto} from "typedb-protocol/proto/concept"
import {EntityTypeImpl} from "../../concept/type/EntityTypeImpl";
import {RelationTypeImpl} from "../../concept/type/RelationTypeImpl";
import {AttributeTypeImpl} from "../../concept/type/AttributeTypeImpl";
import {RoleTypeImpl} from "../../concept/type/RoleTypeImpl";
import {ThingTypeImpl} from "../../concept/type/ThingTypeImpl";
import {EntityImpl} from "../../concept/thing/EntityImpl";
import {RelationImpl} from "../../concept/thing/RelationImpl";
import {AttributeImpl} from "../../concept/thing/AttributeImpl";
import {ValueImpl} from "../../concept/value/ValueImpl";
import {TypeDBClientError} from "../errors/TypeDBClientError";
import {ErrorMessage} from "../errors/ErrorMessage";
import BAD_ENCODING = ErrorMessage.Concept.BAD_ENCODING;

/* eslint no-inner-declarations: "off" */
export namespace ResponseReader {
    export namespace Concept {
        export function of(proto: ConceptProto) {
            if (proto.has_entity_type) return EntityTypeImpl.ofEntityTypeProto(proto.entity_type);
            else if (proto.has_relation_type) return RelationTypeImpl.ofRelationTypeProto(proto.relation_type);
            else if (proto.has_attribute_type) return AttributeTypeImpl.ofAttributeTypeProto(proto.attribute_type);
            else if (proto.has_role_type) return RoleTypeImpl.ofRoleTypeProto(proto.role_type);
            else if (proto.has_thing_type_root) return ThingTypeImpl.Root.ofThingTypeRootProto(proto.thing_type_root);
            else if (proto.has_entity) return EntityImpl.ofEntityProto(proto.entity);
            else if (proto.has_relation) return RelationImpl.ofRelationProto(proto.relation);
            else if (proto.has_attribute) return AttributeImpl.ofAttributeProto(proto.attribute);
            else if (proto.has_value) return ValueImpl.ofValueProto(proto.value);
            else throw new TypeDBClientError(BAD_ENCODING.message(proto));
        }
    }
}
