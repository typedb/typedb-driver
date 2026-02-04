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
    public abstract class PipelineStage : NativeObjectWrapper<Pinvoke.PipelineStage>, IPipelineStage
    {
        protected PipelineStage(Pinvoke.PipelineStage nativeObject)
            : base(nativeObject)
        {
        }

        public static PipelineStage Of(Pinvoke.PipelineStage nativeObject)
        {
            var variant = Pinvoke.typedb_driver.pipeline_stage_variant(nativeObject);
            switch (variant)
            {
                case Pinvoke.PipelineStageVariant.Match:
                    return new MatchStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Insert:
                    return new InsertStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Put:
                    return new PutStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Update:
                    return new UpdateStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Delete:
                    return new DeleteStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Select:
                    return new SelectStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Sort:
                    return new SortStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Require:
                    return new RequireStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Offset:
                    return new OffsetStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Limit:
                    return new LimitStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Distinct:
                    return new DistinctStageImpl(nativeObject);
                case Pinvoke.PipelineStageVariant.Reduce:
                    return new ReduceStageImpl(nativeObject);
                default:
                    throw new InvalidOperationException("Unexpected pipeline stage variant: " + variant);
            }
        }

        public Pinvoke.PipelineStageVariant Variant
        {
            get { return Pinvoke.typedb_driver.pipeline_stage_variant(NativeObject); }
        }

        public virtual bool IsMatch => false;
        public virtual bool IsInsert => false;
        public virtual bool IsPut => false;
        public virtual bool IsUpdate => false;
        public virtual bool IsDelete => false;
        public virtual bool IsSelect => false;
        public virtual bool IsSort => false;
        public virtual bool IsRequire => false;
        public virtual bool IsOffset => false;
        public virtual bool IsLimit => false;
        public virtual bool IsDistinct => false;
        public virtual bool IsReduce => false;

        public virtual IMatchStage AsMatch() => throw InvalidCast("MatchStage");
        public virtual IInsertStage AsInsert() => throw InvalidCast("InsertStage");
        public virtual IPutStage AsPut() => throw InvalidCast("PutStage");
        public virtual IUpdateStage AsUpdate() => throw InvalidCast("UpdateStage");
        public virtual IDeleteStage AsDelete() => throw InvalidCast("DeleteStage");
        public virtual ISelectStage AsSelect() => throw InvalidCast("SelectStage");
        public virtual ISortStage AsSort() => throw InvalidCast("SortStage");
        public virtual IRequireStage AsRequire() => throw InvalidCast("RequireStage");
        public virtual IOffsetStage AsOffset() => throw InvalidCast("OffsetStage");
        public virtual ILimitStage AsLimit() => throw InvalidCast("LimitStage");
        public virtual IDistinctStage AsDistinct() => throw InvalidCast("DistinctStage");
        public virtual IReduceStage AsReduce() => throw InvalidCast("ReduceStage");

        private InvalidOperationException InvalidCast(string targetType)
        {
            return new InvalidOperationException($"Cannot cast {GetType().Name} to {targetType}");
        }

        public override string ToString()
        {
            return Pinvoke.typedb_driver.pipeline_stage_string_repr(NativeObject);
        }

        public class MatchStageImpl : PipelineStage, IMatchStage
        {
            internal MatchStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsMatch => true;
            public override IMatchStage AsMatch() => this;

            public IConjunctionID Block =>
                new ConjunctionID(Pinvoke.typedb_driver.pipeline_stage_get_block(NativeObject));
        }

        public class InsertStageImpl : PipelineStage, IInsertStage
        {
            internal InsertStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsInsert => true;
            public override IInsertStage AsInsert() => this;

            public IConjunctionID Block =>
                new ConjunctionID(Pinvoke.typedb_driver.pipeline_stage_get_block(NativeObject));
        }

        public class PutStageImpl : PipelineStage, IPutStage
        {
            internal PutStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsPut => true;
            public override IPutStage AsPut() => this;

            public IConjunctionID Block =>
                new ConjunctionID(Pinvoke.typedb_driver.pipeline_stage_get_block(NativeObject));
        }

        public class UpdateStageImpl : PipelineStage, IUpdateStage
        {
            internal UpdateStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsUpdate => true;
            public override IUpdateStage AsUpdate() => this;

            public IConjunctionID Block =>
                new ConjunctionID(Pinvoke.typedb_driver.pipeline_stage_get_block(NativeObject));
        }

        public class DeleteStageImpl : PipelineStage, IDeleteStage
        {
            internal DeleteStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsDelete => true;
            public override IDeleteStage AsDelete() => this;

            public IConjunctionID Block =>
                new ConjunctionID(Pinvoke.typedb_driver.pipeline_stage_get_block(NativeObject));

            public IEnumerable<IVariable> DeletedVariables =>
                new NativeEnumerable<Pinvoke.Variable>(
                    Pinvoke.typedb_driver.pipeline_stage_delete_get_deleted_variables(NativeObject))
                    .Select(v => new Variable(v));
        }

        public class SelectStageImpl : PipelineStage, ISelectStage
        {
            internal SelectStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsSelect => true;
            public override ISelectStage AsSelect() => this;

            public IEnumerable<IVariable> Variables =>
                new NativeEnumerable<Pinvoke.Variable>(
                    Pinvoke.typedb_driver.pipeline_stage_select_get_variables(NativeObject))
                    .Select(v => new Variable(v));
        }

        public class SortStageImpl : PipelineStage, ISortStage
        {
            internal SortStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsSort => true;
            public override ISortStage AsSort() => this;

            public IEnumerable<ISortVariable> Variables =>
                new NativeEnumerable<Pinvoke.SortVariable>(
                    Pinvoke.typedb_driver.pipeline_stage_sort_get_sort_variables(NativeObject))
                    .Select(sv => new SortVariableImpl(sv));
        }

        public class SortVariableImpl : NativeObjectWrapper<Pinvoke.SortVariable>, ISortVariable
        {
            internal SortVariableImpl(Pinvoke.SortVariable nativeObject)
                : base(nativeObject)
            {
            }

            public IVariable Variable =>
                new Variable(Pinvoke.typedb_driver.sort_variable_get_variable(NativeObject));

            public Pinvoke.SortOrder Order =>
                Pinvoke.typedb_driver.sort_variable_get_order(NativeObject);
        }

        public class RequireStageImpl : PipelineStage, IRequireStage
        {
            internal RequireStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsRequire => true;
            public override IRequireStage AsRequire() => this;

            public IEnumerable<IVariable> Variables =>
                new NativeEnumerable<Pinvoke.Variable>(
                    Pinvoke.typedb_driver.pipeline_stage_require_get_variables(NativeObject))
                    .Select(v => new Variable(v));
        }

        public class OffsetStageImpl : PipelineStage, IOffsetStage
        {
            internal OffsetStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsOffset => true;
            public override IOffsetStage AsOffset() => this;

            public long Offset => Pinvoke.typedb_driver.pipeline_stage_offset_get_offset(NativeObject);
        }

        public class LimitStageImpl : PipelineStage, ILimitStage
        {
            internal LimitStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsLimit => true;
            public override ILimitStage AsLimit() => this;

            public long Limit => Pinvoke.typedb_driver.pipeline_stage_limit_get_limit(NativeObject);
        }

        public class DistinctStageImpl : PipelineStage, IDistinctStage
        {
            internal DistinctStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsDistinct => true;
            public override IDistinctStage AsDistinct() => this;
        }

        public class ReduceStageImpl : PipelineStage, IReduceStage
        {
            internal ReduceStageImpl(Pinvoke.PipelineStage nativeObject) : base(nativeObject) { }

            public override bool IsReduce => true;
            public override IReduceStage AsReduce() => this;

            public IEnumerable<IVariable> GroupBy =>
                new NativeEnumerable<Pinvoke.Variable>(
                    Pinvoke.typedb_driver.pipeline_stage_reduce_get_groupby(NativeObject))
                    .Select(v => new Variable(v));

            public IEnumerable<IReduceAssignment> ReducerAssignments =>
                new NativeEnumerable<Pinvoke.ReduceAssignment>(
                    Pinvoke.typedb_driver.pipeline_stage_reduce_get_reducer_assignments(NativeObject))
                    .Select(ra => new ReduceAssignmentImpl(ra));
        }

        public class ReduceAssignmentImpl : NativeObjectWrapper<Pinvoke.ReduceAssignment>, IReduceAssignment
        {
            internal ReduceAssignmentImpl(Pinvoke.ReduceAssignment nativeObject)
                : base(nativeObject)
            {
            }

            public IVariable Assigned =>
                new Variable(Pinvoke.typedb_driver.reduce_assignment_get_assigned(NativeObject));

            public IReducer Reducer =>
                new Reducer(Pinvoke.typedb_driver.reduce_assignment_get_reducer(NativeObject));
        }
    }
}
