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
using System.Collections.Generic;

using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Analyze
{
    public abstract class Fetch : NativeObjectWrapper<Pinvoke.Fetch>, IFetch
    {
        protected Fetch(Pinvoke.Fetch nativeObject)
            : base(nativeObject)
        {
        }

        public static Fetch? Of(Pinvoke.Fetch? nativeObject)
        {
            if (nativeObject == null)
            {
                return null;
            }

            var variant = Pinvoke.typedb_driver.fetch_variant(nativeObject);
            switch (variant)
            {
                case Pinvoke.FetchVariant.LeafDocument:
                    return new FetchLeafImpl(nativeObject);
                case Pinvoke.FetchVariant.ListDocument:
                    return new FetchListImpl(nativeObject);
                case Pinvoke.FetchVariant.ObjectDocument:
                    return new FetchObjectImpl(nativeObject);
                default:
                    throw new InvalidOperationException("Unexpected fetch variant: " + variant);
            }
        }

        public Pinvoke.FetchVariant Variant
        {
            get { return Pinvoke.typedb_driver.fetch_variant(NativeObject); }
        }

        public virtual bool IsLeaf => false;
        public virtual bool IsList => false;
        public virtual bool IsObject => false;

        public virtual IFetchLeaf AsLeaf() => throw InvalidCast("FetchLeaf");
        public virtual IFetchList AsList() => throw InvalidCast("FetchList");
        public virtual IFetchObject AsObject() => throw InvalidCast("FetchObject");

        private InvalidOperationException InvalidCast(string targetType)
        {
            return new InvalidOperationException($"Cannot cast {GetType().Name} to {targetType}");
        }

        public class FetchLeafImpl : Fetch, IFetchLeaf
        {
            internal FetchLeafImpl(Pinvoke.Fetch nativeObject) : base(nativeObject) { }

            public override bool IsLeaf => true;
            public override IFetchLeaf AsLeaf() => this;

            public IEnumerable<string> Annotations =>
                new NativeEnumerable<string>(
                    Pinvoke.typedb_driver.fetch_leaf_annotations(NativeObject));
        }

        public class FetchListImpl : Fetch, IFetchList
        {
            internal FetchListImpl(Pinvoke.Fetch nativeObject) : base(nativeObject) { }

            public override bool IsList => true;
            public override IFetchList AsList() => this;

            public IFetch Element
            {
                get
                {
                    var nativeElement = Pinvoke.typedb_driver.fetch_list_element(NativeObject);
                    return Fetch.Of(nativeElement)!;
                }
            }
        }

        public class FetchObjectImpl : Fetch, IFetchObject
        {
            internal FetchObjectImpl(Pinvoke.Fetch nativeObject) : base(nativeObject) { }

            public override bool IsObject => true;
            public override IFetchObject AsObject() => this;

            public IEnumerable<string> Keys =>
                new NativeEnumerable<string>(
                    Pinvoke.typedb_driver.fetch_object_fields(NativeObject));

            public IFetch Get(string key)
            {
                var nativeFetch = Pinvoke.typedb_driver.fetch_object_get_field(NativeObject, key);
                return Fetch.Of(nativeFetch)!;
            }
        }
    }
}
