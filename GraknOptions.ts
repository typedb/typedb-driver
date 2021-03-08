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

export class GraknOptions {
    infer?: boolean;
    traceInference?: boolean;
    explain?: boolean;
    parallel?: boolean;
    batchSize?: number;
    prefetch?: boolean;
    sessionIdleTimeoutMillis?: number;
    schemaLockAcquireTimeoutMillis?: number;

    constructor(obj: {[K in keyof GraknOptions]: GraknOptions[K]} = {}) {
        Object.assign(this, obj);
    }
}

export class GraknOptionsCluster extends GraknOptions {
    readAnyReplica?: boolean;

    constructor(obj: {[K in keyof GraknOptionsCluster]: GraknOptionsCluster[K]} = {}) {
        super(obj);
    }
}

export namespace GraknOptions {
    export function core(options: {[K in keyof GraknOptions]: GraknOptions[K]} = {}) {
        return new GraknOptions(options);
    }

    export function cluster(options: {[K in keyof GraknOptionsCluster]: GraknOptionsCluster[K]} = {}) {
        return new GraknOptionsCluster(options);
    }
}
