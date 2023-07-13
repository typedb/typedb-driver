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

package com.vaticle.typedb.client.api.query;

import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.client.api.answer.ConceptMapGroup;
import com.vaticle.typedb.client.api.answer.Numeric;
import com.vaticle.typedb.client.api.answer.NumericGroup;
import com.vaticle.typedb.client.api.logic.Explanation;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLMatch;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface QueryManager {
    @CheckReturnValue
    Stream<ConceptMap> match(TypeQLMatch query);

    @CheckReturnValue
    Stream<ConceptMap> match(TypeQLMatch query, TypeDBOptions options);

    @CheckReturnValue
    Stream<ConceptMap> match(String query);

    @CheckReturnValue
    Stream<ConceptMap> match(String query, TypeDBOptions options);

    @CheckReturnValue
    Numeric match(TypeQLMatch.Aggregate query);

    @CheckReturnValue
    Numeric match(TypeQLMatch.Aggregate query, TypeDBOptions options);

    @CheckReturnValue
    Numeric matchAggregate(String query);

    @CheckReturnValue
    Numeric matchAggregate(String query, TypeDBOptions options);

    @CheckReturnValue
    Stream<ConceptMapGroup> match(TypeQLMatch.Group query);

    @CheckReturnValue
    Stream<ConceptMapGroup> match(TypeQLMatch.Group query, TypeDBOptions options);

    @CheckReturnValue
    Stream<ConceptMapGroup> matchGroup(String query);

    @CheckReturnValue
    Stream<ConceptMapGroup> matchGroup(String query, TypeDBOptions options);

    @CheckReturnValue
    Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query);

    @CheckReturnValue
    Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query, TypeDBOptions options);

    @CheckReturnValue
    Stream<NumericGroup> matchGroupAggregate(String query);

    @CheckReturnValue
    Stream<NumericGroup> matchGroupAggregate(String query, TypeDBOptions options);

    Stream<ConceptMap> insert(TypeQLInsert query);

    Stream<ConceptMap> insert(TypeQLInsert query, TypeDBOptions options);

    Stream<ConceptMap> insert(String query);

    Stream<ConceptMap> insert(String query, TypeDBOptions options);

    void delete(TypeQLDelete query);

    void delete(TypeQLDelete query, TypeDBOptions options);

    void delete(String query);

    void delete(String query, TypeDBOptions options);

    Stream<ConceptMap> update(TypeQLUpdate query);

    Stream<ConceptMap> update(TypeQLUpdate query, TypeDBOptions options);

    Stream<ConceptMap> update(String query);

    Stream<ConceptMap> update(String query, TypeDBOptions options);

    void define(TypeQLDefine query);

    void define(TypeQLDefine query, TypeDBOptions options);

    void define(String query);

    void define(String query, TypeDBOptions options);

    void undefine(TypeQLUndefine query);

    void undefine(TypeQLUndefine query, TypeDBOptions options);

    void undefine(String query);

    void undefine(String query, TypeDBOptions options);

    @CheckReturnValue
    Stream<Explanation> explain(ConceptMap.Explainable explainable);

    @CheckReturnValue
    Stream<Explanation> explain(ConceptMap.Explainable explainable, TypeDBOptions options);
}
