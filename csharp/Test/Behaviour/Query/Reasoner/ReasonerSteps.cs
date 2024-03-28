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
        private DocString? _previousQuery;

        public static string DEFAULT_DATABASE = "test";

        [Given(@"reasoning schema")]
        public void ReasoningSchema(DocString defineQueryStatements)
        {
            if (!Driver!.Databases.Contains(DEFAULT_DATABASE))
            {
                ConnectionCreateDatabase(DEFAULT_DATABASE);
            }

            ConnectionOpenSchemaSessionForDatabase(DEFAULT_DATABASE);
            ForEachSessionOpenTransactionsOfType("write");

            TypeqlDefine(defineQueryStatements);
            _previousQuery = defineQueryStatements;

            TransactionCommits();
            ConnectionCloseAllSessions();
        }

        [Given(@"reasoning data")]
        public void ReasoningData(DocString dataQueryStatements)
        {
            ConnectionOpenDataSessionForDatabase(DEFAULT_DATABASE);
            ForEachSessionOpenTransactionsOfType("write");

            TypeqlInsert(dataQueryStatements);
            _previousQuery = dataQueryStatements;

            TransactionCommits();
            ConnectionCloseAllSessions();
        }

        [Given(@"reasoning query")]
        public void ReasoningQuery(DocString getQueryStatements)
        {
            ConnectionOpenDataSessionForDatabase(DEFAULT_DATABASE);
            ForEachSessionOpenTransactionsOfType("read");

            GetAnswersOfTypeqlGet(getQueryStatements);
            _previousQuery = getQueryStatements;

            ConnectionCloseAllSessions();
        }

        [Given(@"verifier is initialised")]
        public void VerifierIsInitialised()
        {
            // no-op: verification runs on the backend only.
        }

        [Then(@"verify answers are sound")]
        public void VerifyAnswersAreSound()
        {
            // no-op: verification runs on the backend only.
        }

        [Then(@"verify answers are complete")]
        public void VerifyAnswersAreComplete()
        {
            // no-op: verification runs on the backend only.
        }

        [Then(@"verify answer set is equivalent for query")]
        [And(@"verify answer set is equivalent for query")]
        public void VerifyAnswerSetIsEquivalentForQuery(DocString queryStatements)
        {
            List<IConceptMap> oldAnswers = new List<IConceptMap>();
            foreach (var a in _answers!)
            {
                oldAnswers.Add(a);
            }

            ReasoningQuery(queryStatements);

            int answersCount = _answers.Count;
            Assert.Equal(oldAnswers.Count, answersCount);

            int matchedCount = 0;

            foreach (var currentAnswer in _answers)
            {
                var matchedAnswer =
                    oldAnswers.Where(oldAnswer => oldAnswer.Equals(currentAnswer)).FirstOrDefault();

                if (matchedAnswer != null)
                {
                    matchedCount += 1;
                }
            }

            Assert.Equal(answersCount, matchedCount);
        }

        [Then(@"verify answers are consistent across {int} executions")]
        public void VerifyAnswersAreConsistentAcrossExecutions(int executionNum)
        {
            for (int i = 0; i < executionNum; i++)
            {
                VerifyAnswerSetIsEquivalentForQuery(_previousQuery!);
            }
        }

        [Then(@"verify answer size is: {int}")]
        public void VerifyAnswerSizeIs(int expectedAnswers)
        {
            AnswerSizeIs(expectedAnswers);
        }
    }
}
