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

import { Rule as RuleProto } from "typedb-protocol/common/logic_pb";
import { TypeDBTransaction } from "../api/connection/TypeDBTransaction";
import { RemoteRule, Rule } from "../api/logic/Rule";
import { RequestBuilder } from "../common/rpc/RequestBuilder";
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

    asRemote(transaction: TypeDBTransaction): RemoteRule {
        return new RemoteRuleImpl((transaction as TypeDBTransaction.Extended), this._label, this._when, this._then);
    }

    getLabel(): string {
        return this._label;
    }

    getWhen(): string {
        return this._when;
    }

    getThen(): string {
        return this._then;
    }

    isRemote(): boolean {
        return false;
    }

    toString() {
        return "Rule[" + this._label + "]";
    }

}

export class RemoteRuleImpl extends RuleImpl implements RemoteRule {
    private _transaction: TypeDBTransaction.Extended;

    constructor(transaction: TypeDBTransaction.Extended, label: string, when: string, then: string) {
        super(label, when, then);
        this._transaction = transaction;
    }

    async delete(): Promise<void> {
        await this._transaction.rpcExecute(deleteReq(this._label));
    }

    async isDeleted(): Promise<boolean> {
        return !(await this._transaction.logic().getRule(this._label));
    }

    async setLabel(label: string): Promise<void> {
        await this._transaction.rpcExecute(setLabelReq(this._label, label));
        this._label = label;
    }

    isRemote(): boolean {
        return true;
    }

    toString() {
        return "RemoteRule[" + this._label + "]";
    }

}

export namespace RuleImpl {

    export function of(ruleProto: RuleProto) {
        return new RuleImpl(ruleProto.getLabel(), ruleProto.getWhen(), ruleProto.getThen());
    }

}
