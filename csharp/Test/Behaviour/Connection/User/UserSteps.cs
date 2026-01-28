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
using System;
using System.Collections.Generic;
using System.Linq;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [Given(@"connection opens with username '([^']*)', password '([^']*)'")]
        [When(@"connection opens with username '([^']*)', password '([^']*)'")]
        [Then(@"connection opens with username '([^']*)', password '([^']*)'")]
        public void ConnectionOpensWithUsername(string username, string password)
        {
            if (Driver != null && Driver is IDisposable disposable)
            {
                disposable.Dispose();
            }
            Driver = TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials(username, password),
                new DriverOptions(false, null));
        }

        [Then(@"connection opens with username '([^']*)', password '([^']*)'; fails with a message containing: ""(.*)""")]
        public void ConnectionOpensWithUsernameFailsWithMessage(
            string username, string password, string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                TypeDB.Driver(
                    TypeDB.DefaultAddress,
                    new Credentials(username, password),
                    new DriverOptions(false, null));
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"connection has (\d+) users?")]
        [Then(@"connection has (\d+) users?")]
        public void ConnectionHasUserCount(int expectedCount)
        {
            Assert.NotNull(Driver);
            Assert.Equal(expectedCount, Driver!.Users.GetAll().Count);
        }

        [Then(@"get all users contains: (.+)")]
        public void GetAllUsersContains(string username)
        {
            Assert.NotNull(Driver);
            var allUsers = Driver!.Users.GetAll();
            Assert.True(
                allUsers.Any(u => u.Username == username),
                $"Expected users to contain '{username}' but they don't. " +
                $"Users: {string.Join(", ", allUsers.Select(u => u.Username))}");
        }

        [Then(@"get all users does not contain: (.+)")]
        public void GetAllUsersDoesNotContain(string username)
        {
            Assert.NotNull(Driver);
            var allUsers = Driver!.Users.GetAll();
            Assert.False(
                allUsers.Any(u => u.Username == username),
                $"Expected users to NOT contain '{username}' but they do.");
        }

        [Then(@"get all users:")]
        public void GetAllUsersExact(DataTable names)
        {
            Assert.NotNull(Driver);
            var expectedNames = new HashSet<string>();
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    expectedNames.Add(cell.Value);
                }
            }
            var actualNames = Driver!.Users.GetAll()
                .Select(u => u.Username).ToHashSet();
            Assert.Equal(expectedNames, actualNames);
        }

        [Then(@"get all users; fails with a message containing: ""(.*)""")]
        public void GetAllUsersFailsWithMessage(string expectedMessage)
        {
            Assert.NotNull(Driver);
            var exception = Assert.ThrowsAny<Exception>(() => Driver!.Users.GetAll());
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"create user with username '([^']*)', password '([^']*)'")]
        [When(@"create user with username '([^']*)', password '([^']*)'")]
        [Then(@"create user with username '([^']*)', password '([^']*)'")]
        public void CreateUserWithUsername(string username, string password)
        {
            Assert.NotNull(Driver);
            Driver!.Users.Create(username, password);
        }

        [When(@"create user with username '([^']*)', password '([^']*)'; fails with a message containing: ""(.*)""")]
        [Then(@"create user with username '([^']*)', password '([^']*)'; fails with a message containing: ""(.*)""")]
        public void CreateUserWithUsernameFailsWithMessage(
            string username, string password, string expectedMessage)
        {
            Assert.NotNull(Driver);
            var exception = Assert.ThrowsAny<Exception>(
                () => Driver!.Users.Create(username, password));
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"delete user: (\S+)")]
        [When(@"delete user: (\S+)")]
        [Then(@"delete user: (\S+)")]
        public void DeleteUser(string username)
        {
            Assert.NotNull(Driver);
            var user = Driver!.Users.Get(username);
            Assert.NotNull(user);
            user!.Delete();
        }

        [When(@"delete user: ([^;]+); fails with a message containing: ""(.*)""")]
        [Then(@"delete user: ([^;]+); fails with a message containing: ""(.*)""")]
        public void DeleteUserFailsWithMessage(string username, string expectedMessage)
        {
            Assert.NotNull(Driver);
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var user = Driver!.Users.Get(username.Trim());
                user!.Delete();
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Then(@"get user\(([^)]+)\) get name: (.+)")]
        public void GetUserGetName(string username, string expectedName)
        {
            Assert.NotNull(Driver);
            var user = Driver!.Users.Get(username);
            Assert.NotNull(user);
            Assert.Equal(expectedName, user!.Username);
        }

        [Then(@"get user: ([^;]+); fails with a message containing: ""(.*)""")]
        public void GetUserFailsWithMessage(string username, string expectedMessage)
        {
            Assert.NotNull(Driver);
            var exception = Assert.ThrowsAny<Exception>(
                () => Driver!.Users.Get(username.Trim()));
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"get user\(([^)]+)\) update password to '([^']*)'")]
        [When(@"get user\(([^)]+)\) update password to '([^']*)'")]
        [Then(@"get user\(([^)]+)\) update password to '([^']*)'")]
        public void GetUserUpdatePassword(string username, string password)
        {
            Assert.NotNull(Driver);
            var user = Driver!.Users.Get(username);
            Assert.NotNull(user);
            user!.UpdatePassword(password);
        }

        [Then(@"get user\(([^)]+)\) update password to '([^']*)'; fails with a message containing: ""(.*)""")]
        public void GetUserUpdatePasswordFailsWithMessage(
            string username, string password, string expectedMessage)
        {
            Assert.NotNull(Driver);
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var user = Driver!.Users.Get(username);
                user!.UpdatePassword(password);
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Then(@"get current username: (.+)")]
        public void GetCurrentUsername(string expectedUsername)
        {
            Assert.NotNull(Driver);
            var currentUser = Driver!.Users.GetCurrentUser();
            Assert.NotNull(currentUser);
            Assert.Equal(expectedUsername, currentUser.Username);
        }
    }
}
