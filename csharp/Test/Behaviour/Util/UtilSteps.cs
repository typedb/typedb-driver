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
using System.IO;
using System.Linq;
using System.Threading;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [Given(@"wait (\d+) seconds")]
        [When(@"wait (\d+) seconds")]
        [Then(@"wait (\d+) seconds")]
        public void WaitSeconds(int seconds)
        {
            Thread.Sleep(seconds * 1000);
        }

        [Given(@"set time-zone: (.+)")]
        [When(@"set time-zone: (.+)")]
        public void SetTimeZone(string timeZoneId)
        {
            // C# doesn't support changing the process timezone at runtime the way
            // Java's TimeZone.setDefault() does. We set the TZ environment variable
            // which some systems respect, and track it for test documentation.
            Environment.SetEnvironmentVariable("TZ", timeZoneId);
        }

        #region File Operation Steps

        // Pattern: file(X) does not exist (Given = ensure, Then = assert)
        [Given(@"file\(([^)]+)\) does not exist")]
        public void FileDoesNotExistGiven(string fileName)
        {
            var path = ConnectionStepsBase.FullPath(fileName);
            if (File.Exists(path))
            {
                File.Delete(path);
            }
            Assert.False(File.Exists(path));
        }

        [Then(@"file\(([^)]+)\) does not exist")]
        public void FileDoesNotExistThen(string fileName)
        {
            Assert.False(File.Exists(ConnectionStepsBase.FullPath(fileName)));
        }

        // Pattern: file(X) exists
        [Then(@"file\(([^)]+)\) exists")]
        public void FileExists(string fileName)
        {
            Assert.True(File.Exists(ConnectionStepsBase.FullPath(fileName)));
        }

        // Pattern: file(X) is not empty
        [Then(@"file\(([^)]+)\) is not empty")]
        public void FileIsNotEmpty(string fileName)
        {
            var path = ConnectionStepsBase.FullPath(fileName);
            Assert.True(File.Exists(path));
            Assert.True(new FileInfo(path).Length > 0);
        }

        // Pattern: file(X) has schema: (with DocString)
        [Then(@"file\(([^)]+)\) has schema:")]
        public void FileHasSchema(string fileName, DocString expectedSchema)
        {
            var filePath = ConnectionStepsBase.FullPath(fileName);
            var fileContents = File.ReadAllText(filePath).Trim();

            var expectedText = expectedSchema.Content.Trim();
            if (string.IsNullOrWhiteSpace(expectedText))
            {
                Assert.Equal("", fileContents);
                return;
            }

            // Normalize both schemas through temp databases for comparison
            // (handles reordering and formatting differences)
            var expectedNormalized = ExecuteAndRetrieveSchemaForComparison(
                RemoveTwoSpacesInTabulation(expectedText));
            var fileNormalized = ExecuteAndRetrieveSchemaForComparison(fileContents);
            Assert.Equal(expectedNormalized, fileNormalized);
        }

        // Pattern: file(X) write: (with DocString)
        [When(@"file\(([^)]+)\) write:")]
        public void FileWrite(string fileName, DocString content)
        {
            var path = ConnectionStepsBase.FullPath(fileName);
            File.WriteAllText(path, content.Content.Trim());
        }

        #endregion

        #region Schema Comparison Helpers

        /// <summary>
        /// Creates a temporary database with the given schema, retrieves the normalized
        /// schema string, then deletes the temporary database. This normalizes schema
        /// formatting for comparison.
        /// </summary>
        private string ExecuteAndRetrieveSchemaForComparison(string schemaQuery)
        {
            var tempName = "temp-" + new Random().Next(10000);
            Driver!.Databases.Create(tempName);
            try
            {
                var tx = Driver.Transaction(tempName, TransactionType.Schema);
                tx.Query(schemaQuery);
                tx.Commit();
                return Driver.Databases.Get(tempName).GetSchema();
            }
            finally
            {
                try { Driver.Databases.Get(tempName).Delete(); } catch { }
            }
        }

        /// <summary>
        /// Removes two leading spaces from each line of a docstring.
        /// Gherkin docstrings in feature files have extra indentation that needs to be removed.
        /// </summary>
        private static string RemoveTwoSpacesInTabulation(string input)
        {
            return string.Join("\n", input.Split('\n')
                .Select(line => line.StartsWith("  ") ? line.Substring(2) : line));
        }

        #endregion
    }
}
