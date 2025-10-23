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

package com.typedb.driver.analyze;

import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.stream.Stream;

import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_RETURN_OPERATION_CASTING;
import static com.typedb.driver.common.util.Objects.className;

public class Function extends NativeObject<com.typedb.driver.jni.Function> {

    protected Function(com.typedb.driver.jni.Function nativeObject) {
        super(nativeObject);
    }

    public Pipeline body() {
        return new Pipeline(com.typedb.driver.jni.typedb_driver.function_body(nativeObject));
    }

    public Stream<Variable> argument_variables() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.function_argument_variables(nativeObject)).stream().map(Variable::new);
    }

    public ReturnOperation return_operation() {
        return ReturnOperation.of(com.typedb.driver.jni.typedb_driver.function_return_operation(nativeObject));
    }

    public Stream<VariableAnnotations> argument_annotations() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.function_argument_annotations(nativeObject)).stream().map(VariableAnnotations::new);
    }

    public Stream<VariableAnnotations> return_annotations() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.function_return_annotations(nativeObject)).stream().map(VariableAnnotations::new);
    }

    public static abstract class ReturnOperation extends NativeObject<com.typedb.driver.jni.ReturnOperation> {

        protected ReturnOperation(com.typedb.driver.jni.ReturnOperation nativeObject) {
            super(nativeObject);
        }

        protected static ReturnOperation of(com.typedb.driver.jni.ReturnOperation nativeObject) {
            switch (com.typedb.driver.jni.typedb_driver.return_operation_variant(nativeObject)) {
                case StreamReturn:
                    return new Stream(nativeObject);
                case SingleReturn:
                    return new Single(nativeObject);
                case CheckReturn:
                    return new Check(nativeObject);
                case ReduceReturn:
                    return new Reduce(nativeObject);
                default:
                    throw new IllegalArgumentException("Unknown return operation variant");
            }
        }

        public com.typedb.driver.jni.ReturnOperationVariant variant() {
            return com.typedb.driver.jni.typedb_driver.return_operation_variant(nativeObject);
        }

        public ReturnOperation.Stream asStream() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(ReturnOperation.Stream.class));
        }

        public ReturnOperation.Single asSingle() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(ReturnOperation.Single.class));
        }

        public ReturnOperation.Check asCheck() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(ReturnOperation.Check.class));
        }

        public Reduce asReduce() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(Reduce.class));
        }

        public static class Stream extends ReturnOperation {
            private Stream(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public Stream asStream() {
                return this;
            }

            public java.util.stream.Stream<Variable> variables() {
                return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.return_operation_stream_variables(nativeObject)).stream().map(Variable::new);
            }
        }

        public static class Single extends ReturnOperation {
            private Single(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public Single asSingle() {
                return this;
            }

            public java.util.stream.Stream<Variable> variables() {
                return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.return_operation_single_variables(nativeObject)).stream().map(Variable::new);
            }

            public String selector() {
                return com.typedb.driver.jni.typedb_driver.return_operation_single_selector(nativeObject);
            }
        }

        public static class Check extends ReturnOperation {
            private Check(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public Check asCheck() {
                return this;
            }
        }

        public static class Reduce extends ReturnOperation {
            private Reduce(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public Reduce asReduce() {
                return this;
            }

            public java.util.stream.Stream<Reducer> reducers() {
                return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.return_operation_reducers(nativeObject)).stream().map(Reducer::new);
            }
        }
    }
}
