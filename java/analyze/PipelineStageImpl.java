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

import com.typedb.driver.api.analyze.PipelineStage;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.stream.Stream;

import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_STAGE_CASTING;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.common.util.Objects.className;

public abstract class PipelineStageImpl extends NativeObject<com.typedb.driver.jni.PipelineStage> implements PipelineStage {

    PipelineStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
        super(nativeObject);
    }

    public com.typedb.driver.jni.PipelineStageVariant getVariant() {
        return com.typedb.driver.jni.typedb_driver.pipeline_stage_variant(nativeObject);
    }

    @Override
    public boolean isMatch() {
        return false;
    }

    @Override
    public boolean isInsert() {
        return false;
    }

    @Override
    public boolean isPut() {
        return false;
    }

    @Override
    public boolean isUpdate() {
        return false;
    }

    @Override
    public boolean isDelete() {
        return false;
    }

    @Override
    public boolean isSelect() {
        return false;
    }

    @Override
    public boolean isSort() {
        return false;
    }

    @Override
    public boolean isRequire() {
        return false;
    }

    @Override
    public boolean isOffset() {
        return false;
    }

    @Override
    public boolean isLimit() {
        return false;
    }

    @Override
    public boolean isDistinct() {
        return false;
    }

    @Override
    public boolean isReduce() {
        return false;
    }

    @Override
    public MatchStageImpl asMatch() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(MatchStageImpl.class));
    }

    @Override
    public InsertStageImpl asInsert() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(InsertStageImpl.class));
    }

    @Override
    public PutStageImpl asPut() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(PutStageImpl.class));
    }

    @Override
    public UpdateStageImpl asUpdate() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(UpdateStageImpl.class));
    }

    @Override
    public DeleteStageImpl asDelete() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(DeleteStageImpl.class));
    }

    @Override
    public SelectStageImpl asSelect() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(SelectStageImpl.class));
    }

    @Override
    public SortStageImpl asSort() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(SortStageImpl.class));
    }

    @Override
    public RequireStageImpl asRequire() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(RequireStageImpl.class));
    }

    @Override
    public OffsetStageImpl asOffset() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(OffsetStageImpl.class));
    }

    @Override
    public LimitStageImpl asLimit() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(LimitStageImpl.class));
    }

    @Override
    public DistinctStageImpl asDistinct() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(DistinctStageImpl.class));
    }

    @Override
    public ReduceStageImpl asReduce() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(ReduceStageImpl.class));
    }

    public static PipelineStageImpl of(com.typedb.driver.jni.PipelineStage nativeObject) {
        switch (com.typedb.driver.jni.typedb_driver.pipeline_stage_variant(nativeObject)) {
            case Match:
                return new MatchStageImpl(nativeObject);
            case Insert:
                return new InsertStageImpl(nativeObject);
            case Put:
                return new PutStageImpl(nativeObject);
            case Update:
                return new UpdateStageImpl(nativeObject);
            case Delete:
                return new DeleteStageImpl(nativeObject);
            case Select:
                return new SelectStageImpl(nativeObject);
            case Sort:
                return new SortStageImpl(nativeObject);
            case Require:
                return new RequireStageImpl(nativeObject);
            case Offset:
                return new OffsetStageImpl(nativeObject);
            case Limit:
                return new LimitStageImpl(nativeObject);
            case Distinct:
                return new DistinctStageImpl(nativeObject);
            case Reduce:
                return new ReduceStageImpl(nativeObject);
            default:
                throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
        }
    }

    @Override
    public String toString() {
        return com.typedb.driver.jni.typedb_driver.pipeline_stage_to_string(nativeObject);
    }

    public static class MatchStageImpl extends PipelineStageImpl implements PipelineStage.MatchStage {
        MatchStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isMatch() {
            return true;
        }

        @Override
        public MatchStageImpl asMatch() {
            return this;
        }

        @Override
        public ConjunctionIDImpl block() {
            return new ConjunctionIDImpl(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class InsertStageImpl extends PipelineStageImpl implements PipelineStage.InsertStage {
        InsertStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isInsert() {
            return true;
        }

        @Override
        public InsertStageImpl asInsert() {
            return this;
        }

        @Override
        public ConjunctionIDImpl block() {
            return new ConjunctionIDImpl(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class PutStageImpl extends PipelineStageImpl implements PipelineStage.PutStage {
        PutStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isPut() {
            return true;
        }

        @Override
        public PutStageImpl asPut() {
            return this;
        }

        @Override
        public ConjunctionIDImpl block() {
            return new ConjunctionIDImpl(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class UpdateStageImpl extends PipelineStageImpl implements PipelineStage.UpdateStage {
        UpdateStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isUpdate() {
            return true;
        }

        @Override
        public UpdateStageImpl asUpdate() {
            return this;
        }

        @Override
        public ConjunctionIDImpl block() {
            return new ConjunctionIDImpl(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class DeleteStageImpl extends PipelineStageImpl implements PipelineStage.DeleteStage {
        DeleteStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isDelete() {
            return true;
        }

        @Override
        public DeleteStageImpl asDelete() {
            return this;
        }

        @Override
        public ConjunctionIDImpl block() {
            return new ConjunctionIDImpl(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }

        @Override
        public Stream<VariableImpl> deletedVariables() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.pipeline_stage_delete_get_deleted_variables(nativeObject)).stream().map(VariableImpl::new);
        }
    }

    public static class SelectStageImpl extends PipelineStageImpl implements PipelineStage.SelectStage {
        SelectStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isSelect() {
            return true;
        }

        @Override
        public SelectStageImpl asSelect() {
            return this;
        }

        @Override
        public Stream<VariableImpl> variables() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.pipeline_stage_select_get_variables(nativeObject)).stream().map(VariableImpl::new);
        }
    }

    public static class SortStageImpl extends PipelineStageImpl implements PipelineStage.SortStage {
        SortStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isSort() {
            return true;
        }

        @Override
        public SortStageImpl asSort() {
            return this;
        }

        @Override
        public Stream<SortVariableImpl> variables() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.pipeline_stage_sort_get_sort_variables(nativeObject)).stream().map(SortVariableImpl::new);
        }

        public static class SortVariableImpl extends NativeObject<com.typedb.driver.jni.SortVariable> implements PipelineStage.SortStage.SortVariable {

            SortVariableImpl(com.typedb.driver.jni.SortVariable nativeObject) {
                super(nativeObject);
            }

            @Override
            public VariableImpl variable() {
                return new VariableImpl(com.typedb.driver.jni.typedb_driver.sort_variable_get_variable(nativeObject));
            }

            @Override
            public com.typedb.driver.jni.SortOrder order() {
                return com.typedb.driver.jni.typedb_driver.sort_variable_get_order(nativeObject);
            }
        }
    }

    public static class RequireStageImpl extends PipelineStageImpl implements PipelineStage.RequireStage {
        RequireStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isRequire() {
            return true;
        }

        @Override
        public RequireStageImpl asRequire() {
            return this;
        }

        @Override
        public Stream<VariableImpl> variables() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.pipeline_stage_require_get_variables(nativeObject)).stream().map(VariableImpl::new);
        }
    }

    public static class OffsetStageImpl extends PipelineStageImpl implements PipelineStage.OffsetStage {
        OffsetStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isOffset() {
            return true;
        }

        @Override
        public OffsetStageImpl asOffset() {
            return this;
        }

        @Override
        public long offset() {
            return com.typedb.driver.jni.typedb_driver.pipeline_stage_offset_get_offset(nativeObject);
        }
    }

    public static class LimitStageImpl extends PipelineStageImpl implements PipelineStage.LimitStage {
        LimitStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isLimit() {
            return true;
        }

        @Override
        public LimitStageImpl asLimit() {
            return this;
        }

        @Override
        public long limit() {
            return com.typedb.driver.jni.typedb_driver.pipeline_stage_limit_get_limit(nativeObject);
        }
    }

    public static class DistinctStageImpl extends PipelineStageImpl implements PipelineStage.DistinctStage {
        DistinctStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public DistinctStageImpl asDistinct() {
            return this;
        }
    }

    public static class ReduceStageImpl extends PipelineStageImpl implements PipelineStage.ReduceStage {
        ReduceStageImpl(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isReduce() {
            return true;
        }

        @Override
        public ReduceStageImpl asReduce() {
            return this;
        }

        @Override
        public Stream<VariableImpl> groupBy() {
            return new NativeIterator<>(
                    com.typedb.driver.jni.typedb_driver.pipeline_stage_reduce_get_groupby(nativeObject)
            ).stream().map(VariableImpl::new);
        }

        @Override
        public Stream<ReduceAssignmentImpl> reducerAssignments() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.pipeline_stage_reduce_get_reducer_assignments(nativeObject))
                    .stream()
                    .map(ReduceAssignmentImpl::new);
        }

        public static class ReduceAssignmentImpl extends NativeObject<com.typedb.driver.jni.ReduceAssignment> implements PipelineStage.ReduceStage.ReduceAssignment {
            ReduceAssignmentImpl(com.typedb.driver.jni.ReduceAssignment nativeObject) {
                super(nativeObject);
            }

            @Override
            public VariableImpl assigned() {
                return new VariableImpl(com.typedb.driver.jni.typedb_driver.reduce_assignment_get_assigned(nativeObject));
            }

            @Override
            public ReducerImpl reducer() {
                return new ReducerImpl(com.typedb.driver.jni.typedb_driver.reduce_assignment_get_reducer(nativeObject));
            }
        }
    }
}
