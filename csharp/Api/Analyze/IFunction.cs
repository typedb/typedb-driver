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

using System.Collections.Generic;

namespace TypeDB.Driver.Api.Analyze
{
    /// <summary>
    /// Holds a representation of a function, and the result of type-inference for each variable.
    /// </summary>
    public interface IFunction
    {
        /// <summary>
        /// Gets the pipeline which forms the body of the function.
        /// </summary>
        IPipeline Body { get; }

        /// <summary>
        /// Gets the variables which are the arguments of the function.
        /// </summary>
        IEnumerable<IVariable> ArgumentVariables { get; }

        /// <summary>
        /// Gets the return operation of the function.
        /// </summary>
        IReturnOperation ReturnOperation { get; }

        /// <summary>
        /// Gets the type annotations for each argument of the function.
        /// </summary>
        IEnumerable<IVariableAnnotations> ArgumentAnnotations { get; }

        /// <summary>
        /// Gets the type annotations for each concept returned by the function.
        /// </summary>
        IEnumerable<IVariableAnnotations> ReturnAnnotations { get; }
    }

    /// <summary>
    /// A representation of the return operation of a function.
    /// </summary>
    public interface IReturnOperation
    {
        /// <summary>
        /// Gets the variant. One of: StreamReturn, SingleReturn, CheckReturn, ReduceReturn
        /// </summary>
        Pinvoke.ReturnOperationVariant Variant { get; }

        bool IsStream { get; }
        bool IsSingle { get; }
        bool IsCheck { get; }
        bool IsReduce { get; }

        IStreamReturn AsStream();
        ISingleReturn AsSingle();
        ICheckReturn AsCheck();
        IReduceReturn AsReduce();
    }

    /// <summary>
    /// Indicates the function returns a stream of concepts.
    /// </summary>
    public interface IStreamReturn : IReturnOperation
    {
        IEnumerable<IVariable> Variables { get; }
    }

    /// <summary>
    /// Indicates the function returns a single row of the specified variables.
    /// </summary>
    public interface ISingleReturn : IReturnOperation
    {
        IEnumerable<IVariable> Variables { get; }
        string Selector { get; }
    }

    /// <summary>
    /// Indicates the function returns a boolean.
    /// </summary>
    public interface ICheckReturn : IReturnOperation
    {
    }

    /// <summary>
    /// Indicates the function returns an aggregation over the rows in the body.
    /// </summary>
    public interface IReduceReturn : IReturnOperation
    {
        IEnumerable<IReducer> Reducers { get; }
    }
}
