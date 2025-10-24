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

package com.typedb.driver.api.analyze;

import java.util.stream.Stream;

public interface PipelineStage {
    com.typedb.driver.jni.PipelineStageVariant getVariant();

    boolean isMatch();

    boolean isInsert();

    boolean isPut();

    boolean isUpdate();

    boolean isDelete();

    boolean isSelect();

    boolean isSort();

    boolean isRequire();

    boolean isOffset();

    boolean isLimit();

    boolean isDistinct();

    boolean isReduce();

    MatchStage asMatch();

    InsertStage asInsert();

    PutStage asPut();

    UpdateStage asUpdate();

    DeleteStage asDelete();

    SelectStage asSelect();

    SortStage asSort();

    RequireStage asRequire();

    OffsetStage asOffset();

    LimitStage asLimit();

    DistinctStage asDistinct();

    ReduceStage asReduce();

    interface MatchStage extends PipelineStage {
        com.typedb.driver.jni.ConjunctionID block();
    }

    interface InsertStage extends PipelineStage {
        com.typedb.driver.jni.ConjunctionID block();
    }

    interface PutStage extends PipelineStage {
        com.typedb.driver.jni.ConjunctionID block();
    }

    interface UpdateStage extends PipelineStage {
        com.typedb.driver.jni.ConjunctionID block();
    }

    interface DeleteStage extends PipelineStage {
        com.typedb.driver.jni.ConjunctionID block();

        Stream<com.typedb.driver.jni.Variable> deletedVariables();
    }

    interface SelectStage extends PipelineStage {
        Stream<com.typedb.driver.jni.Variable> variables();
    }

    interface SortStage extends PipelineStage {
        Stream<? extends SortVariable> variables();

        interface SortVariable {
            com.typedb.driver.jni.Variable variable();

            com.typedb.driver.jni.SortOrder order();
        }
    }

    interface RequireStage extends PipelineStage {
        Stream<com.typedb.driver.jni.Variable> variables();
    }

    interface OffsetStage extends PipelineStage {
        long offset();
    }

    interface LimitStage extends PipelineStage {
        long limit();
    }

    interface DistinctStage extends PipelineStage {
    }

    interface ReduceStage extends PipelineStage {
        Stream<com.typedb.driver.jni.Variable> groupBy();

        Stream<? extends ReduceAssignment> reducerAssignments();

        interface ReduceAssignment {
            com.typedb.driver.jni.Variable assigned();

            Reducer reducer();
        }
    }
}
