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

import com.typedb.driver.jni.typedb_driver;

import java.util.stream.Stream;

public interface Constraint {
    com.typedb.driver.jni.ConstraintVariant variant();

    boolean isIsa();

    boolean isHas();

    boolean isLinks();

    boolean isSub();

    boolean isOwns();

    boolean isRelates();

    boolean isPlays();

    boolean isFunctionCall();

    boolean isExpression();

    boolean isIs();

    boolean isIid();

    boolean isComparison();

    boolean isKindOf();

    boolean isLabel();

    boolean isValue();

    boolean isOr();

    boolean isNot();

    boolean isTry();

    Isa asIsa();

    Has asHas();

    Links asLinks();

    Sub asSub();

    Owns asOwns();

    Relates asRelates();

    Plays asPlays();

    FunctionCall asFunctionCall();

    Expression asExpression();

    Is asIs();

    Iid asIid();

    Comparison asComparison();

    Kind asKindOf();

    Label asLabel();

    Value asValue();

    Or asOr();

    Not asNot();

    Try asTry();

    interface Isa extends Constraint {
        ConstraintVertex instance();

        ConstraintVertex type();

        com.typedb.driver.jni.ConstraintExactness exactness();
    }

    interface Has extends Constraint {
        ConstraintVertex owner();

        ConstraintVertex attribute();

        com.typedb.driver.jni.ConstraintExactness exactness();
    }

    interface Links extends Constraint {
        ConstraintVertex relation();

        ConstraintVertex player();

        ConstraintVertex role();

        com.typedb.driver.jni.ConstraintExactness exactness();
    }

    interface Sub extends Constraint {
        ConstraintVertex subtype();

        ConstraintVertex supertype();

        com.typedb.driver.jni.ConstraintExactness exactness();
    }

    interface Owns extends Constraint {
        ConstraintVertex owner();

        ConstraintVertex attribute();

        com.typedb.driver.jni.ConstraintExactness exactness();
    }

    interface Relates extends Constraint {
        ConstraintVertex relation();

        ConstraintVertex role();

        com.typedb.driver.jni.ConstraintExactness exactness();
    }

    interface Plays extends Constraint {
        ConstraintVertex player();

        ConstraintVertex role();

        com.typedb.driver.jni.ConstraintExactness exactness();
    }

    interface FunctionCall extends Constraint {
        String name();

        Stream<? extends ConstraintVertex> arguments();

        Stream<? extends ConstraintVertex> assigned();
    }

    interface Expression extends Constraint {
        String text();

        Stream<? extends ConstraintVertex> arguments();

        ConstraintVertex assigned();
    }

    interface Is extends Constraint {
        ConstraintVertex lhs();

        ConstraintVertex rhs();
    }

    interface Iid extends Constraint {
        ConstraintVertex variable();

        String iid();
    }

    interface Comparison extends Constraint {
        ConstraintVertex lhs();

        ConstraintVertex rhs();

        com.typedb.driver.jni.Comparator comparator();

        static String comparatorName(com.typedb.driver.jni.Comparator comparator) {
            return typedb_driver.comparator_get_name(comparator);
        }
    }

    interface Kind extends Constraint {
        com.typedb.driver.jni.Kind kind();

        ConstraintVertex type();
    }

    interface Label extends Constraint {
        ConstraintVertex variable();

        String label();
    }

    interface Value extends Constraint {
        ConstraintVertex attributeType();

        String valueType();
    }

    interface Or extends Constraint {
        Stream<? extends com.typedb.driver.jni.ConjunctionID> branches();
    }

    interface Not extends Constraint {
        com.typedb.driver.jni.ConjunctionID conjunction();
    }

    interface Try extends Constraint {
        com.typedb.driver.jni.ConjunctionID conjunction();
    }
}
