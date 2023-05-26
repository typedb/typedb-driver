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


import {Concept} from "../Concept";

export interface Value extends Concept {

    readonly valueType: Concept.ValueType;

    readonly value: boolean | string | number | Date;

    isBoolean(): boolean;

    isLong(): boolean;

    isDouble(): boolean;

    isString(): boolean;

    isDateTime(): boolean;

    asBoolean(): Value.Boolean;

    asLong(): Value.Long;

    asDouble(): Value.Double;

    asString(): Value.String;

    asDateTime(): Value.DateTime;
}

export namespace Value {

    export interface Boolean extends Value {

        readonly value: boolean;

    }

    export interface Long extends Value {

        readonly value: number;

    }

    export interface Double extends Value {

        readonly value: number;

    }

    export interface String extends Value {

        readonly value: string;

    }

    export interface DateTime extends Value {

        readonly value: Date;

    }
}
