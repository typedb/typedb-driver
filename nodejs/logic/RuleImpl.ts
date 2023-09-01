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

import {Rule as RuleProto} from "typedb-protocol/proto/logic";
import {TypeDBTransaction} from "../api/connection/TypeDBTransaction";
import {Rule} from "../api/logic/Rule";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import deleteReq = RequestBuilder.Rule.deleteReq;
import setLabelReq = RequestBuilder.Rule.setLabelReq;

export class RuleImpl implements Rule {
    protected readonly _when: string;
    protected readonly _then: string;
    protected _label: string;

    constructor(label: string, when: string, then: string) {
        this._label = label;
        this._when = when;
        this._then = then;
    }

    get label(): string {
        return this._label;
    }

    get when(): string {
        return this._when;
    }

    get then(): string {
        return this._then;
    }

    toString() {
        return "Rule[" + this._label + "]";
    }

    async delete(transaction: TypeDBTransaction): Promise<void> {
        const ext = transaction as TypeDBTransaction.Extended;
        await ext.rpcExecute(deleteReq(this._label));
    }

    async isDeleted(transaction: TypeDBTransaction): Promise<boolean> {
        return !(await transaction.logic.getRule(this._label));
    }

    async setLabel(transaction: TypeDBTransaction, label: string): Promise<void> {
        const ext = transaction as TypeDBTransaction.Extended;
        await ext.rpcExecute(setLabelReq(this._label, label));
        this._label = label;
    }
}

export namespace RuleImpl {
    export function of(ruleProto: RuleProto) {
        return new RuleImpl(ruleProto.label, ruleProto.when, ruleProto.then);
    }
}
