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


import {Label} from "../../../common/Label";
import {Stream} from "../../../common/util/Stream";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Concept} from "../Concept";
import Transitivity = Concept.Transitivity;

export interface Type extends Concept {
    readonly label: Label;

    readonly root: boolean;

    readonly abstract: boolean;

    delete(transaction: TypeDBTransaction): Promise<void>;
    isDeleted(transaction: TypeDBTransaction): Promise<boolean>;

    setLabel(transaction: TypeDBTransaction, label: string): Promise<void>;

    getSupertype(transaction: TypeDBTransaction): Promise<Type>;

    getSupertypes(transaction: TypeDBTransaction): Stream<Type>;

    getSubtypes(transaction: TypeDBTransaction): Stream<Type>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Type>;
}
