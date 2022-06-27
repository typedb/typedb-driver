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

import {Options} from "typedb-protocol/common/options_pb";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import NEGATIVE_VALUE_NOT_ALLOWED = ErrorMessage.Client.NEGATIVE_VALUE_NOT_ALLOWED;

namespace Opts {
    export interface Core {
        infer?: boolean;
        traceInference?: boolean;
        explain?: boolean;
        parallel?: boolean;
        prefetchSize?: number;
        prefetch?: boolean;
        sessionIdleTimeoutMillis?: number;
        transactionTimeoutMillis?: number;
        schemaLockAcquireTimeoutMillis?: number;
    }

    export interface Cluster extends Core {
        readAnyReplica?: boolean;
    }

    export function proto(options: TypeDBOptions): Options {
        const optionsProto = new Options();
        if (options) {
            if (options.infer != null) optionsProto.setInfer(options.infer);
            if (options.traceInference != null) optionsProto.setTraceInference(options.traceInference);
            if (options.explain != null) optionsProto.setExplain(options.explain);
            if (options.parallel != null) optionsProto.setParallel(options.parallel);
            if (options.prefetchSize != null) optionsProto.setPrefetchSize(options.prefetchSize);
            if (options.prefetch != null) optionsProto.setPrefetch(options.prefetch);
            if (options.sessionIdleTimeoutMillis != null) optionsProto.setSessionIdleTimeoutMillis(options.sessionIdleTimeoutMillis);
            if (options.transactionTimeoutMillis != null) optionsProto.setTransactionTimeoutMillis(options.transactionTimeoutMillis);
            if (options.schemaLockAcquireTimeoutMillis != null) optionsProto.setSchemaLockAcquireTimeoutMillis(options.schemaLockAcquireTimeoutMillis);
            if (options.isCluster()) {
                const clusterOptions = options as Opts.Cluster;
                if (clusterOptions.readAnyReplica != null) optionsProto.setReadAnyReplica(clusterOptions.readAnyReplica);
            }
        }
        return optionsProto;
    }
}

export class TypeDBOptions implements Opts.Core {

    private _infer: boolean;
    private _traceInference: boolean;
    private _explain: boolean;
    private _parallel: boolean;
    private _prefetchSize: number;
    private _prefetch: boolean;
    private _sessionIdleTimeoutMillis: number;
    private _transactionTimeoutMillis: number;
    private _schemaLockAcquireTimeoutMillis: number;

    constructor(obj: { [K in keyof Opts.Core]: Opts.Core[K] } = {}) {
        Object.assign(this, obj);
    }

    isCluster(): boolean {
        return false;
    }

    proto(): Options {
        return Opts.proto(this);
    }

    get infer() {
        return this._infer;
    }

    set infer(value: boolean) {
        this._infer = value;
    }

    get traceInference() {
        return this._traceInference;
    }

    set traceInference(value: boolean) {
        this._traceInference = value;
    }

    get explain() {
        return this._explain;
    }

    set explain(value: boolean) {
        this._explain = value;
    }

    get parallel() {
        return this._parallel;
    }

    set parallel(value: boolean) {
        this._parallel = value;
    }

    get prefetch() {
        return this._prefetch;
    }

    set prefetch(value: boolean) {
        this._prefetch = value;
    }

    get prefetchSize() {
        return this._prefetchSize;
    }

    set prefetchSize(value: number) {
        if (value < 1) {
            throw new TypeDBClientError(NEGATIVE_VALUE_NOT_ALLOWED.message(value));
        }
        this._prefetchSize = value;
    }

    get sessionIdleTimeoutMillis() {
        return this._sessionIdleTimeoutMillis;
    }

    set sessionIdleTimeoutMillis(millis: number) {
        if (millis < 1) {
            throw new TypeDBClientError(NEGATIVE_VALUE_NOT_ALLOWED.message(millis));
        }
        this._sessionIdleTimeoutMillis = millis;
    }

    get transactionTimeoutMillis() {
        return this._transactionTimeoutMillis;
    }

    set transactionTimeoutMillis(millis: number) {
        if (millis < 1) {
            throw new TypeDBClientError(NEGATIVE_VALUE_NOT_ALLOWED.message(millis));
        }
        this._transactionTimeoutMillis = millis;
    }

    get schemaLockAcquireTimeoutMillis() {
        return this._schemaLockAcquireTimeoutMillis;
    }

    set schemaLockAcquireTimeoutMillis(value: number) {
        if (value < 1) {
            throw new TypeDBClientError(NEGATIVE_VALUE_NOT_ALLOWED.message(value));
        }
        this._schemaLockAcquireTimeoutMillis = value;
    }
}

export class TypeDBClusterOptions extends TypeDBOptions implements Opts.Cluster {

    private _readAnyReplica: boolean;

    constructor(obj: { [K in keyof Opts.Cluster]: Opts.Cluster[K] } = {}) {
        super(obj);
    }

    isCluster(): boolean {
        return true;
    }

    get readAnyReplica() {
        return this._readAnyReplica;
    }

    set readAnyReplica(value: boolean) {
        this._readAnyReplica = value;
    }
}

export namespace TypeDBOptions {

    export function core(options: { [K in keyof Opts.Core]: Opts.Core[K] } = {}) {
        return new TypeDBOptions(options);
    }

    export function cluster(options: { [K in keyof Opts.Cluster]: Opts.Cluster[K] } = {}) {
        return new TypeDBClusterOptions(options);
    }
}
