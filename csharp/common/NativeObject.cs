/*
 * Copyright (C) 2022 Vaticle
 *
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

#nullable enable

using com.vaticle.typedb.driver.pinvoke;
using com.vaticle.typedb.driver.Common.Exception;

namespace com.vaticle.typedb.driver.Common
{
    public abstract class NativeObjectWrapper<T>
    {
        public readonly T? NativeObject;

        static NativeObjectWrapper()
        {
            pinvoke.typedb_driver.init_logging();
        }

        protected NativeObjectWrapper(T? NativeObject)
        {
            if (NativeObject == null)
            {
//                throw new TypeDBDriverException(ErrorMessage.Internal.NULL_NATIVE_VALUE);
            }

            this.NativeObject = NativeObject;
        }
    }
}
