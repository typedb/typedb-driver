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

using System;
using TypeDB.Driver.Api.Answer;

namespace TypeDB.Driver.Answer
{
    /// <summary>
    /// Represents a simple Ok message as a server answer. Doesn't contain concepts.
    /// </summary>
    public class OkQueryAnswer : QueryAnswer, IOkQueryAnswer, IDisposable
    {
        // Store the native answer to prevent it from being GC'd prematurely.
        // The native answer must be kept alive as long as this wrapper exists.
        private readonly Pinvoke.QueryAnswer _nativeAnswer;
        private bool _disposed;

        internal OkQueryAnswer(Pinvoke.QueryAnswer nativeAnswer)
            : base(nativeAnswer)
        {
            _nativeAnswer = nativeAnswer;
            _disposed = false;
        }

        /// <summary>
        /// Disposes the native QueryAnswer object.
        /// </summary>
        public void Dispose()
        {
            if (!_disposed)
            {
                _disposed = true;
                _nativeAnswer.Dispose();
            }
            GC.SuppressFinalize(this);
        }

        ~OkQueryAnswer()
        {
            // Note: Don't call Dispose(false) pattern here because we're just
            // wrapping _nativeAnswer which will be finalized by its own finalizer
            // if not already disposed.
        }
    }
}
