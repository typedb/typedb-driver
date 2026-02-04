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

using DocString = Gherkin.Ast.DocString;
using System;
using System.Collections.Generic;
using System.Linq;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver.Api.Analyze;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private static IAnalyzedQuery? _analyzedQuery;

        private void ClearAnalyzedQuery()
        {
            _analyzedQuery = null;
        }

        [When(@"get answers of typeql analyze")]
        public void GetAnswersOfTypeqlAnalyze(DocString query)
        {
            ClearAnalyzedQuery();
            _analyzedQuery = Tx.Analyze(query.Content);
        }

        [Then(@"typeql analyze; parsing fails")]
        public void TypeqlAnalyzeParsingFails(DocString query)
        {
            ClearAnalyzedQuery();
            Assert.ThrowsAny<Exception>(() => Tx.Analyze(query.Content));
        }

        [Then(@"typeql analyze; fails with a message containing: ""(.*)""")]
        public void TypeqlAnalyzeFailsWithMessage(string expectedMessage, DocString query)
        {
            ClearAnalyzedQuery();
            var exception = Assert.ThrowsAny<Exception>(() => Tx.Analyze(query.Content));
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Then(@"analyzed query pipeline structure is:")]
        public void AnalyzedQueryPipelineStructureIs(DocString expectedFunctor)
        {
            Assert.NotNull(_analyzedQuery);
            var pipeline = _analyzedQuery!.Pipeline;
            var encoder = new FunctorEncoder.StructureEncoder(pipeline);
            var actualFunctor = encoder.Encode(pipeline);
            Assert.Equal(
                FunctorEncoder.NormalizeForCompare(expectedFunctor.Content),
                FunctorEncoder.NormalizeForCompare(actualFunctor));
        }

        [Then(@"analyzed query preamble contains:")]
        public void AnalyzedQueryPreambleContains(DocString expectedFunctor)
        {
            Assert.NotNull(_analyzedQuery);
            var expectedNormalized = FunctorEncoder.NormalizeForCompare(expectedFunctor.Content);

            var preambleFunctors = new List<string>();
            foreach (var func in _analyzedQuery!.Preamble)
            {
                var encoder = new FunctorEncoder.StructureEncoder(func.Body);
                var actualFunctor = encoder.Encode(func);
                preambleFunctors.Add(FunctorEncoder.NormalizeForCompare(actualFunctor));
            }

            Assert.True(
                preambleFunctors.Contains(expectedNormalized),
                $"Did not find {expectedNormalized} in [{string.Join(",", preambleFunctors)}]");
        }

        [Then(@"analyzed query pipeline annotations are:")]
        public void AnalyzedQueryPipelineAnnotationsAre(DocString expectedFunctor)
        {
            Assert.NotNull(_analyzedQuery);
            var pipeline = _analyzedQuery!.Pipeline;
            var encoder = new FunctorEncoder.AnnotationsEncoder(pipeline);
            var actualFunctor = encoder.Encode(pipeline);
            Assert.Equal(
                FunctorEncoder.NormalizeForCompare(expectedFunctor.Content),
                FunctorEncoder.NormalizeForCompare(actualFunctor));
        }

        [Then(@"analyzed preamble annotations contains:")]
        public void AnalyzedPreambleAnnotationsContains(DocString expectedFunctor)
        {
            Assert.NotNull(_analyzedQuery);
            var expectedNormalized = FunctorEncoder.NormalizeForCompare(expectedFunctor.Content);

            var preambleFunctors = new List<string>();
            foreach (var func in _analyzedQuery!.Preamble)
            {
                var encoder = new FunctorEncoder.AnnotationsEncoder(func.Body);
                var actualFunctor = encoder.Encode(func);
                preambleFunctors.Add(FunctorEncoder.NormalizeForCompare(actualFunctor));
            }

            Assert.True(
                preambleFunctors.Contains(expectedNormalized),
                $"Did not find {expectedNormalized} in [{string.Join(",", preambleFunctors)}]");
        }

        [Then(@"analyzed fetch annotations are:")]
        public void AnalyzedFetchAnnotationsAre(DocString expectedFunctor)
        {
            Assert.NotNull(_analyzedQuery);
            var pipeline = _analyzedQuery!.Pipeline;
            var fetch = _analyzedQuery!.Fetch;
            Assert.NotNull(fetch);

            var encoder = new FunctorEncoder.AnnotationsEncoder(pipeline);
            var actualFunctor = encoder.Encode(fetch!);
            Assert.Equal(
                FunctorEncoder.NormalizeForCompare(expectedFunctor.Content),
                FunctorEncoder.NormalizeForCompare(actualFunctor));
        }

        [Then(@"answers have query structure:")]
        public void AnswersHaveQueryStructure(DocString expectedFunctor)
        {
            CollectRowsAnswerIfNeeded();
            Assert.NotNull(_collectedRows);
            Assert.True(_collectedRows!.Count > 0, "Expected at least one answer row");

            // Get the query structure from the first answer
            var firstRow = _collectedRows![0];
            var queryStructure = firstRow.QueryStructure;
            Assert.NotNull(queryStructure);

            var encoder = new FunctorEncoder.StructureEncoder(queryStructure!);
            var actualFunctor = encoder.Encode(queryStructure!);
            Assert.Equal(
                FunctorEncoder.NormalizeForCompare(expectedFunctor.Content),
                FunctorEncoder.NormalizeForCompare(actualFunctor));
        }
    }
}
