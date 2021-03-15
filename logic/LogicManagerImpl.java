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

package grakn.client.logic;

import grakn.client.api.Transaction;
import grakn.client.api.logic.LogicManager;
import grakn.client.api.logic.Rule;
import grakn.client.common.Proto;
import grakn.protocol.LogicProto;
import grakn.protocol.TransactionProto;
import graql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public final class LogicManagerImpl implements LogicManager {

    private final Transaction.Extended transactionRPC;

    public LogicManagerImpl(Transaction.Extended transactionRPC) {
        this.transactionRPC = transactionRPC;
    }

    @Override
    @Nullable
    public Rule getRule(String label) {
        LogicProto.LogicManager.GetRule.Res res = execute(Proto.LogicManager.getRule(label)).getGetRuleRes();
        switch (res.getResCase()) {
            case RULE:
                return RuleImpl.of(res.getRule());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    @Override
    public Stream<RuleImpl> getRules() {
        return stream(Proto.LogicManager.getRules())
                .flatMap(res -> res.getGetRulesResPart().getRulesList().stream())
                .map(RuleImpl::of);
    }

    @Override
    public Rule putRule(String label, Pattern when, Pattern then) {
        LogicProto.LogicManager.Res res =
                execute(Proto.LogicManager.putRule(label, when.toString(), then.toString()));
        return RuleImpl.of(res.getPutRuleRes().getRule());
    }

    private LogicProto.LogicManager.Res execute(TransactionProto.Transaction.Req.Builder req) {
        return transactionRPC.execute(req).getLogicManagerRes();
    }

    private Stream<LogicProto.LogicManager.ResPart> stream(TransactionProto.Transaction.Req.Builder req) {
        return transactionRPC.stream(req).map(TransactionProto.Transaction.ResPart::getLogicManagerResPart);
    }
}
