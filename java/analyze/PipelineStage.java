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

import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_CONSTRAINT_CASTING;
import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_STAGE_CASTING;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.common.util.Objects.className;

public abstract class PipelineStage extends NativeObject<com.typedb.driver.jni.PipelineStage> {

    PipelineStage(com.typedb.driver.jni.PipelineStage nativeObject) {
        super(nativeObject);
    }


    public com.typedb.driver.jni.PipelineStageVariant getVariant() {
        return com.typedb.driver.jni.typedb_driver.pipeline_stage_variant(nativeObject);
    }

    public boolean isMatch() {
        return false;
    }

    public boolean isInsert() {
        return false;
    }

    public boolean isPut() {
        return false;
    }

    public boolean isUpdate() {
        return false;
    }

    public boolean isDelete() {
        return false;
    }

    public boolean isSelect() {
        return false;
    }

    public boolean isSort() {
        return false;
    }

    public boolean isRequire() {
        return false;
    }

    public boolean isOffset() {
        return false;
    }

    public boolean isLimit() {
        return false;
    }

    public boolean isDistinct() {
        return false;
    }

    public boolean isReduce() {
        return false;
    }


    public MatchStage asMatch() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(MatchStage.class));
    }

    public InsertStage asInsert() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(InsertStage.class));
    }

    public PutStage asPut() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(PutStage.class));
    }

    public UpdateStage asUpdate() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(UpdateStage.class));
    }

    public DeleteStage asDelete() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(DeleteStage.class));
    }

    public SelectStage asSelect() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(SelectStage.class));
    }

    public SortStage asSort() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(SortStage.class));
    }

    public RequireStage asRequire() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(RequireStage.class));
    }

    public OffsetStage asOffset() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(OffsetStage.class));
    }

    public LimitStage asLimit() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(LimitStage.class));
    }

    public DistinctStage asDistinct() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(DistinctStage.class));
    }

    public ReduceStage asReduce() {
        throw new TypeDBDriverException(INVALID_STAGE_CASTING, className(this.getClass()), className(ReduceStage.class));
    }

    public static PipelineStage of(com.typedb.driver.jni.PipelineStage nativeObject) {
        switch (com.typedb.driver.jni.typedb_driver.pipeline_stage_variant(nativeObject)) {
            case Match:
                return new MatchStage(nativeObject);
            case Insert:
                return new InsertStage(nativeObject);
            case Put:
                return new PutStage(nativeObject);
            case Update:
                return new UpdateStage(nativeObject);
            case Delete:
                return new DeleteStage(nativeObject);
            case Select:
                return new SelectStage(nativeObject);
            case Sort:
                return new SortStage(nativeObject);
            case Require:
                return new RequireStage(nativeObject);
            case Offset:
                return new OffsetStage(nativeObject);
            case Limit:
                return new LimitStage(nativeObject);
            case Distinct:
                return new DistinctStage(nativeObject);
            case Reduce:
                return new ReduceStage(nativeObject);
            default:
                throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
        }
    }

    public static class MatchStage extends PipelineStage {

        MatchStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isMatch() {
            return true;
        }

        @Override
        public MatchStage asMatch() {
            return this;
        }

        public ConjunctionID block() {
            return new ConjunctionID(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class InsertStage extends PipelineStage {

        InsertStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isInsert() {
            return true;
        }

        @Override
        public InsertStage asInsert() {
            return this;
        }

        public ConjunctionID block() {
            return new ConjunctionID(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class PutStage extends PipelineStage {
        PutStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isPut() {
            return true;
        }

        @Override
        public PutStage asPut() {
            return this;
        }

        public ConjunctionID block() {
            return new ConjunctionID(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class UpdateStage extends PipelineStage {
        UpdateStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isUpdate() {
            return true;
        }

        @Override
        public UpdateStage asUpdate() {
            return this;
        }

        public ConjunctionID block() {
            return new ConjunctionID(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }
    }

    public static class DeleteStage extends PipelineStage {
        DeleteStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isDelete() {
            return true;
        }

        @Override
        public DeleteStage asDelete() {
            return this;
        }

        public ConjunctionID block() {
            return new ConjunctionID(com.typedb.driver.jni.typedb_driver.pipeline_stage_get_block(nativeObject));
        }

        public Stream<Variable> deletedVariables() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.pipeline_stage_delete_get_deleted_variables(nativeObject)).stream().map(Variable::new);
        }
    }

    public static class SelectStage extends PipelineStage {
        SelectStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isSelect() {
            return true;
        }

        @Override
        public SelectStage asSelect() {
            return this;
        }


        public Stream<Variable> variables() {
            return new NativeIterator<com.typedb.driver.jni.Variable>(com.typedb.driver.jni.typedb_driver.pipeline_stage_select_get_variables(nativeObject)).stream().map(Variable::new);
        }
    }

    public static class SortStage extends PipelineStage {
        SortStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isSort() {
            return true;
        }

        @Override
        public SortStage asSort() {
            return this;
        }

        public Stream<SortVariable> variables() {
            return new NativeIterator<com.typedb.driver.jni.SortVariable>(
                    com.typedb.driver.jni.typedb_driver.pipeline_stage_sort_get_sort_variables(nativeObject)
            ).stream().map(SortVariable::new);
        }

        public static class SortVariable extends NativeObject<com.typedb.driver.jni.SortVariable> {

            protected SortVariable(com.typedb.driver.jni.SortVariable nativeObject) {
                super(nativeObject);
            }

            public Variable variable() {
                return new Variable(com.typedb.driver.jni.typedb_driver.sort_variable_get_variable(nativeObject));
            }

            public com.typedb.driver.jni.SortOrder order() {
                return com.typedb.driver.jni.typedb_driver.sort_variable_get_order(nativeObject);
            }
        }
    }

    public static class RequireStage extends PipelineStage {
        RequireStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isRequire() {
            return true;
        }

        @Override
        public RequireStage asRequire() {
            return this;
        }

        public Stream<Variable> variables() {
            return new NativeIterator<com.typedb.driver.jni.Variable>(com.typedb.driver.jni.typedb_driver.pipeline_stage_require_get_variables(nativeObject)).stream().map(Variable::new);
        }
    }

    public static class OffsetStage extends PipelineStage {
        OffsetStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isOffset() {
            return true;
        }

        @Override
        public OffsetStage asOffset() {
            return this;
        }

        public long offset() {
            return com.typedb.driver.jni.typedb_driver.pipeline_stage_offset_get_offset(nativeObject);
        }
    }

    public static class LimitStage extends PipelineStage {
        LimitStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isLimit() {
            return true;
        }

        @Override
        public LimitStage asLimit() {
            return this;
        }


        public long limit() {
            return com.typedb.driver.jni.typedb_driver.pipeline_stage_limit_get_limit(nativeObject);
        }
    }

    public static class DistinctStage extends PipelineStage {
        DistinctStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public DistinctStage asDistinct() {
            return this;
        }
    }

    public static class ReduceStage extends PipelineStage {
        ReduceStage(com.typedb.driver.jni.PipelineStage nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isReduce() {
            return true;
        }

        @Override
        public ReduceStage asReduce() {
            return this;
        }

        public Stream<Variable> groupBy() {
            return new NativeIterator<com.typedb.driver.jni.Variable>(
                    com.typedb.driver.jni.typedb_driver.pipeline_stage_reduce_get_groupby(nativeObject)
            ).stream().map(Variable::new);
        }

        public Stream<ReduceAssignment> reducerAssignments() {
            return new NativeIterator<com.typedb.driver.jni.ReduceAssignment>(
                    com.typedb.driver.jni.typedb_driver.pipeline_stage_reduce_get_reducer_assignments(nativeObject)
            ).stream().map(ReduceAssignment::new);
        }

        public static class ReduceAssignment extends NativeObject<com.typedb.driver.jni.ReduceAssignment> {

            protected ReduceAssignment(com.typedb.driver.jni.ReduceAssignment nativeObject) {
                super(nativeObject);
            }

            public Variable assigned() {
                return new Variable(com.typedb.driver.jni.typedb_driver.reduce_assignment_get_assigned(nativeObject));
            }

            public Reducer reducer() {
                return new Reducer(com.typedb.driver.jni.typedb_driver.reduce_assignment_get_reducer(nativeObject));
            }
        }
    }
}
