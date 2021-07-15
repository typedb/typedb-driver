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


import { Type as TypeProto } from "typedb-protocol/common/concept_pb";
import { ErrorMessage } from "../../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../../common/errors/TypeDBClientError";
import { Label } from "../../../common/Label";
import { Stream } from "../../../common/util/Stream";
import { TypeDBTransaction } from "../../connection/TypeDBTransaction";
import { Concept } from "../Concept";
import { Attribute } from "../thing/Attribute";
import { Entity } from "../thing/Entity";
import { Relation } from "../thing/Relation";
import { Thing } from "../thing/Thing";
import { AttributeType } from "./AttributeType";
import { EntityType } from "./EntityType";
import { RelationType } from "./RelationType";
import { RoleType } from "./RoleType";
import { ThingType } from "./ThingType";
import BAD_ENCODING = ErrorMessage.Concept.BAD_ENCODING;

export interface Type extends Concept {

    readonly label: Label;

    readonly root: boolean;

    asRemote(transaction: TypeDBTransaction): Type.Remote;
}

export namespace Type {

    export interface Remote extends Type, Concept.Remote {

        asRemote(transaction: TypeDBTransaction): Type.Remote;

        asType(): Type.Remote;

        asThingType(): ThingType.Remote;

        asEntityType(): EntityType.Remote;

        asAttributeType(): AttributeType.Remote;

        asRelationType(): RelationType.Remote;

        asRoleType(): RoleType.Remote;

        asThing(): Thing.Remote;

        asEntity(): Entity.Remote;

        asAttribute(): Attribute.Remote;

        asRelation(): Relation.Remote;

        setLabel(label: string): Promise<void>;

        isAbstract(): Promise<boolean>;

        getSupertype(): Promise<Type>;

        getSupertypes(): Stream<Type>;

        getSubtypes(): Stream<Type>;
    }

    export function encoding(type: Type): TypeProto.Encoding {
        if (type.isEntityType()) {
            return TypeProto.Encoding.ENTITY_TYPE;
        } else if (type.isRelationType()) {
            return TypeProto.Encoding.RELATION_TYPE;
        } else if (type.isAttributeType()) {
            return TypeProto.Encoding.ATTRIBUTE_TYPE;
        } else if (type.isRoleType()) {
            return TypeProto.Encoding.ROLE_TYPE;
        } else if (type.isThingType()) {
            return TypeProto.Encoding.THING_TYPE;
        } else {
            throw new TypeDBClientError(BAD_ENCODING);
        }
    }
}
