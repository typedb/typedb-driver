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

package com.typedb.driver.api.analyze;

import java.util.stream.Stream;

public interface Function {
    Pipeline body();

    Stream<com.typedb.driver.jni.Variable> argument_variables();

    ReturnOperation return_operation();

    Stream<? extends VariableAnnotations> argument_annotations();

    Stream<? extends VariableAnnotations> return_annotations();

    interface ReturnOperation {
        com.typedb.driver.jni.ReturnOperationVariant variant();

        Stream asStream();

        Single asSingle();

        Check asCheck();

        Reduce asReduce();

        interface Stream extends ReturnOperation {
            java.util.stream.Stream<com.typedb.driver.jni.Variable> variables();
        }

        interface Single extends ReturnOperation {
            java.util.stream.Stream<com.typedb.driver.jni.Variable> variables();

            String selector();
        }

        interface Check extends ReturnOperation {
        }

        interface Reduce extends ReturnOperation {
            @Override
            default Reduce asReduce() {
                return this;
            }

            java.util.stream.Stream<? extends Reducer> reducers();
        }
    }
}
