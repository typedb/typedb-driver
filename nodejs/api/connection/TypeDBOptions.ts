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

import {Options} from "typedb-protocol/proto/options";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBDriverError} from "../../common/errors/TypeDBDriverError";
import NEGATIVE_VALUE_NOT_ALLOWED = ErrorMessage.Driver.NEGATIVE_VALUE_NOT_ALLOWED;

/** Interface for TypeDBOptions. Use {@link TypeDBOptions} instead. */
export interface Opts {
    /** If set to <code>True</code>, enables inference for queries. Only settable at transaction level and above. Only affects read transactions.*/
    infer?: boolean;
    /** If set to <code>True</code>, reasoning tracing graphs are output in the logging directory. Should be used with <code>parallel = False</code>.*/
    traceInference?: boolean;
    /** If set to <code>True</code>, enables explanations for queries. Only affects read transactions.*/
    explain?: boolean;
    /** If set to <code>True</code>, the server uses parallel instead of single-threaded execution.*/
    parallel?: boolean;
    /** If set, specifies a guideline number of answers that the server should send before the driver issues a fresh request. */
    prefetchSize?: number;
    /** If set to <code>True</code>, the first batch of answers is streamed to the driver even without an explicit request for it. */
    prefetch?: boolean;
    /** If set, specifies a timeout that allows the server to close sessions if the driver terminates or becomes unresponsive. */
    sessionIdleTimeoutMillis?: number;
    /** If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions. */
    transactionTimeoutMillis?: number;
    /** If set, specifies how long the driver should wait if opening a session or transaction is blocked by a schema write lock. */
    schemaLockAcquireTimeoutMillis?: number;
    /** If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput. Only settable in TypeDB Enterprise. */
    readAnyReplica?: boolean;
}

namespace Opts {
    export function proto(options: TypeDBOptions): Options {
        const optionsProto = new Options();
        if (options) {
            if (options.infer != null) optionsProto.infer = options.infer;
            if (options.traceInference != null) optionsProto.trace_inference = options.traceInference;
            if (options.explain != null) optionsProto.explain = options.explain;
            if (options.parallel != null) optionsProto.parallel = options.parallel;
            if (options.prefetchSize != null) optionsProto.prefetch_size = options.prefetchSize;
            if (options.prefetch != null) optionsProto.prefetch = options.prefetch;
            if (options.sessionIdleTimeoutMillis != null) optionsProto.session_idle_timeout_millis = options.sessionIdleTimeoutMillis;
            if (options.transactionTimeoutMillis != null) optionsProto.transaction_timeout_millis = options.transactionTimeoutMillis;
            if (options.schemaLockAcquireTimeoutMillis != null) optionsProto.schema_lock_acquire_timeout_millis = options.schemaLockAcquireTimeoutMillis;
            if (options.readAnyReplica != null) optionsProto.read_any_replica = options.readAnyReplica;
        }
        return optionsProto;
    }
}

/**
 * TypeDB session and transaction options. <code>TypeDBOptions</code> object can be used to override the default server behaviour query processing.
 * Options could be specified either as constructor arguments or using setters.
 *
 * ### Examples
 *
 * ```ts
 * transactionOptions = new TypeDBOptions({"infer": true})
 * transactionOptions.infer(true)
 * ```
 */
export class TypeDBOptions implements Opts {
    private _infer: boolean;
    private _traceInference: boolean;
    private _explain: boolean;
    private _parallel: boolean;
    private _prefetchSize: number;
    private _prefetch: boolean;
    private _sessionIdleTimeoutMillis: number;
    private _transactionTimeoutMillis: number;
    private _schemaLockAcquireTimeoutMillis: number;
    private _readAnyReplica: boolean;

    constructor(obj: { [K in keyof Opts]: Opts[K] } = {}) {
        Object.assign(this, obj);
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
            throw new TypeDBDriverError(NEGATIVE_VALUE_NOT_ALLOWED.message(value));
        }
        this._prefetchSize = value;
    }

    get sessionIdleTimeoutMillis() {
        return this._sessionIdleTimeoutMillis;
    }

    set sessionIdleTimeoutMillis(millis: number) {
        if (millis < 1) {
            throw new TypeDBDriverError(NEGATIVE_VALUE_NOT_ALLOWED.message(millis));
        }
        this._sessionIdleTimeoutMillis = millis;
    }

    get transactionTimeoutMillis() {
        return this._transactionTimeoutMillis;
    }

    set transactionTimeoutMillis(millis: number) {
        if (millis < 1) {
            throw new TypeDBDriverError(NEGATIVE_VALUE_NOT_ALLOWED.message(millis));
        }
        this._transactionTimeoutMillis = millis;
    }

    get schemaLockAcquireTimeoutMillis() {
        return this._schemaLockAcquireTimeoutMillis;
    }

    set schemaLockAcquireTimeoutMillis(value: number) {
        if (value < 1) {
            throw new TypeDBDriverError(NEGATIVE_VALUE_NOT_ALLOWED.message(value));
        }
        this._schemaLockAcquireTimeoutMillis = value;
    }

    get readAnyReplica() {
        return this._readAnyReplica;
    }

    set readAnyReplica(value: boolean) {
        this._readAnyReplica = value;
    }
}
