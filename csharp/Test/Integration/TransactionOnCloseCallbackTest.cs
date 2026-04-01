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
using System.Threading;

using NUnit.Framework;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Integration
{
    /// <summary>
    /// Tests for transaction OnClose callback functionality.
    /// These tests verify that OnClose callbacks are properly invoked
    /// in both explicit close and GC finalization scenarios.
    /// </summary>
    [TestFixture]
    public class TransactionOnCloseCallbackTest
    {
        private IDriver? _driver;

        [SetUp]
        public void SetUp()
        {
            _driver = TypeDB.Driver(TypeDB.DefaultAddress, new Credentials("admin", "password"), new DriverOptions(false, null));

            // Clean up any existing test database
            if (_driver.Databases.Contains("callback_test"))
            {
                _driver.Databases.Get("callback_test").Delete();
            }
            _driver.Databases.Create("callback_test");
        }

        [TearDown]
        public void TearDown()
        {
            if (_driver != null)
            {
                if (_driver.Databases.Contains("callback_test"))
                {
                    _driver.Databases.Get("callback_test").Delete();
                }
                _driver.Close();
                _driver = null;
            }
        }

        /// <summary>
        /// Test that OnClose callback is invoked when transaction is explicitly closed.
        /// This mirrors the Java driver test.
        /// </summary>
        [Test]
        public void OnCloseCallbackIsInvokedOnExplicitClose()
        {
            bool callbackInvoked = false;
            Exception? callbackError = null;

            var tx = _driver!.Transaction("callback_test", TransactionType.Read);

            tx.OnClose(error =>
            {
                callbackInvoked = true;
                callbackError = error;
            });

            tx.Close();

            Assert.IsTrue(callbackInvoked, "OnClose callback should be invoked on explicit close");
            Assert.IsNull(callbackError, "OnClose callback should not receive an error on normal close");
        }

        /// <summary>
        /// Test that OnClose callback is invoked when transaction goes out of scope
        /// and is garbage collected without explicit close.
        ///
        /// This tests the scenario that transaction_close_sync was designed to handle:
        /// ensuring callbacks are invoked before SWIG directors are finalized.
        /// </summary>
        [Test]
        public void OnCloseCallbackIsInvokedOnGarbageCollection()
        {
            // Use a ManualResetEvent to wait for callback with timeout
            var callbackEvent = new ManualResetEventSlim(false);
            bool callbackInvoked = false;

            // Create transaction in a separate method so it goes out of scope
            CreateTransactionWithCallback(() =>
            {
                callbackInvoked = true;
                callbackEvent.Set();
            });

            // Force garbage collection
            GC.Collect();
            GC.WaitForPendingFinalizers();
            GC.Collect();

            // Wait for callback with timeout (should be quick if it works)
            bool signaled = callbackEvent.Wait(TimeSpan.FromSeconds(5));

            Assert.IsTrue(callbackInvoked, "OnClose callback should be invoked when transaction is garbage collected");
            Assert.IsTrue(signaled, "Callback should complete within timeout");
        }

        /// <summary>
        /// Helper method to create a transaction with callback in a separate scope.
        /// The transaction is NOT explicitly closed - it will be finalized by GC.
        /// </summary>
        private void CreateTransactionWithCallback(Action onCloseAction)
        {
            var tx = _driver!.Transaction("callback_test", TransactionType.Read);

            tx.OnClose(error =>
            {
                onCloseAction();
            });

            // Intentionally do NOT close the transaction
            // It will be garbage collected when this method returns
        }

        /// <summary>
        /// Test multiple OnClose callbacks on the same transaction.
        /// </summary>
        [Test]
        public void MultipleOnCloseCallbacksAreAllInvoked()
        {
            int callbackCount = 0;

            var tx = _driver!.Transaction("callback_test", TransactionType.Read);

            tx.OnClose(error => { Interlocked.Increment(ref callbackCount); });
            tx.OnClose(error => { Interlocked.Increment(ref callbackCount); });
            tx.OnClose(error => { Interlocked.Increment(ref callbackCount); });

            tx.Close();

            Assert.AreEqual(3, callbackCount, "All three OnClose callbacks should be invoked");
        }
    }
}
