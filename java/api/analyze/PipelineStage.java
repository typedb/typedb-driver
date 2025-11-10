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

/**
 * Representation of a stage in a <code>Pipeline</code>.
 */
public interface PipelineStage {
    /**
     * @return the pipeline stage variant
     */
    com.typedb.driver.jni.PipelineStageVariant getVariant();

    // Type check methods
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

    // Conversion methods
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

    /**
     * Represents a "match" stage: match &lt;block&gt;
     * e.g. {@code match $f isa friendship, links (friend: $x, friend: $y)}
     */
    interface MatchStage extends PipelineStage {
        /**
         * The index into <code>Pipeline.conjunctions</code>
         */
        com.typedb.driver.jni.ConjunctionID block();
    }

    /**
     * Represents an "insert" stage: insert &lt;block&gt;
     * e.g. {@code insert $f isa friendship, links (friend: $x, friend: $y)}
     */
    interface InsertStage extends PipelineStage {
        /**
         * The index into <code>Pipeline.conjunctions</code>
         */
        com.typedb.driver.jni.ConjunctionID block();
    }

    /**
     * Represents a "put" stage: put &lt;block&gt;
     * e.g. {@code put $f isa friendship, links (friend: $x, friend: $y)}
     */
    interface PutStage extends PipelineStage {
        /**
         * The index into <code>Pipeline.conjunctions</code>
         */
        com.typedb.driver.jni.ConjunctionID block();
    }

    /**
     * Represents an "update" stage: update &lt;block&gt;
     * e.g. {@code update $owner has name "John"}
     */
    interface UpdateStage extends PipelineStage {
        /**
         * The index into <code>Pipeline.conjunctions</code>
         */
        com.typedb.driver.jni.ConjunctionID block();
    }

    /**
     * Represents a "delete" stage:
     * <pre>
     * delete
     *     &lt;block&gt;;
     *     &lt;deleted_variables&gt;;
     * </pre>
     * e.g.
     * <pre>
     * delete
     *     has $attribute of $owner; links ($player) of $relation;
     *     $deleted-instance;
     * </pre>
     */
    interface DeleteStage extends PipelineStage {
        /**
         * The index into <code>Pipeline.conjunctions</code>
         */
        com.typedb.driver.jni.ConjunctionID block();

        /**
         * The variables for which the unified concepts are to be deleted.
         */
        Stream<com.typedb.driver.jni.Variable> deletedVariables();
    }

    /**
     * Represents a "select" stage: select &lt;variables&gt;
     * e.g. {@code select $x, $y}
     */
    interface SelectStage extends PipelineStage {
        /**
         * The variables being selected
         */
        Stream<com.typedb.driver.jni.Variable> variables();
    }

    /**
     * Represents a "sort" stage: sort &lt;variables-and-order&gt;
     * e.g. {@code sort $x asc, $y desc}
     */
    interface SortStage extends PipelineStage {
        /**
         * The sort variables and their sort orders
         */
        Stream<? extends SortVariable> variables();

        /**
         * A variable and its sort order
         */
        interface SortVariable {
            /**
             * The variable to sort by
             */
            com.typedb.driver.jni.Variable variable();

            /**
             * The sort order (ascending or descending)
             */
            com.typedb.driver.jni.SortOrder order();
        }
    }

    /**
     * Represents a "require" stage: require &lt;variables&gt;
     * e.g. {@code require $x, $y}
     */
    interface RequireStage extends PipelineStage {
        /**
         * The variables that must be present in the result
         */
        Stream<com.typedb.driver.jni.Variable> variables();
    }

    /**
     * Represents an "offset" stage: offset &lt;offset&gt;
     * e.g. {@code offset 3}
     */
    interface OffsetStage extends PipelineStage {
        /**
         * The number of results to skip
         */
        long offset();
    }

    /**
     * Represents a "limit" stage: limit &lt;limit&gt;
     * e.g. {@code limit 5}
     */
    interface LimitStage extends PipelineStage {
        /**
         * The maximum number of results to return
         */
        long limit();
    }

    /**
     * Represents a "distinct" stage
     */
    interface DistinctStage extends PipelineStage {
    }

    /**
     * Represents a "reduce" stage:
     * <pre>
     * reduce &lt;reducers&gt; groupby &lt;groupby&gt;
     * </pre>
     * e.g.
     * <pre>
     * reduce $sum = sum($v), $count = count groupby $x, $y;
     * </pre>
     */
    interface ReduceStage extends PipelineStage {
        /**
         * The variables to group by
         */
        Stream<com.typedb.driver.jni.Variable> groupBy();

        /**
         * The reducer assignments
         */
        Stream<? extends ReduceAssignment> reducerAssignments();

        /**
         * An assignment of a reducer to a variable
         */
        interface ReduceAssignment {
            /**
             * The variable being assigned to
             */
            com.typedb.driver.jni.Variable assigned();

            /**
             * The reducer being applied
             */
            Reducer reducer();
        }
    }
}
