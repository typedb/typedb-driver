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

import grakn.client.logic.impl.RuleImpl;
import grakn.client.rpc.TransactionRPC;
import grakn.protocol.LogicProto;
import grakn.protocol.TransactionProto;
import graql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.tracing.TracingProtoBuilder.tracingData;

public final class LogicManager {

    private final TransactionRPC transactionRPC;

    public LogicManager(TransactionRPC transactionRPC) {
        this.transactionRPC = transactionRPC;
    }

    public Rule putRule(String label, Pattern when, Pattern then) {
        LogicProto.LogicManager.Req req = LogicProto.LogicManager.Req.newBuilder()
                .setPutRuleReq(LogicProto.LogicManager.PutRule.Req.newBuilder()
                                       .setLabel(label)
                                       .setWhen(when.toString())
                                       .setThen(then.toString())).build();
        LogicProto.LogicManager.Res res = execute(req);
        return RuleImpl.of(res.getPutRuleRes().getRule());
    }

    @Nullable
    @CheckReturnValue
    public Rule getRule(String label) {
        LogicProto.LogicManager.Req req = LogicProto.LogicManager.Req.newBuilder()
                .setGetRuleReq(LogicProto.LogicManager.GetRule.Req.newBuilder().setLabel(label)).build();

        LogicProto.LogicManager.Res response = execute(req);
        switch (response.getGetRuleRes().getResCase()) {
            case RULE:
                return RuleImpl.of(response.getGetRuleRes().getRule());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    @CheckReturnValue
    public Stream<RuleImpl> getRules() {
        LogicProto.LogicManager.Req.Builder method = LogicProto.LogicManager.Req.newBuilder()
                .setGetRulesReq(LogicProto.LogicManager.GetRules.Req.getDefaultInstance());
        return ruleStream(method, res -> res.getGetRulesRes().getRulesList());
    }

    private LogicProto.LogicManager.Res execute(LogicProto.LogicManager.Req request) {
        TransactionProto.Transaction.Req.Builder req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setLogicManagerReq(request);
        return transactionRPC.execute(req).getLogicManagerRes();
    }

    private Stream<RuleImpl> ruleStream(LogicProto.LogicManager.Req.Builder method, Function<LogicProto.LogicManager.Res, List<LogicProto.Rule>> ruleListGetter) {
        TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setLogicManagerReq(method);
        return transactionRPC.stream(request, res -> ruleListGetter.apply(res.getLogicManagerRes()).stream().map(RuleImpl::of));
    }
}
