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

import com.typedb.driver.api.analyze.Variable;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.jni.typedb_driver;

public class VariableImpl extends NativeObject<com.typedb.driver.jni.Variable> implements Variable {
    VariableImpl(com.typedb.driver.jni.Variable nativeObject) {
        super(nativeObject);
    }

    private long id() {
        return com.typedb.driver.jni.typedb_driver.variable_id_as_u32(nativeObject);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VariableImpl that = (VariableImpl) obj;
        return this.id() == that.id();
    }

    @Override
    public String toString() {
        return typedb_driver.variable_string_repr(nativeObject);
    }
}
