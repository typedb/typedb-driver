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

using System;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Analyze
{
    public class Function : NativeObjectWrapper<Pinvoke.Function>, IFunction
    {
        internal Function(Pinvoke.Function nativeObject)
            : base(nativeObject)
        {
        }

        public IPipeline Body =>
            new Pipeline(Pinvoke.typedb_driver.function_body(NativeObject));

        public IEnumerable<IVariable> ArgumentVariables =>
            new NativeEnumerable<Pinvoke.Variable>(
                Pinvoke.typedb_driver.function_argument_variables(NativeObject))
                .Select(v => new Variable(v));

        public IReturnOperation ReturnOperation =>
            ReturnOperationBase.Of(Pinvoke.typedb_driver.function_return_operation(NativeObject));

        public IEnumerable<IVariableAnnotations> ArgumentAnnotations =>
            new NativeEnumerable<Pinvoke.VariableAnnotations>(
                Pinvoke.typedb_driver.function_argument_annotations(NativeObject))
                .Select(va => new VariableAnnotations(va));

        public IEnumerable<IVariableAnnotations> ReturnAnnotations =>
            new NativeEnumerable<Pinvoke.VariableAnnotations>(
                Pinvoke.typedb_driver.function_return_annotations(NativeObject))
                .Select(va => new VariableAnnotations(va));
    }

    public abstract class ReturnOperationBase : NativeObjectWrapper<Pinvoke.ReturnOperation>, IReturnOperation
    {
        protected ReturnOperationBase(Pinvoke.ReturnOperation nativeObject)
            : base(nativeObject)
        {
        }

        internal static ReturnOperationBase Of(Pinvoke.ReturnOperation nativeObject)
        {
            var variant = Pinvoke.typedb_driver.return_operation_variant(nativeObject);
            switch (variant)
            {
                case Pinvoke.ReturnOperationVariant.StreamReturn:
                    return new StreamReturnImpl(nativeObject);
                case Pinvoke.ReturnOperationVariant.SingleReturn:
                    return new SingleReturnImpl(nativeObject);
                case Pinvoke.ReturnOperationVariant.CheckReturn:
                    return new CheckReturnImpl(nativeObject);
                case Pinvoke.ReturnOperationVariant.ReduceReturn:
                    return new ReduceReturnImpl(nativeObject);
                default:
                    throw new InvalidOperationException("Unknown return operation variant: " + variant);
            }
        }

        public Pinvoke.ReturnOperationVariant Variant =>
            Pinvoke.typedb_driver.return_operation_variant(NativeObject);

        public virtual bool IsStream => false;
        public virtual bool IsSingle => false;
        public virtual bool IsCheck => false;
        public virtual bool IsReduce => false;

        public virtual IStreamReturn AsStream() => throw InvalidCast("StreamReturn");
        public virtual ISingleReturn AsSingle() => throw InvalidCast("SingleReturn");
        public virtual ICheckReturn AsCheck() => throw InvalidCast("CheckReturn");
        public virtual IReduceReturn AsReduce() => throw InvalidCast("ReduceReturn");

        private InvalidOperationException InvalidCast(string targetType)
        {
            return new InvalidOperationException($"Cannot cast {GetType().Name} to {targetType}");
        }
    }

    public class StreamReturnImpl : ReturnOperationBase, IStreamReturn
    {
        internal StreamReturnImpl(Pinvoke.ReturnOperation nativeObject) : base(nativeObject) { }

        public override bool IsStream => true;
        public override IStreamReturn AsStream() => this;

        public IEnumerable<IVariable> Variables =>
            new NativeEnumerable<Pinvoke.Variable>(
                Pinvoke.typedb_driver.return_operation_stream_variables(NativeObject))
                .Select(v => new Variable(v));
    }

    public class SingleReturnImpl : ReturnOperationBase, ISingleReturn
    {
        internal SingleReturnImpl(Pinvoke.ReturnOperation nativeObject) : base(nativeObject) { }

        public override bool IsSingle => true;
        public override ISingleReturn AsSingle() => this;

        public IEnumerable<IVariable> Variables =>
            new NativeEnumerable<Pinvoke.Variable>(
                Pinvoke.typedb_driver.return_operation_single_variables(NativeObject))
                .Select(v => new Variable(v));

        public string Selector =>
            Pinvoke.typedb_driver.return_operation_single_selector(NativeObject);
    }

    public class CheckReturnImpl : ReturnOperationBase, ICheckReturn
    {
        internal CheckReturnImpl(Pinvoke.ReturnOperation nativeObject) : base(nativeObject) { }

        public override bool IsCheck => true;
        public override ICheckReturn AsCheck() => this;
    }

    public class ReduceReturnImpl : ReturnOperationBase, IReduceReturn
    {
        internal ReduceReturnImpl(Pinvoke.ReturnOperation nativeObject) : base(nativeObject) { }

        public override bool IsReduce => true;
        public override IReduceReturn AsReduce() => this;

        public IEnumerable<IReducer> Reducers =>
            new NativeEnumerable<Pinvoke.Reducer>(
                Pinvoke.typedb_driver.return_operation_reducers(NativeObject))
                .Select(r => new Reducer(r));
    }
}
