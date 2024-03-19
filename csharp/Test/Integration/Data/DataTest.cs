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

using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using static TypeDB.Driver.Api.IThingType;
using static TypeDB.Driver.Api.IThingType.Annotation;

namespace TypeDB.Driver.Test.Integration
{
    [TestFixture]
    public class DataTestFixture
    {
        [Test]
        public void TransitivityEnumIsCorrectlyConvertible()
        {
            void CheckConversions(Pinvoke.Transitivity native, IConcept.Transitivity wrapper)
            {
                Assert.AreEqual(wrapper, (IConcept.Transitivity)native);
                Assert.AreEqual(native, (Pinvoke.Transitivity)wrapper);
            }

            CheckConversions(Pinvoke.Transitivity.Transitive, IConcept.Transitivity.Transitive);
            CheckConversions(Pinvoke.Transitivity.Explicit, IConcept.Transitivity.Explicit);
        }

        [Test]
        public void SessionTypeEnumIsCorrectlyConvertible()
        {
            void CheckConversions(Pinvoke.SessionType native, SessionType wrapper)
            {
                Assert.AreEqual(wrapper, (SessionType)native);
                Assert.AreEqual(native, (Pinvoke.SessionType)wrapper);
            }

            CheckConversions(Pinvoke.SessionType.Schema, SessionType.Schema);
            CheckConversions(Pinvoke.SessionType.Data, SessionType.Data);
        }

        [Test]
        public void TransactionTypeEnumIsCorrectlyConvertible()
        {
            void CheckConversions(Pinvoke.TransactionType native, TransactionType wrapper)
            {
                Assert.AreEqual(wrapper, (TransactionType)native);
                Assert.AreEqual(native, (Pinvoke.TransactionType)wrapper);
            }

            CheckConversions(Pinvoke.TransactionType.Read, TransactionType.Read);
            CheckConversions(Pinvoke.TransactionType.Write, TransactionType.Write);
        }

        [Test]
        public void ValueTypeEnumIsCorrectlyConvertible()
        {
            void CheckConversions(Pinvoke.ValueType native, IValue.ValueType wrapper)
            {
                Assert.AreEqual(wrapper, (IValue.ValueType)native);
                Assert.AreEqual(native, (Pinvoke.ValueType)wrapper);
            }

            CheckConversions(Pinvoke.ValueType.Object, IValue.ValueType.Object);
            CheckConversions(Pinvoke.ValueType.Boolean, IValue.ValueType.Bool);
            CheckConversions(Pinvoke.ValueType.Long, IValue.ValueType.Long);
            CheckConversions(Pinvoke.ValueType.Double, IValue.ValueType.Double);
            CheckConversions(Pinvoke.ValueType.String, IValue.ValueType.String);
            CheckConversions(Pinvoke.ValueType.DateTime, IValue.ValueType.DateTime);
        }

        [Test]
        public void ValueTypeEnumsGetValueClassIsCorrect()
        {
            Assert.AreEqual(typeof(object), IValue.ValueType.Object.GetValueClass());
            Assert.AreEqual(typeof(bool), IValue.ValueType.Bool.GetValueClass());
            Assert.AreEqual(typeof(long), IValue.ValueType.Long.GetValueClass());
            Assert.AreEqual(typeof(double), IValue.ValueType.Double.GetValueClass());
            Assert.AreEqual(typeof(string), IValue.ValueType.String.GetValueClass());
            Assert.AreEqual(typeof(System.DateTime), IValue.ValueType.DateTime.GetValueClass());
        }

        [Test]
        public void ValueTypeEnumsIsWritableIsCorrect()
        {
            Assert.False(IValue.ValueType.Object.IsWritable());
            Assert.True(IValue.ValueType.Bool.IsWritable());
            Assert.True(IValue.ValueType.Long.IsWritable());
            Assert.True(IValue.ValueType.Double.IsWritable());
            Assert.True(IValue.ValueType.String.IsWritable());
            Assert.True(IValue.ValueType.DateTime.IsWritable());
        }

        [Test]
        public void ValueTypeEnumsIsKeyableIsCorrect()
        {
            Assert.False(IValue.ValueType.Object.IsKeyable());
            Assert.False(IValue.ValueType.Bool.IsKeyable());
            Assert.True(IValue.ValueType.Long.IsKeyable());
            Assert.False(IValue.ValueType.Double.IsKeyable());
            Assert.True(IValue.ValueType.String.IsKeyable());
            Assert.True(IValue.ValueType.DateTime.IsKeyable());
        }
    }
}
