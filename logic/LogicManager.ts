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

import { TransactionRPC, Rule, RuleImpl, Stream } from "../dependencies_internal"
import LogicProto from "grakn-protocol/protobuf/logic_pb";
import TransactionProto from "grakn-protocol/protobuf/transaction_pb";

export class LogicManager {
    private readonly _transactionRPC: TransactionRPC;

    constructor (transactionRPC: TransactionRPC) {
        this._transactionRPC = transactionRPC;
    }

    async putRule(label: string, when: string, then: string): Promise<Rule> {
        const req = new LogicProto.LogicManager.Req()
            .setPutRuleReq(new LogicProto.LogicManager.PutRule.Req()
                .setLabel(label)
                .setWhen(when)
                .setThen(then));
        const res = await this.execute(req);
        return RuleImpl.of(res.getPutRuleRes().getRule());
    }

    async getRule(label: string): Promise<Rule> {
        const req = new LogicProto.LogicManager.Req()
            .setGetRuleReq(new LogicProto.LogicManager.GetRule.Req().setLabel(label));
        const res = await this.execute(req);
        if (res.getGetRuleRes().getResCase() === LogicProto.LogicManager.GetRule.Res.ResCase.RULE) return RuleImpl.of(res.getGetRuleRes().getRule());
        return null;
    }

    getRules(): Stream<RuleImpl> {
        const method = new LogicProto.LogicManager.Req().setGetRulesReq(new LogicProto.LogicManager.GetRules.Req());
        return this.ruleStream(method, res => res.getGetRulesRes().getRulesList());
    }

    private async execute(logicManagerReq: LogicProto.LogicManager.Req): Promise<LogicProto.LogicManager.Res> {
        const transactionReq = new TransactionProto.Transaction.Req()
            .setLogicManagerReq(logicManagerReq);
        return await this._transactionRPC.execute(transactionReq, res => res.getLogicManagerRes());
    }

    private ruleStream(method: LogicProto.LogicManager.Req, ruleListGetter: (res: LogicProto.LogicManager.Res) => LogicProto.Rule[]): Stream<RuleImpl> {
        const request = new TransactionProto.Transaction.Req().setLogicManagerReq(method);
        return this._transactionRPC.stream(request, res => ruleListGetter(res.getLogicManagerRes()).map(RuleImpl.of));
    }
}
