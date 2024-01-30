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

using System;
using System.Collections.Generic;
using System.Threading;
using Xunit.Gherkin.Quick;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Common;
using com.vaticle.typedb.driver.Test.Behaviour.Connection;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection
{
    public abstract class ConnectionStepsBase: Feature, IDisposable
    {
        // TODO: ThreadPool is static and doesn't seem to require setting specific value!
//        public static int ThreadPoolSize = 32;
        public static ITypeDBDriver Driver;
        public static List<ITypeDBSession> Sessions = new List<ITypeDBSession>();
//        public static List<CompletableFuture<TypeDBSession>> sessionsParallel = new ArrayList<>();
//        public static Dictionary<TypeDBSession, List<TypeDBTransaction>> sessionsToTransactions = new HashMap<>();
//        public static Dictionary<TypeDBSession, List<CompletableFuture<TypeDBTransaction>>> sessionsToTransactionsParallel = new HashMap<>();
//        public static Dictionary<CompletableFuture<TypeDBSession>, List<CompletableFuture<TypeDBTransaction>>> sessionsParallelToTransactionsParallel = new HashMap<>();
        public static TypeDBOptions SessionOptions;
        public static TypeDBOptions TransactionOptions;
        private static bool s_isBeforeAllRan = false;

//        public static readonly Dictionary<string, BiConsumer<TypeDBOptions, String>> optionSetters = map(
//                pair("session-idle-timeout-millis", (option, val) -> option.sessionIdleTimeoutMillis(Integer.parseInt(val))),
//                pair("transaction-timeout-millis", (option, val) -> option.transactionTimeoutMillis(Integer.parseInt(val)))
//        );

        public static readonly Dictionary<string, string> ServerOptions =
        new Dictionary<string, string>{
            {"--diagnostics.reporting.enable", "false"},
        };

//        public static TypeDBTransaction Tx()
//        {
//            return SessionsToTransactions.Get(sessions.get(0)).get(0);
//        }

        protected ConnectionStepsBase()
        {
            BeforeAllOnce();

            SessionOptions = CreateOptions().Infer(true);
            TransactionOptions = CreateOptions().Infer(true);

            Console.WriteLine("ConnectionSteps.Before");
        }

        protected virtual void BeforeAllOnce()
        {
            if (s_isBeforeAllRan)
            {
                return;
            }

            s_isBeforeAllRan = true;
//            TypeDBSingleton.deleteTypeDBRunner();
        }

        public virtual void Dispose()
        {
//            Sessions.ParallelStream().forEach(TypeDBSession::close);
//            Sessions.Clear();
//
//            Stream<CompletableFuture<Void>> closures = sessionsParallel
//                    .stream().map(futureSession -> futureSession.thenApplyAsync(session -> {
//                        session.close();
//                        return null;
//                    }));
//            CompletableFuture.allOf(closures.toArray(CompletableFuture[]::new)).join();
//            sessionsParallel.clear();
//            sessionsToTransactions.clear();
//            sessionsToTransactionsParallel.clear();
//            sessionsParallelToTransactionsParallel.clear();
//            driver = createTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address());
//            driver.databases().all().forEach(Database::delete);
//            driver.close();

            Console.WriteLine("ConnectionSteps.After");
        }

        public abstract void CreateTypeDBDriver(string address);

        internal TypeDBOptions CreateOptions()
        {
            return new TypeDBOptions();
        }

        public virtual void TypeDBStarts()
        {
//            TypeDBRunner runner = TypeDBSingleton.getTypeDBRunner();
//            if (runner != null && runner.isStopped())
//            {
//                runner.start();
//            }
            Console.WriteLine("TYPEDBSTARTS!");
        }

        public abstract void ConnectionOpensWithDefaultAuthentication();

        public virtual void ConnectionHasBeenOpened()
        {
//            assertNotNull(driver);
//            assertTrue(driver.isOpen());
Console.WriteLine("ConnectionSteps.ConnectionHasBeenOpened");
        }

        public virtual void ConnectionCloses()
        {
            Driver.Close();
            Driver = null;
            Console.WriteLine("ConnectionCloses");
        }

        public virtual void ConnectionDoesNotHaveAnyDatabase()
        {
//            assertNotNull(driver);
//            assertTrue(driver.isOpen());
//            assertTrue(driver.databases().all().isEmpty());
Console.WriteLine("ConnectionSteps.ConnectionDoesNotHaveAnyDatabase");
        }
    }
}
