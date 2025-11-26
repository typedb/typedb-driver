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

import com.typedb.driver.api.analyze.NamedRole;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.jni.typedb_driver;

public class NamedRoleImpl extends NativeObject<com.typedb.driver.jni.NamedRole> implements NamedRole {
    NamedRoleImpl(com.typedb.driver.jni.NamedRole nativeObject) {
        super(nativeObject);
    }

    private long id() {
        return com.typedb.driver.jni.typedb_driver.named_role_as_u32(nativeObject);
    }

    public VariableImpl variable() {
        return new VariableImpl(typedb_driver.named_role_get_variable(nativeObject));
    }

    public String name() {
        return typedb_driver.named_role_get_name(nativeObject);
    }

    @Override
    public int hashCode() {
        return variable().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NamedRoleImpl that = (NamedRoleImpl) obj;
        return this.id() == that.id();
    }

    @Override
    public String toString() {
        return typedb_driver.named_role_to_string(nativeObject);
    }
}
