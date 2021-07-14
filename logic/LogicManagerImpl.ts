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

import { LogicManager as LogicProto } from "typedb-protocol/common/logic_pb";
import { Transaction } from "typedb-protocol/common/transaction_pb";
import { TypeDBTransaction } from "../api/connection/TypeDBTransaction";
import { LogicManager } from "../api/logic/LogicManager";
import { Rule } from "../api/logic/Rule";
import { RequestBuilder } from "../common/rpc/RequestBuilder";
import { Stream } from "../common/util/Stream";
import { RuleImpl } from "./RuleImpl";

export class LogicManagerImpl implements LogicManager {
    private _transaction: TypeDBTransaction.Extended;

    constructor(transaction: TypeDBTransaction.Extended) {
        this._transaction = transaction;
    }

    public async getRule(label: string): Promise<Rule | undefined> {
        const request = RequestBuilder.LogicManager.getRuleReq(label);
        const response = await this.execute(request);
        const ruleResponse = response.getGetRuleRes();
        switch (ruleResponse.getResCase()) {
            case LogicProto.GetRule.Res.ResCase.RULE:
                return RuleImpl.of(ruleResponse.getRule());
            case LogicProto.GetRule.Res.ResCase.RES_NOT_SET:
            default:
                return null;
        }
    }

    public getRules(): Stream<Rule> {
        const request = RequestBuilder.LogicManager.getRulesReq();
        return this.stream(request).flatMap((resPart) =>
            Stream.array(resPart.getGetRulesResPart().getRulesList()).map((ruleProto) => RuleImpl.of(ruleProto))
        );
    }

    public async putRule(label: string, when: string, then: string): Promise<Rule> {
        const request = RequestBuilder.LogicManager.putRuleReq(label, when, then);
        const response = await this.execute(request);
        const ruleResponse = response.getPutRuleRes();
        return RuleImpl.of(ruleResponse.getRule());
    }

    private execute(request: Transaction.Req) {
        return this._transaction.rpcExecute(request).then((res) => res.getLogicManagerRes());
    }

    private stream(request: Transaction.Req) {
        return this._transaction.rpcStream(request).map((res) => res.getLogicManagerResPart());
    }

}
