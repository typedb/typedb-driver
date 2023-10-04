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

import {Attribute} from "./thing/Attribute";
import {Entity} from "./thing/Entity";
import {Relation} from "./thing/Relation";
import {AttributeType} from "./type/AttributeType";
import {EntityType} from "./type/EntityType";
import {RelationType} from "./type/RelationType";
import {ThingType} from "./type/ThingType";
import {Concept} from "./Concept";
import {TypeDBDriverError} from "../../common/errors/TypeDBDriverError";

/**
 * Provides access for all Concept API methods.
 */
export interface ConceptManager {
    /**
     * Retrieves the root <code>ThingType</code>, “thing”.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getRootThingType()
     * ```
     */
    getRootThingType(): Promise<ThingType>;

    /**
     * Retrieves the root <code>EntityType</code>, “entity”.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getRootEntityType()
     * ```
     */
    getRootEntityType(): Promise<EntityType>;
    /**
     * Retrieve the root <code>RelationType</code>, “relation”.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getRootRelationType()
     * ```
     */
    getRootRelationType(): Promise<RelationType>;
    /**
     * Retrieve the root <code>AttributeType</code>, “attribute”.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getRootAttributeType()
     * ```
     */
    getRootAttributeType(): Promise<AttributeType>;

    /**
     * Retrieves an <code>EntityType</code> by its label.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getEntityType(label)
     * ```
     *
     * @param label - The label of the <code>EntityType</code> to retrieve
     */
    getEntityType(label: string): Promise<EntityType>;
    /**
     * Retrieves a <code>RelationType</code> by its label.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getRelationType(label)
     * ```
     *
     * @param label - The label of the <code>RelationType</code> to retrieve
     */
    getRelationType(label: string): Promise<RelationType>;
    /**
     * Retrieves an <code>AttributeType</code> by its label.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getAttributeType(label)
     * ```
     *
     * @param label - The label of the <code>AttributeType</code> to retrieve
     */
    getAttributeType(label: string): Promise<AttributeType>;

    /**
     * Creates a new <code>EntityType</code> if none exists with the given label, otherwise retrieves the existing one.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().putEntityType(label)
     * ```
     *
     * @param label - The label of the <code>EntityType</code> to create or retrieve
     */
    putEntityType(label: string): Promise<EntityType>;
    /**
     * Creates a new <code>RelationType</code> if none exists with the given label, otherwise retrieves the existing one.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().putRelationType(label)
     * ```
     *
     * @param label - The label of the <code>RelationType</code> to create or retrieve
     */
    putRelationType(label: string): Promise<RelationType>;
    /**
     * Creates a new <code>AttributeType</code> if none exists with the given label, or retrieves the existing one.
     * or retrieve. :return:
     *
     * ### Examples
     *
     * ```ts
     * await transaction.concepts().putAttributeType(label, valueType)
     * ```
     *
     * @param label - The label of the <code>AttributeType</code> to create or retrieve
     * @param valueType - The value type of the <code>AttributeType</code> to create
     */
    putAttributeType(label: string, valueType: Concept.ValueType): Promise<AttributeType>;

    /**
     * Retrieves an <code>Entity</code> by its iid.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getEntity(iid)
     * ```
     *
     * @param iid - The iid of the <code>Entity</code> to retrieve
     */
    getEntity(iid: string): Promise<Entity>;
    /**
     * Retrieves a <code>Relation</code> by its iid.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getRelation(iid)
     * ```
     *
     * @param iid - The iid of the <code>Relation</code> to retrieve
     */
    getRelation(iid: string): Promise<Relation>;
    /**
     * Retrieves an <code>Attribute</code> by its iid.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getAttribute(iid)
     * ```
     *
     * @param iid - The iid of the <code>Attribute</code> to retrieve
     */
    getAttribute(iid: string): Promise<Attribute>;

    /**
     * Retrieves a list of all exceptions for the current transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.concepts().getSchemaException()
     * ```
     */
    getSchemaExceptions(): Promise<TypeDBDriverError[]>
}
