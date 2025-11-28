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

import com.typedb.driver.api.analyze.Function;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.stream.Stream;

import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_RETURN_OPERATION_CASTING;
import static com.typedb.driver.common.util.Objects.className;

public class FunctionImpl extends NativeObject<com.typedb.driver.jni.Function> implements Function {

    protected FunctionImpl(com.typedb.driver.jni.Function nativeObject) {
        super(nativeObject);
    }

    public PipelineImpl body() {
        return new PipelineImpl(com.typedb.driver.jni.typedb_driver.function_body(nativeObject));
    }

    public Stream<VariableImpl> argument_variables() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.function_argument_variables(nativeObject)).stream().map(VariableImpl::new);
    }

    public ReturnOperationImpl return_operation() {
        return ReturnOperationImpl.of(com.typedb.driver.jni.typedb_driver.function_return_operation(nativeObject));
    }

    public Stream<VariableAnnotationsImpl> argument_annotations() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.function_argument_annotations(nativeObject)).stream().map(VariableAnnotationsImpl::new);
    }

    public Stream<VariableAnnotationsImpl> return_annotations() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.function_return_annotations(nativeObject)).stream().map(VariableAnnotationsImpl::new);
    }

    public static abstract class ReturnOperationImpl extends NativeObject<com.typedb.driver.jni.ReturnOperation> implements Function.ReturnOperation {

        protected ReturnOperationImpl(com.typedb.driver.jni.ReturnOperation nativeObject) {
            super(nativeObject);
        }

        protected static ReturnOperationImpl of(com.typedb.driver.jni.ReturnOperation nativeObject) {
            switch (com.typedb.driver.jni.typedb_driver.return_operation_variant(nativeObject)) {
                case StreamReturn:
                    return new StreamImpl(nativeObject);
                case SingleReturn:
                    return new SingleImpl(nativeObject);
                case CheckReturn:
                    return new CheckImpl(nativeObject);
                case ReduceReturn:
                    return new ReduceImpl(nativeObject);
                default:
                    throw new IllegalArgumentException("Unknown return operation variant");
            }
        }

        public com.typedb.driver.jni.ReturnOperationVariant variant() {
            return com.typedb.driver.jni.typedb_driver.return_operation_variant(nativeObject);
        }

        @Override
        public boolean isStream() {
            return false;
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public boolean isCheck() {
            return false;
        }

        @Override
        public boolean isReduce() {
            return false;
        }

        public StreamImpl asStream() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(StreamImpl.class));
        }

        public SingleImpl asSingle() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(SingleImpl.class));
        }

        public CheckImpl asCheck() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(CheckImpl.class));
        }

        public ReduceImpl asReduce() {
            throw new TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, className(this.getClass()), className(ReduceImpl.class));
        }

        public static class StreamImpl extends ReturnOperationImpl implements Function.ReturnOperation.Stream {
            private StreamImpl(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public StreamImpl asStream() {
                return this;
            }

            public java.util.stream.Stream<VariableImpl> variables() {
                return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.return_operation_stream_variables(nativeObject)).stream().map(VariableImpl::new);
            }
        }

        public static class SingleImpl extends ReturnOperationImpl implements Function.ReturnOperation.Single {
            private SingleImpl(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public boolean isStream() {
                return true;
            }

            @Override
            public boolean isSingle() {
                return true;
            }

            @Override
            public SingleImpl asSingle() {
                return this;
            }

            public java.util.stream.Stream<VariableImpl> variables() {
                return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.return_operation_single_variables(nativeObject)).stream().map(VariableImpl::new);
            }

            public String selector() {
                return com.typedb.driver.jni.typedb_driver.return_operation_single_selector(nativeObject);
            }
        }

        public static class CheckImpl extends ReturnOperationImpl implements Function.ReturnOperation.Check {
            private CheckImpl(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public boolean isCheck() {
                return true;
            }

            @Override
            public CheckImpl asCheck() {
                return this;
            }
        }

        public static class ReduceImpl extends ReturnOperationImpl implements Function.ReturnOperation.Reduce {
            private ReduceImpl(com.typedb.driver.jni.ReturnOperation nativeObject) {
                super(nativeObject);
            }

            @Override
            public boolean isReduce() {
                return true;
            }

            @Override
            public ReduceImpl asReduce() {
                return this;
            }

            public java.util.stream.Stream<ReducerImpl> reducers() {
                return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.return_operation_reducers(nativeObject)).stream().map(ReducerImpl::new);
            }
        }
    }
}
