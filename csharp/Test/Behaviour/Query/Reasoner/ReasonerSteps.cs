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
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using static TypeDB.Driver.Api.IThingType;
using static TypeDB.Driver.Api.IThingType.Annotation;

using QueryError = TypeDB.Driver.Common.Error.Query;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private static string DEFAULT_DATABASE = "test";

        [Given(@"reasoning schema")]
        public void ReasoningSchema(DocString defineQueryStatements)
        {
            if (!Driver.Databases.Contains(DEFAULT_DATABASE))
            {
                ConnectionCreateDatabase(DEFAULT_DATABASE);
            }

            ConnectionOpenSchemaSessionForDatabase(DEFAULT_DATABASE);
            ForEachSessionOpenTransactionsOfType("write");

            TypeqlDefine(defineQueryStatements);
            TransactionCommits();
            ConnectionCloseAllSessions();
        }

        [Given(@"reasoning data")]
        public void ReasoningData(DocString dataQueryStatements)
        {
            ConnectionOpenDataSessionForDatabase(DEFAULT_DATABASE);
            ForEachSessionOpenTransactionsOfType("write");

            TypeqlInsert(dataQueryStatements);
            TransactionCommits();
            ConnectionCloseAllSessions();
        }

        [Given(@"reasoning query")]
        public void ReasoningQuery(DocString getQueryStatements)
        {
            ConnectionOpenDataSessionForDatabase(DEFAULT_DATABASE);
            ForEachSessionOpenTransactionsOfType("read");

            GetAnswersOfTypeqlGet(getQueryStatements);
            ConnectionCloseAllSessions();
        }

        [Given(@"verifier is initialised")]
        public void VerifierIsInitialised()
        {
            // Not enough instruments to verify for now. // TODO: Add task (for Rust as well)?
        }

        [Then(@"verify answers are sound")]
        public void VerifyAnswersAreSound()
        {
            // Not enough instruments to verify for now. // TODO: Add task (for Rust as well)?
        }

        [Then(@"verify answers are complete")]
        public void VerifyAnswersAreComplete()
        {
            // Not enough instruments to verify for now. // TODO: Add task (for Rust as well)?
        }

        [Then(@"verify answer set is equivalent for query")]
        [And(@"verify answer set is equivalent for query")]
        public void VerifyAnswerSetIsEquivalentForQuery(DocString queryStatements)
        {
            var prevAnswer = _answers;
            ReasoningQuery(queryStatements);
            int totalRows = _answers.Count;
            // TODO: Collect to two sets and compare???
            int matchedRows = 0;
            foreach (var currentAnswer in _answers)
            {
                var matchedElement =
                    prevAnswer.Where(val => val == currentAnswer).FirstOrDefault();

                if (matchedElement != null)
                {
                    matchedRows += 1;
                }
            }

            Assert.Equal(totalRows, matchedRows);
        }

        [Then(@"verify answers are consistent across {int} executions")]
        public void VerifyAnswersAreConsistentAcrossExecutions(int executionNum)
        {
            // TODO: We can't execute previous query again because don't remember the query // Why not storing it?
        }

        [Then(@"verify answer size is: {int}")]
        public void VerifyAnswerSizeIs(int expectedAnswers)
        {
            AnswerSizeIs(expectedAnswers);
        }
    }
}
