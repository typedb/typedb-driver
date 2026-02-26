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
    /// Representation of a stage in a pipeline.
    /// </summary>
    public interface IPipelineStage
    {
        /// <summary>
        /// Gets the pipeline stage variant.
        /// </summary>
        Pinvoke.PipelineStageVariant Variant { get; }

        bool IsMatch { get; }
        bool IsInsert { get; }
        bool IsPut { get; }
        bool IsUpdate { get; }
        bool IsDelete { get; }
        bool IsSelect { get; }
        bool IsSort { get; }
        bool IsRequire { get; }
        bool IsOffset { get; }
        bool IsLimit { get; }
        bool IsDistinct { get; }
        bool IsReduce { get; }

        IMatchStage AsMatch();
        IInsertStage AsInsert();
        IPutStage AsPut();
        IUpdateStage AsUpdate();
        IDeleteStage AsDelete();
        ISelectStage AsSelect();
        ISortStage AsSort();
        IRequireStage AsRequire();
        IOffsetStage AsOffset();
        ILimitStage AsLimit();
        IDistinctStage AsDistinct();
        IReduceStage AsReduce();
    }

    /// <summary>
    /// Represents a "match" stage: match block
    /// </summary>
    public interface IMatchStage : IPipelineStage
    {
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents an "insert" stage: insert block
    /// </summary>
    public interface IInsertStage : IPipelineStage
    {
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents a "put" stage: put block
    /// </summary>
    public interface IPutStage : IPipelineStage
    {
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents an "update" stage: update block
    /// </summary>
    public interface IUpdateStage : IPipelineStage
    {
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents a "delete" stage: delete block; deleted_variables;
    /// </summary>
    public interface IDeleteStage : IPipelineStage
    {
        IConjunctionID Block { get; }
        IEnumerable<IVariable> DeletedVariables { get; }
    }

    /// <summary>
    /// Represents a "select" stage: select variables
    /// </summary>
    public interface ISelectStage : IPipelineStage
    {
        IEnumerable<IVariable> Variables { get; }
    }

    /// <summary>
    /// Represents a "sort" stage: sort variables-and-order
    /// </summary>
    public interface ISortStage : IPipelineStage
    {
        IEnumerable<ISortVariable> Variables { get; }
    }

    /// <summary>
    /// A variable and its sort order.
    /// </summary>
    public interface ISortVariable
    {
        IVariable Variable { get; }
        Pinvoke.SortOrder Order { get; }
    }

    /// <summary>
    /// Represents a "require" stage: require variables
    /// </summary>
    public interface IRequireStage : IPipelineStage
    {
        IEnumerable<IVariable> Variables { get; }
    }

    /// <summary>
    /// Represents an "offset" stage: offset n
    /// </summary>
    public interface IOffsetStage : IPipelineStage
    {
        long Offset { get; }
    }

    /// <summary>
    /// Represents a "limit" stage: limit n
    /// </summary>
    public interface ILimitStage : IPipelineStage
    {
        long Limit { get; }
    }

    /// <summary>
    /// Represents a "distinct" stage.
    /// </summary>
    public interface IDistinctStage : IPipelineStage
    {
    }

    /// <summary>
    /// Represents a "reduce" stage: reduce reducers groupby groupby
    /// </summary>
    public interface IReduceStage : IPipelineStage
    {
        IEnumerable<IVariable> GroupBy { get; }
        IEnumerable<IReduceAssignment> ReducerAssignments { get; }
    }

    /// <summary>
    /// An assignment of a reducer to a variable.
    /// </summary>
    public interface IReduceAssignment
    {
        IVariable Assigned { get; }
        IReducer Reducer { get; }
    }
}
