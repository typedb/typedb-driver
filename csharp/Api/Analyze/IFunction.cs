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

        /// <summary>
        /// Checks if this is a stream return.
        /// </summary>
        bool IsStream { get; }

        /// <summary>
        /// Checks if this is a single return.
        /// </summary>
        bool IsSingle { get; }

        /// <summary>
        /// Checks if this is a check return.
        /// </summary>
        bool IsCheck { get; }

        /// <summary>
        /// Checks if this is a reduce return.
        /// </summary>
        bool IsReduce { get; }

        /// <summary>
        /// Casts this return operation to a stream return.
        /// </summary>
        IStreamReturn AsStream();

        /// <summary>
        /// Casts this return operation to a single return.
        /// </summary>
        ISingleReturn AsSingle();

        /// <summary>
        /// Casts this return operation to a check return.
        /// </summary>
        ICheckReturn AsCheck();

        /// <summary>
        /// Casts this return operation to a reduce return.
        /// </summary>
        IReduceReturn AsReduce();
    }

    /// <summary>
    /// Indicates the function returns a stream of concepts.
    /// </summary>
    public interface IStreamReturn : IReturnOperation
    {
        /// <summary>
        /// The variables in the returned row.
        /// </summary>
        IEnumerable<IVariable> Variables { get; }
    }

    /// <summary>
    /// Indicates the function returns a single row of the specified variables.
    /// </summary>
    public interface ISingleReturn : IReturnOperation
    {
        /// <summary>
        /// The variables in the returned row.
        /// </summary>
        IEnumerable<IVariable> Variables { get; }

        /// <summary>
        /// The selector that determines how the row is selected.
        /// </summary>
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
        /// <summary>
        /// The reducers used to compute the aggregations.
        /// </summary>
        IEnumerable<IReducer> Reducers { get; }
    }
}
