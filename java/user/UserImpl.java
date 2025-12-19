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

package com.typedb.driver.user;

import com.typedb.driver.api.ConsistencyLevel;
import com.typedb.driver.api.user.User;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;
import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.jni.typedb_driver.user_delete;
import static com.typedb.driver.jni.typedb_driver.user_get_name;
import static com.typedb.driver.jni.typedb_driver.user_update_password;

public class UserImpl extends NativeObject<com.typedb.driver.jni.User> implements User {
    UserImpl(com.typedb.driver.jni.User user) {
        super(user);
    }

    @Override
    public String name() {
        return user_get_name(nativeObject);
    }

    @Override
    public void updatePassword(String password, ConsistencyLevel consistencyLevel) {
        Validator.requireNonNull(password, "password");
        try {
            user_update_password(nativeObject, password, ConsistencyLevel.nativeValue(consistencyLevel));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void delete(ConsistencyLevel consistencyLevel) {
        try {
            user_delete(nativeObject.released(), ConsistencyLevel.nativeValue(consistencyLevel));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
