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

using DataTable = Gherkin.Ast.DataTable; // TODO Remove if not needed
using DocString = Gherkin.Ast.DocString; // TODO Remove if not needed
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [When(@"attribute\\( ?{type_label} ?) get instances contain: {var}")]
        public void attribute_type_get_instances_contain(string typeLabel, string var)
        {
            var instances = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .getInstances(SingleTransaction);

            assertTrue(instances.anyMatch(i => i.equals(get(var))));
        }

        [Then(@"attribute {var} get owners contain: {var}")]
        public void attribute_get_owners_contain(string var1, string var2)
        {
            assertTrue(get(var1).asAttribute().getOwners(SingleTransaction).anyMatch(o => o.equals(get(var2))));
        }

        [Then(@"attribute {var} get owners do not contain: {var}")]
        public void attribute_get_owners_do_not_contain(string var1, string var2)
        {
            assertTrue(get(var1).asAttribute().getOwners(SingleTransaction).noneMatch(o => o.equals(get(var2))));
        }

        [Then(@"attribute {var} has value type: {value_type}")]
        public void attribute_has_value_type(string var, Value.Type valueType)
        {
            assertEquals(valueType, get(var).asAttribute().getType().getValueType());
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?boolean ?) put: {bool}")]
        public void attribute_type_as_boolean_put(string var, string typeLabel, boolean value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?boolean ?) put: {bool}; throws exception")]
        public void attribute_type_as_boolean_put_throws_exception(string typeLabel, boolean value)
        {
            assertThrows(() => SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve());
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?long ?) put: {int}")]
        public void attribute_type_as_long_put(string var, string typeLabel, long value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?long ?) put: {int}; throws exception")]
        public void attribute_type_as_long_put_throws_exception(string typeLabel, long value)
        {
            assertThrows(() => SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve());
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?double ?) put: {double}")]
        public void attribute_type_as_double_put(string var, string typeLabel, double value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?double ?) put: {double}; throws exception")]
        public void attribute_type_as_double_put_throws_exception(string typeLabel, double value)
        {
            assertThrows(() => SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve());
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?string ?) put: {word}")]
        public void attribute_type_as_string_put(string var, string typeLabel, string value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?string ?) put: {word}; throws exception")]
        public void attribute_type_as_string_put_throws_exception(string typeLabel, string value)
        {
            assertThrows(() => SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve());
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?datetime ?) put: {datetime}")]
        public void attribute_type_as_datetime_put(string var, string typeLabel, LocalDateTime value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?datetime ?) put: {datetime}; throws exception")]
        public void attribute_type_as_datetime_put_throws_exception(string typeLabel, LocalDateTime value)
        {
            var exception = assertThrows(() => SingleTransaction
                .Concepts.getAttributeType(typeLabel).Resolve()
                .put(SingleTransaction, value));

                Console.WriteLine(exception.Message); // TODO: Just for debug, remove later.
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?boolean ?) get: {bool}")]
        public void attribute_type_as_boolean_get(string var, string typeLabel, boolean value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .get(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?long ?) get: {int}")]
        public void attribute_type_as_long_get(string var, string typeLabel, long value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .get(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?double ?) get: {double}")]
        public void attribute_type_as_double_get(string var, string typeLabel, double value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .get(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?string ?) get: {word}")]
        public void attribute_type_as_string_get(string var, string typeLabel, string value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .get(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [When(@"{var} = attribute\\( ?{type_label} ?) as\\( ?datetime ?) get: {datetime}")]
        public void attribute_type_as_datetime_get(string var, string typeLabel, LocalDateTime value)
        {
            var attributeType = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .get(SingleTransaction, value).Resolve();

            put(var, attributeType);
        }

        [Then(@"attribute {var} has boolean value: {bool}")]
        public void attribute_has_boolean_value(string var, boolean value)
        {
            assertEquals(value, get(var).asAttribute().getValue().asBoolean());
        }

        [Then(@"attribute {var} has long value: {long}")]
        public void attribute_has_long_value(string var, long value)
        {
            assertEquals(value, get(var).asAttribute().getValue().asLong());
        }

        [Then(@"attribute {var} has double value: {double}")]
        public void attribute_has_double_value(string var, double value)
        {
            assertEquals(value, get(var).asAttribute().getValue().asDouble(), 0.0001);
        }

        [Then(@"attribute {var} has string value: {word}")]
        public void attribute_has_string_value(string var, string value)
        {
            assertEquals(value, get(var).asAttribute().getValue().asString());
        }

        [Then(@"attribute {var} has datetime value: {datetime}")]
        public void attribute_has_datetime_value(string var, LocalDateTime value)
        {
            assertEquals(value, get(var).asAttribute().getValue().asDateTime());
        }
    }
}
