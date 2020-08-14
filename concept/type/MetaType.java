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

package grakn.client.concept.type;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.type.impl.MetaTypeImpl;

public interface MetaType extends ThingType {

    @Override
    default MetaType.Remote asRemote(Transaction tx) {
        return MetaType.Remote.of(tx, getIID());
    }

    interface Local extends ThingType.Local, MetaType {
    }

    /**
     * Type Class of a MetaType
     */
    interface Remote extends MetaType, ThingType.Remote {

        static MetaType.Remote of(Transaction tx, ConceptIID iid) {
            return new MetaTypeImpl.Remote(tx, iid);
        }

    }
}
