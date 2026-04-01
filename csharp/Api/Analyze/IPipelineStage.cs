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

        /// <summary>
        /// Checks if this stage is a match stage.
        /// </summary>
        bool IsMatch { get; }

        /// <summary>
        /// Checks if this stage is an insert stage.
        /// </summary>
        bool IsInsert { get; }

        /// <summary>
        /// Checks if this stage is a put stage.
        /// </summary>
        bool IsPut { get; }

        /// <summary>
        /// Checks if this stage is an update stage.
        /// </summary>
        bool IsUpdate { get; }

        /// <summary>
        /// Checks if this stage is a delete stage.
        /// </summary>
        bool IsDelete { get; }

        /// <summary>
        /// Checks if this stage is a select stage.
        /// </summary>
        bool IsSelect { get; }

        /// <summary>
        /// Checks if this stage is a sort stage.
        /// </summary>
        bool IsSort { get; }

        /// <summary>
        /// Checks if this stage is a require stage.
        /// </summary>
        bool IsRequire { get; }

        /// <summary>
        /// Checks if this stage is an offset stage.
        /// </summary>
        bool IsOffset { get; }

        /// <summary>
        /// Checks if this stage is a limit stage.
        /// </summary>
        bool IsLimit { get; }

        /// <summary>
        /// Checks if this stage is a distinct stage.
        /// </summary>
        bool IsDistinct { get; }

        /// <summary>
        /// Checks if this stage is a reduce stage.
        /// </summary>
        bool IsReduce { get; }

        /// <summary>
        /// Casts this stage to a match stage.
        /// </summary>
        IMatchStage AsMatch();

        /// <summary>
        /// Casts this stage to an insert stage.
        /// </summary>
        IInsertStage AsInsert();

        /// <summary>
        /// Casts this stage to a put stage.
        /// </summary>
        IPutStage AsPut();

        /// <summary>
        /// Casts this stage to an update stage.
        /// </summary>
        IUpdateStage AsUpdate();

        /// <summary>
        /// Casts this stage to a delete stage.
        /// </summary>
        IDeleteStage AsDelete();

        /// <summary>
        /// Casts this stage to a select stage.
        /// </summary>
        ISelectStage AsSelect();

        /// <summary>
        /// Casts this stage to a sort stage.
        /// </summary>
        ISortStage AsSort();

        /// <summary>
        /// Casts this stage to a require stage.
        /// </summary>
        IRequireStage AsRequire();

        /// <summary>
        /// Casts this stage to an offset stage.
        /// </summary>
        IOffsetStage AsOffset();

        /// <summary>
        /// Casts this stage to a limit stage.
        /// </summary>
        ILimitStage AsLimit();

        /// <summary>
        /// Casts this stage to a distinct stage.
        /// </summary>
        IDistinctStage AsDistinct();

        /// <summary>
        /// Casts this stage to a reduce stage.
        /// </summary>
        IReduceStage AsReduce();
    }

    /// <summary>
    /// Represents a "match" stage: match block
    /// </summary>
    public interface IMatchStage : IPipelineStage
    {
        /// <summary>
        /// The index into the pipeline's conjunctions.
        /// </summary>
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents an "insert" stage: insert block
    /// </summary>
    public interface IInsertStage : IPipelineStage
    {
        /// <summary>
        /// The index into the pipeline's conjunctions.
        /// </summary>
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents a "put" stage: put block
    /// </summary>
    public interface IPutStage : IPipelineStage
    {
        /// <summary>
        /// The index into the pipeline's conjunctions.
        /// </summary>
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents an "update" stage: update block
    /// </summary>
    public interface IUpdateStage : IPipelineStage
    {
        /// <summary>
        /// The index into the pipeline's conjunctions.
        /// </summary>
        IConjunctionID Block { get; }
    }

    /// <summary>
    /// Represents a "delete" stage: delete block; deleted_variables;
    /// </summary>
    public interface IDeleteStage : IPipelineStage
    {
        /// <summary>
        /// The index into the pipeline's conjunctions.
        /// </summary>
        IConjunctionID Block { get; }

        /// <summary>
        /// The variables for which the unified concepts are to be deleted.
        /// </summary>
        IEnumerable<IVariable> DeletedVariables { get; }
    }

    /// <summary>
    /// Represents a "select" stage: select variables
    /// </summary>
    public interface ISelectStage : IPipelineStage
    {
        /// <summary>
        /// The variables being selected.
        /// </summary>
        IEnumerable<IVariable> Variables { get; }
    }

    /// <summary>
    /// Represents a "sort" stage: sort variables-and-order
    /// </summary>
    public interface ISortStage : IPipelineStage
    {
        /// <summary>
        /// The sort variables and their sort orders.
        /// </summary>
        IEnumerable<ISortVariable> Variables { get; }
    }

    /// <summary>
    /// A variable and its sort order.
    /// </summary>
    public interface ISortVariable
    {
        /// <summary>
        /// The variable to sort by.
        /// </summary>
        IVariable Variable { get; }

        /// <summary>
        /// The sort order (ascending or descending).
        /// </summary>
        Pinvoke.SortOrder Order { get; }
    }

    /// <summary>
    /// Represents a "require" stage: require variables
    /// </summary>
    public interface IRequireStage : IPipelineStage
    {
        /// <summary>
        /// The variables that must be present in the result.
        /// </summary>
        IEnumerable<IVariable> Variables { get; }
    }

    /// <summary>
    /// Represents an "offset" stage: offset n
    /// </summary>
    public interface IOffsetStage : IPipelineStage
    {
        /// <summary>
        /// The number of results to skip.
        /// </summary>
        long Offset { get; }
    }

    /// <summary>
    /// Represents a "limit" stage: limit n
    /// </summary>
    public interface ILimitStage : IPipelineStage
    {
        /// <summary>
        /// The maximum number of results to return.
        /// </summary>
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
        /// <summary>
        /// The variables to group by.
        /// </summary>
        IEnumerable<IVariable> GroupBy { get; }

        /// <summary>
        /// The reducer assignments.
        /// </summary>
        IEnumerable<IReduceAssignment> ReducerAssignments { get; }
    }

    /// <summary>
    /// An assignment of a reducer to a variable.
    /// </summary>
    public interface IReduceAssignment
    {
        /// <summary>
        /// The variable being assigned to.
        /// </summary>
        IVariable Assigned { get; }

        /// <summary>
        /// The reducer being applied.
        /// </summary>
        IReducer Reducer { get; }
    }
}
