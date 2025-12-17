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

/**
 * 'links' & 'relates' constraints accept unscoped role names.
 * Since an unscoped role-name does not uniquely identify a role-type,
 * (Different role-types belonging to different relation types may share the same name)
 * an internal variable is introduced to handle the ambiguity
 */
public interface NamedRole {

    /**
     * The internal variable injected to handle ambiguity in unscoped role names.
     *
     * @return the variable associated with this named role
     */
    Variable variable();

    /**
     * The unscoped role name specified in the query.
     *
     * @return the unscoped name of the role
     */
    String name();
}
