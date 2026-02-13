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
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    /// <summary>
    /// Debug step definitions for investigating multi-driver scenarios.
    /// These steps help test the hypothesis that having multiple active drivers
    /// during UpdatePassword can cause connection conflicts.
    /// </summary>
    public partial class BehaviourSteps
    {
        // Additional drivers for multi-driver testing
        private static List<IDriver> _extraDrivers = new List<IDriver>();

        [Given(@"I create (\d+) additional drivers")]
        [When(@"I create (\d+) additional drivers")]
        public void CreateAdditionalDrivers(int count)
        {
            Console.WriteLine($"Creating {count} additional drivers...");
            for (int i = 0; i < count; i++)
            {
                var driver = TypeDB.Driver(
                    TypeDB.DefaultAddress,
                    new Credentials(AdminUsername, AdminPassword),
                    new DriverOptions(false, null));
                _extraDrivers.Add(driver);
                Console.WriteLine($"  Created extra driver {i + 1}, IsOpen: {driver.IsOpen()}");
            }
            Console.WriteLine($"Total extra drivers: {_extraDrivers.Count}");
        }

        [Given(@"I close all additional drivers")]
        [When(@"I close all additional drivers")]
        [Then(@"I close all additional drivers")]
        public void CloseAllAdditionalDrivers()
        {
            Console.WriteLine($"Closing {_extraDrivers.Count} additional drivers...");
            foreach (var driver in _extraDrivers)
            {
                try
                {
                    driver.Close();
                    Console.WriteLine("  Closed an extra driver");
                }
                catch (Exception e)
                {
                    Console.WriteLine($"  Error closing extra driver: {e.Message}");
                }
            }
            _extraDrivers.Clear();
        }

        [When(@"update admin password to '([^']*)' with (\d+) other drivers open")]
        public void UpdateAdminPasswordWithOtherDriversOpen(string newPassword, int otherDriverCount)
        {
            Console.WriteLine($"=== Testing UpdatePassword with {otherDriverCount} other drivers open ===");

            // Create the other drivers first
            var otherDrivers = new List<IDriver>();
            for (int i = 0; i < otherDriverCount; i++)
            {
                var d = TypeDB.Driver(
                    TypeDB.DefaultAddress,
                    new Credentials(AdminUsername, AdminPassword),
                    new DriverOptions(false, null));
                otherDrivers.Add(d);
                Console.WriteLine($"  Created other driver {i + 1}, IsOpen: {d.IsOpen()}");
            }

            // Now create a new driver to perform the UpdatePassword
            Console.WriteLine("Creating driver for UpdatePassword...");
            var updateDriver = TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials(AdminUsername, AdminPassword),
                new DriverOptions(false, null));
            Console.WriteLine($"  UpdateDriver created, IsOpen: {updateDriver.IsOpen()}");

            // Perform UpdatePassword while other drivers are still open
            Console.WriteLine($"Calling UpdatePassword with {otherDrivers.Count} other drivers still open...");
            try
            {
                updateDriver.Users.Get(AdminUsername).UpdatePassword(newPassword);
                Console.WriteLine("  UpdatePassword SUCCEEDED");
            }
            catch (Exception e)
            {
                Console.WriteLine($"  UpdatePassword FAILED: {e.Message}");
                throw;
            }

            // Close the update driver
            Console.WriteLine("Closing updateDriver...");
            updateDriver.Close();

            // Now close the other drivers
            Console.WriteLine("Closing other drivers...");
            foreach (var d in otherDrivers)
            {
                try { d.Close(); } catch { }
            }
            Console.WriteLine("=== Test complete ===");
        }

        [When(@"update admin password to '([^']*)' after closing all other drivers")]
        public void UpdateAdminPasswordAfterClosingOtherDrivers(string newPassword)
        {
            Console.WriteLine("=== Testing UpdatePassword with NO other drivers open ===");

            // Close Driver if open
            if (Driver != null)
            {
                Console.WriteLine("Closing Driver...");
                try { Driver.Close(); } catch { }
                Driver = null;
            }

            // Close BackgroundDriver if open
            if (BackgroundDriver != null)
            {
                Console.WriteLine("Closing BackgroundDriver...");
                try { BackgroundDriver.Close(); } catch { }
                BackgroundDriver = null;
            }

            // Close any extra drivers
            CloseAllAdditionalDrivers();

            // Now create a fresh driver for UpdatePassword
            Console.WriteLine("Creating fresh driver for UpdatePassword...");
            var updateDriver = TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials(AdminUsername, AdminPassword),
                new DriverOptions(false, null));
            Console.WriteLine($"  UpdateDriver created, IsOpen: {updateDriver.IsOpen()}");

            // Perform UpdatePassword with no other drivers
            Console.WriteLine("Calling UpdatePassword with NO other drivers open...");
            try
            {
                updateDriver.Users.Get(AdminUsername).UpdatePassword(newPassword);
                Console.WriteLine("  UpdatePassword SUCCEEDED");
            }
            catch (Exception e)
            {
                Console.WriteLine($"  UpdatePassword FAILED: {e.Message}");
                throw;
            }

            // Close the update driver
            Console.WriteLine("Closing updateDriver...");
            updateDriver.Close();
            Console.WriteLine("=== Test complete ===");
        }

        [Then(@"all drivers report open status correctly")]
        public void AllDriversReportOpenStatusCorrectly()
        {
            Console.WriteLine("Checking driver statuses...");
            if (Driver != null)
            {
                Console.WriteLine($"  Driver.IsOpen(): {Driver.IsOpen()}");
            }
            if (BackgroundDriver != null)
            {
                Console.WriteLine($"  BackgroundDriver.IsOpen(): {BackgroundDriver.IsOpen()}");
            }
            for (int i = 0; i < _extraDrivers.Count; i++)
            {
                Console.WriteLine($"  ExtraDriver[{i}].IsOpen(): {_extraDrivers[i].IsOpen()}");
            }
        }

        [Given(@"background driver is open")]
        [Then(@"background driver is open")]
        public void BackgroundDriverIsOpen()
        {
            Assert.NotNull(BackgroundDriver);
            Assert.True(BackgroundDriver!.IsOpen(), "BackgroundDriver should be open");
            Console.WriteLine($"BackgroundDriver.IsOpen(): {BackgroundDriver.IsOpen()}");
        }

        [Given(@"I verify cleanup happens with drivers: Driver=([^,]*), BackgroundDriver=([^,]*), ExtraCount=(\d+)")]
        [When(@"I verify cleanup happens with drivers: Driver=([^,]*), BackgroundDriver=([^,]*), ExtraCount=(\d+)")]
        public void VerifyCleanupWithDrivers(string driverState, string bgDriverState, int extraCount)
        {
            Console.WriteLine("=== Simulating cleanup scenario ===");
            Console.WriteLine($"  Expected: Driver={driverState}, BackgroundDriver={bgDriverState}, ExtraDrivers={extraCount}");

            // Setup drivers according to specification
            if (driverState == "open" && Driver == null)
            {
                Driver = CreateDefaultTypeDBDriver();
            }
            else if (driverState == "closed" && Driver != null)
            {
                Driver.Close();
                Driver = null;
            }

            if (bgDriverState == "open" && BackgroundDriver == null)
            {
                BackgroundDriver = CreateDefaultTypeDBDriver();
            }
            else if (bgDriverState == "closed" && BackgroundDriver != null)
            {
                BackgroundDriver.Close();
                BackgroundDriver = null;
            }

            // Create extra drivers
            for (int i = _extraDrivers.Count; i < extraCount; i++)
            {
                _extraDrivers.Add(CreateDefaultTypeDBDriver());
            }

            Console.WriteLine($"  Actual: Driver={(Driver != null ? "open" : "null")}, " +
                            $"BackgroundDriver={(BackgroundDriver != null ? "open" : "null")}, " +
                            $"ExtraDrivers={_extraDrivers.Count}");

            // Now attempt UpdatePassword like the cleanup does
            Console.WriteLine("  Attempting UpdatePassword like cleanup...");
            var cleanupDriver = CreateDefaultTypeDBDriver();
            try
            {
                cleanupDriver.Users.Get(AdminUsername).UpdatePassword(AdminPassword);
                Console.WriteLine("  UpdatePassword SUCCEEDED");
            }
            catch (Exception e)
            {
                Console.WriteLine($"  UpdatePassword FAILED: {e.Message}");
                throw;
            }
            finally
            {
                cleanupDriver.Close();
            }
            Console.WriteLine("=== Cleanup simulation complete ===");
        }

        /// <summary>
        /// Simulates the OLD cleanup behavior (before the fix) where cleanupDriver
        /// is created while Driver and BackgroundDriver are still open.
        /// </summary>
        [When(@"I simulate OLD cleanup behavior \(drivers open during UpdatePassword\)")]
        public void SimulateOldCleanupBehavior()
        {
            Console.WriteLine("=== Simulating OLD cleanup behavior ===");
            Console.WriteLine($"  Driver: {(Driver != null && Driver.IsOpen() ? "OPEN" : "closed/null")}");
            Console.WriteLine($"  BackgroundDriver: {(BackgroundDriver != null && BackgroundDriver.IsOpen() ? "OPEN" : "closed/null")}");

            // OLD behavior: Create cleanupDriver WHILE other drivers are still open
            Console.WriteLine("  Creating cleanupDriver while other drivers are STILL OPEN...");
            var cleanupDriver = CreateDefaultTypeDBDriver();
            Console.WriteLine($"  cleanupDriver created, IsOpen: {cleanupDriver.IsOpen()}");

            // Perform operations like original cleanup did
            try
            {
                Console.WriteLine("  Deleting non-admin users...");
                foreach (var user in cleanupDriver.Users.GetAll())
                {
                    if (user.Username != AdminUsername)
                    {
                        try { cleanupDriver.Users.Get(user.Username).Delete(); } catch { }
                    }
                }

                Console.WriteLine("  Calling UpdatePassword (this is where original bug occurred)...");
                cleanupDriver.Users.Get(AdminUsername).UpdatePassword(AdminPassword);
                Console.WriteLine("  UpdatePassword SUCCEEDED");

                Console.WriteLine("  Deleting all databases...");
                foreach (var db in cleanupDriver.Databases.GetAll())
                {
                    try { cleanupDriver.Databases.Get(db.Name).Delete(); } catch { }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"  FAILED: {e.Message}");
                throw;
            }

            Console.WriteLine("  Closing cleanupDriver...");
            cleanupDriver.Close();

            // OLD behavior: Close other drivers AFTER cleanup
            Console.WriteLine("  Now closing BackgroundDriver (AFTER cleanup)...");
            if (BackgroundDriver != null)
            {
                try { BackgroundDriver.Close(); } catch { }
                BackgroundDriver = null;
            }

            Console.WriteLine("  Now closing Driver (AFTER cleanup)...");
            if (Driver != null)
            {
                try { Driver.Close(); } catch { }
                Driver = null;
            }

            Console.WriteLine("=== OLD cleanup simulation complete ===");
        }

        /// <summary>
        /// Simulates the NEW cleanup behavior (after the fix) where Driver and
        /// BackgroundDriver are closed BEFORE creating cleanupDriver.
        /// </summary>
        [When(@"I simulate NEW cleanup behavior \(drivers closed before UpdatePassword\)")]
        public void SimulateNewCleanupBehavior()
        {
            Console.WriteLine("=== Simulating NEW cleanup behavior ===");
            Console.WriteLine($"  Driver: {(Driver != null && Driver.IsOpen() ? "OPEN" : "closed/null")}");
            Console.WriteLine($"  BackgroundDriver: {(BackgroundDriver != null && BackgroundDriver.IsOpen() ? "OPEN" : "closed/null")}");

            // NEW behavior: Close other drivers FIRST
            Console.WriteLine("  Closing BackgroundDriver FIRST...");
            if (BackgroundDriver != null)
            {
                try { BackgroundDriver.Close(); } catch { }
                BackgroundDriver = null;
            }

            Console.WriteLine("  Closing Driver FIRST...");
            if (Driver != null)
            {
                try { Driver.Close(); } catch { }
                Driver = null;
            }

            // NEW behavior: Create cleanupDriver AFTER other drivers are closed
            Console.WriteLine("  Creating cleanupDriver AFTER other drivers are closed...");
            var cleanupDriver = CreateDefaultTypeDBDriver();
            Console.WriteLine($"  cleanupDriver created, IsOpen: {cleanupDriver.IsOpen()}");

            // Perform operations
            try
            {
                Console.WriteLine("  Deleting non-admin users...");
                foreach (var user in cleanupDriver.Users.GetAll())
                {
                    if (user.Username != AdminUsername)
                    {
                        try { cleanupDriver.Users.Get(user.Username).Delete(); } catch { }
                    }
                }

                Console.WriteLine("  Calling UpdatePassword...");
                cleanupDriver.Users.Get(AdminUsername).UpdatePassword(AdminPassword);
                Console.WriteLine("  UpdatePassword SUCCEEDED");

                Console.WriteLine("  Deleting all databases...");
                foreach (var db in cleanupDriver.Databases.GetAll())
                {
                    try { cleanupDriver.Databases.Get(db.Name).Delete(); } catch { }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"  FAILED: {e.Message}");
                throw;
            }

            Console.WriteLine("  Closing cleanupDriver...");
            cleanupDriver.Close();
            Console.WriteLine("=== NEW cleanup simulation complete ===");
        }
    }
}
