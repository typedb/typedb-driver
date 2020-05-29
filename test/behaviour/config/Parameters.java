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
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class Parameters {

    @ParameterType("true|false")
    public Boolean bool(String bool) {
        return Boolean.parseBoolean(bool);
    }

    @ParameterType("[0-9]+")
    public Integer number(String number) {
        return Integer.parseInt(number);
    }

    @ParameterType("read|write")
    public GraknClient.Transaction.Type transaction_type(String type){
        return GraknClient.Transaction.Type.of(type);
    }

    @DataTableType
    public List<GraknClient.Transaction.Type> transaction_types(List<String> values) {
        List<GraknClient.Transaction.Type> typeList = new ArrayList<>();
        for (String value : values) {
            GraknClient.Transaction.Type type = GraknClient.Transaction.Type.of(value);
            assertNotNull(type);
            typeList.add(type);
        }

        return typeList;
    }
}
