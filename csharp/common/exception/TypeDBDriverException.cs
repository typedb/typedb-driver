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

using System;
using System.Diagnostics;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Common.Exception;

namespace Vaticle.Typedb.Driver.Common.Exception
{
    public class TypeDBDriverException : System.Exception
    {
        /**
         * @hidden
         */
        public TypeDBDriverException(ErrorMessage error, params object?[] errorParams)
            : base(error.ToString(errorParams))
        {
            _errorMessage = error;
        }

        /**
         * @hidden
         */
        public TypeDBDriverException(string message)
            : base(message)
        {
            _errorMessage = null;
        }

        /**
         * @hidden
         */
        public TypeDBDriverException(System.Exception error)
            : base(error.Message)
        {
            _errorMessage = null;
        }

        /**
         * @hidden
         */
        public TypeDBDriverException(Pinvoke.Error nativeError)
            : base(nativeError.Message)
        {
            _errorMessage = null;
        }

        public string Name
        {
            get { return this.GetType().Name; }
        }

        public ErrorMessage? ErrorMessage
        {
            get { return _errorMessage; }
        }

        private readonly ErrorMessage? _errorMessage; // TODO: Looks like it is used in Java only for a test...
    }
}
