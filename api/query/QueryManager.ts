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

import { Stream } from "../../common/util/Stream";
import { ConceptMap } from "../answer/ConceptMap";
import { ConceptMapGroup } from "../answer/ConceptMapGroup";
import { Numeric } from "../answer/Numeric";
import { NumericGroup } from "../answer/NumericGroup";
import { TypeDBOptions } from "../connection/TypeDBOptions";
import { Explanation } from "../logic/Explanation";

export interface QueryManager {

    match(query: string, options?: TypeDBOptions): Stream<ConceptMap>;

    matchAggregate(query: string, options?: TypeDBOptions): Promise<Numeric>;

    matchGroup(query: string, options?: TypeDBOptions): Stream<ConceptMapGroup>;

    matchGroupAggregate(query: string, options?: TypeDBOptions): Stream<NumericGroup>;

    insert(query: string, options?: TypeDBOptions): Stream<ConceptMap>;

    delete(query: string, options?: TypeDBOptions): Promise<void>;

    update(query: string, options?: TypeDBOptions): Stream<ConceptMap>;

    define(query: string, options?: TypeDBOptions): Promise<void>;

    undefine(query: string, options?: TypeDBOptions): Promise<void>;

    explain(explainable: ConceptMap.Explainable, options?: TypeDBOptions): Stream<Explanation>;
}
