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

using DataTable = Gherkin.Ast.DataTable;
using DocString = Gherkin.Ast.DocString;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        public void IntegrityIsValidated()
        {
        }

        [Then(@"typeql define")]
        public void TypeqlDefine(DocString defineQueryStatements)
        {
            string query = defineQueryStatements.Content;
            Console.WriteLine("Define CONTENT: " + query);
            throw new Exception("Not implemented yet =)");
        // TODO: Implement!
//            TypeQLDefine typeQLQuery = TypeQL.parseQuery(String.join("\n", defineQueryStatements));
//            tx().query().define(String.join("\n", defineQueryStatements)).Resolve();
        }

        public void TypeqlDefineThrowsException(DocString defineQueryStatements)
        {
            Assert.Throws<Common.Exception.TypeDBDriverException>(
                () => TypeqlDefine(defineQueryStatements));
        }

        [Then(@"typeql define; throws exception containing {string}")]
        public void TypeqlDefineThrowsExceptionContaining(string expectedMessage, DocString defineQueryStatements)
        {
            var exception = Assert.Throws<Common.Exception.TypeDBDriverException>(
                () => TypeqlDefine(defineQueryStatements));

            Assert.Equal(expectedMessage, exception.Message);
        }

        public void TypeqlInsert(DocString insertQueryStatements)
        {
            string query = insertQueryStatements.Content;
            Console.WriteLine("Insert CONTENT: " + query);
            throw new Exception("Not implemented yet =)");
        // TODO: Implement!
        }

        public void TypeqlInsertThrowsException(DocString insertQueryStatements)
        {
            Assert.Throws<Common.Exception.TypeDBDriverException>(
                () => TypeqlInsert(insertQueryStatements));
        }

        [Then(@"typeql insert; throws exception containing {string}")]
        public void TypeqlInsertThrowsExceptionContaining(string expectedMessage, DocString insertQueryStatements)
        {
            var exception = Assert.Throws<Common.Exception.TypeDBDriverException>(
                () => TypeqlInsert(insertQueryStatements));

            Assert.Equal(expectedMessage, exception.Message);
        }

//        public static List<IConceptDictionary> Answers()
//        {
//            return answers;
//        }
//
//        private static List<IConceptDictionary> _answers;
//        private static List<JSON> _fetchAnswers;
//        private static Value? _valueAnswer;
//        private static List<IConceptDictionaryGroup> _answerGroups;
//        private static List<IValueGroup> _valueAnswerGroups;
//        private Dictionary<string, Dictionary<string, string>> _rules;
    }
}
