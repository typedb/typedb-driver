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

using System.Collections;
using System.Collections.Generic;

using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;

using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Common
{
    public class NativeEnumerable<T> : IEnumerable<T>, System.IDisposable
    {
        private readonly NativeEnumerator<T> _enumerator;
        private bool _enumeratorUsed;
        private bool _disposed;

        public NativeEnumerable(IEnumerator<T> enumerator)
        {
            _enumerator = new NativeEnumerator<T>(enumerator);
            _enumeratorUsed = false;
            _disposed = false;
        }

        public IEnumerator<T> GetEnumerator()
        {
            Validator.ThrowIfTrue(() => _enumeratorUsed, InternalError.ENUMERATOR_EXCESSIVE_ACCESS);

            _enumeratorUsed = true;
            return _enumerator;
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return this.GetEnumerator();
        }

        public void Dispose()
        {
            if (!_disposed)
            {
                _disposed = true;
                _enumerator.Dispose();
            }
        }
    }

    internal class NativeEnumerator<T> : IEnumerator<T>
    {
        private IEnumerator<T> _innerEnumerator;

        public NativeEnumerator(IEnumerator<T> enumerator)
        {
            _innerEnumerator = enumerator;
        }

        object IEnumerator.Current
        {
            get { return Current!; }
        }

        public T Current
        {
            get { return _innerEnumerator.Current; }
        }

        public bool MoveNext()
        {
            try
            {
                return _innerEnumerator.MoveNext();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Reset()
        {
            _innerEnumerator.Reset();
        }

        public void Dispose()
        {
            // Dispose the underlying native iterator to release native resources immediately
            // instead of waiting for GC finalization (which can cause race conditions)
            if (_innerEnumerator is System.IDisposable disposable)
            {
                disposable.Dispose();
            }
        }
    }
}
