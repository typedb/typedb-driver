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

/**
 * Holds a representation of a function, and the result of type-inference for each variable.
 */
public interface Function {
    /**
     * Gets the pipeline which forms the body of the function.
     *
     * @return the function body as a pipeline
     */
    Pipeline body();

    /**
     * Gets the variables which are the arguments of the function.
     *
     * @return stream of argument variables
     */
    Stream<com.typedb.driver.jni.Variable> argument_variables();

    /**
     * Gets the return operation of the function.
     *
     * @return the return operation
     */
    ReturnOperation return_operation();

    /**
     * Gets the type annotations for each argument of the function.
     *
     * @return stream of argument type annotations
     */
    Stream<? extends VariableAnnotations> argument_annotations();

    /**
     * Gets the type annotations for each concept returned by the function.
     *
     * @return stream of return type annotations
     */
    Stream<? extends VariableAnnotations> return_annotations();

    /**
     * A representation of the return operation of a function.
     */
    interface ReturnOperation {
        /**
         * Gets the variant of this return operation.
         * One of: <code>StreamReturn, SingleReturn, CheckReturn, ReduceReturn</code>
         *
         * @return the return operation variant
         */
        com.typedb.driver.jni.ReturnOperationVariant variant();

        boolean isStream();

        boolean isSingle();

        boolean isCheck();

        boolean isReduce();

        /**
         * Down-casts this ReturnOperation to a <code>Stream</code> return operation.
         *
         * @return this operation as a Stream
         * @throws IllegalStateException if this is not a Stream return operation
         */
        Stream asStream();

        /**
         * Down-casts this ReturnOperation to a <code>Single</code> return operation.
         *
         * @return this operation as a <code>Single</code>
         * @throws IllegalStateException if this is not a Single return operation
         */
        Single asSingle();

        /**
         * Down-casts this ReturnOperation to a <code>Check</code> return operation.
         *
         * @return this operation as a <code>Check</code>
         * @throws IllegalStateException if this is not a Check return operation
         */
        Check asCheck();

        /**
         * Down-casts this ReturnOperation to a Reduce  return operation.
         *
         * @return this operation as a <code>Reduce</code>
         */
        Reduce asReduce();

        /**
         * Indicates the function returns a stream of concepts.
         * e.g. <code>return { $x, $y };</code>
         */
        interface Stream extends ReturnOperation {
            /**
             * Gets the variables in the returned row.
             *
             * @return stream of variables
             */
            java.util.stream.Stream<com.typedb.driver.jni.Variable> variables();
        }

        /**
         * Indicates the function returns a single row of the specified variables.
         * e.g. <code>return first $x, $y;</code>
         */
        interface Single extends ReturnOperation {
            /**
             * Gets the variables in the returned row.
             *
             * @return stream of variables
             */
            java.util.stream.Stream<com.typedb.driver.jni.Variable> variables();

            /**
             * Gets the selector that determines how the operation selects the row.
             *
             * @return the selector string
             */
            String selector();
        }

        /**
         * Indicates the function returns a boolean - true if the body had answers, false otherwise.
         * e.g. <code>return check;</code>
         */
        interface Check extends ReturnOperation {
        }

        /**
         * Indicates the function returns an aggregation over the rows in the body.
         * e.g. <code>return count($x), sum($y);</code>
         */
        interface Reduce extends ReturnOperation {
            @Override
            default Reduce asReduce() {
                return this;
            }

            /**
             * Gets the reducers used to compute the aggregations.
             *
             * @return stream of reducers
             */
            java.util.stream.Stream<? extends Reducer> reducers();
        }
    }
}
