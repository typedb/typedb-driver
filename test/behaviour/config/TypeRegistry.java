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

package grakn.client.test.behaviour.config;

import grakn.client.GraknClient;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.statement.Variable;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.Transformer;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;

public class TypeRegistry implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(io.cucumber.core.api.TypeRegistry typeRegistry) {
        typeRegistry.defineParameterType(
                new ParameterType<>("boolean", "true|false",
                                    Boolean.class, Boolean::parseBoolean)
        );

        typeRegistry.defineParameterType(
                new ParameterType<>("number", "[0-9]+",
                                    Integer.class, (Transformer<Integer>) Integer::parseInt)
        );

        typeRegistry.defineParameterType(
                new ParameterType<>("transaction-type", "read|write",
                                    GraknClient.Transaction.Type.class, (String type) -> {
                    int id = type.equals("read") ? 0 : 1;
                    return GraknClient.Transaction.Type.of(id);
                })
        );

        typeRegistry.defineParameterType(new ParameterType<>("patterns", "{}", List.class, Graql::parsePatternList));

        // anything followed by a semicolon can be interpreted as the vars after a `get`
        typeRegistry.defineParameterType(new ParameterType<>("vars", "/.*/;", List.class, (String vars) ->
                Arrays.stream(vars.split(","))
                        .map(Variable::new)
                        .collect(Collectors.toList())
                )
        );

        typeRegistry.defineParameterType(new ParameterType<>("graql-file", ".*.gql", File.class, (String file) ->
            Paths.get(file).toFile()
        ));
    }
}