/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License")]; you may not use this file except in compliance
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
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Test.Behaviour;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private bool IsUserInUsers(string username) 
        {
            HashSet<string> users = Driver.Users.All.Select(user => user.Username).ToHashSet();
            return users.Contains(username);
        }
    
        [Then(@"get connected user")]
        public void GetConnectedUser()
        {
            var retrievedUser = Driver.User;
        }
    
        [Then(@"users get user: {word}")]
        public void UsersGetUser(string username)
        {
            var retrievedUser = Driver.Users.Get(username);
        }
    
        [Then(@"users get all")]
        public void UsersGetAll()
        {
            var allRetrievedUsers = Driver.Users.All;
        }
    
        [Then(@"users contains: {word}")]
        public void UsersContains(string username) 
        {
            Assert.True(IsUserInUsers(username));
        }
    
        [Then(@"users not contains: {word}")]
        public void UsersNotContains(string username) 
        {
            Assert.False(IsUserInUsers(username));
        }
    
        [When(@"users create: {word}, {word}")]
        public void UsersCreate(string username, string password) 
        {
            Driver.Users.Create(username, password);
        }
    
        [When(@"users delete: {word}")]
        public void UsersDelete(string username) 
        {
            Driver.Users.Delete(username);
        }
    
        [When(@"user password update: {word}, {word}")]
        public void UserPasswordUpdate(string oldPassword, string newPassword)
        {
            Driver.Users.Get(Driver.User.Username).UpdatePassword(oldPassword, newPassword);
        }
    
        [Then(@"user expiry-seconds")]
        public void UserExpirySeconds() 
        {
            var retrievedSeconds = Driver.User.PasswordExpirySeconds;
        }
    
        [When(@"users password set: {word}, {word}")]
        public void UsersPasswordSet(string username, string newPassword)
        {
            Driver.Users.SetPassword(username, newPassword);
        }
    
        [Then(@"users get user: {word}; throws exception")]
        public void UsersGetUserThrowsException(string username) 
        {
            Assert.Throws<TypeDBDriverException>(() => UsersGetUser(username));
        }
    
        [Then(@"users get all; throws exception")]
        public void UsersGetAllThrowsException() 
        {
            Assert.Throws<TypeDBDriverException>(() => UsersGetAll());
        }
    
        [Then(@"users contains: {word}; throws exception")]
        public void UsersContainsThrowsException(string username) 
        {
            Assert.Throws<TypeDBDriverException>(() => UsersContains(username));
        }
    
        [Then(@"users not contains: {word}; throws exception")]
        public void UsersNotContainsThrowsException(string username) 
        {
            Assert.Throws<TypeDBDriverException>(() => UsersNotContains(username));
        }
    
        [When(@"users create: {word}, {word}; throws exception")]
        public void UsersCreateThrowsException(string username, string password) 
        {
            Assert.Throws<TypeDBDriverException>(() => UsersCreate(username, password));
        }
    
        [When(@"users delete: {word}; throws exception")]
        public void UsersDeleteThrowsException(string username) 
        {
            Assert.Throws<TypeDBDriverException>(() => UsersDelete(username));
        }
    
        [When(@"user password update: {word}, {word}; throws exception")]
        public void UserPasswordUpdateThrowsException(string oldPassword, string newPassword)
        {
            Assert.Throws<TypeDBDriverException>(
                () => UserPasswordUpdate(oldPassword, newPassword));
        }
    
        [When(@"users password set: {word}, {word}; throws exception")]
        public void UsersPasswordSetThrowsException(string username, string passwordNew) 
        {
            Assert.Throws<TypeDBDriverException>(() => UsersPasswordSet(username, passwordNew));
        }
    }
}