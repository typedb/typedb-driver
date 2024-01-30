using System;
using System.Collections.Generic;
using Xunit.Gherkin.Quick;

using com.vaticle.typedb.driver;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Database
{
    [FeatureFile("external/vaticle_typedb_behaviour/connection/database.feature")]
    public sealed class DatabaseTest : Feature
    {
        private string _result;

        [Given(@"typedb starts")]
        public void TypeDBStarts()
        {
            Console.WriteLine("TypeDB Starts!");
//            TypeDBRunner runner = TypeDBSingleton.getTypeDBRunner();
//            if (runner != null && runner.isStopped()) {
//                runner.start();
//            }
        }

        [Given(@"connection opens with default authentication")]
        public void ConnectionOpensWithDefaultAuthentication()
        {
            Console.WriteLine("Connection opens with default authentication!");
//            driver = createTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address());
        }

        [When(@"connection closes")]
        public void ConnectionCloses()
        {
            Console.WriteLine("Connection closes!");
//            super.connection_closes();
        }

        [Given(@"connection has been opened")]
        public void ConnectionHasBeenOpened()
        {
            Console.WriteLine("Connection has been opened!");
//            super.connection_has_been_opened();
        }

        [Given(@"connection does not have any database")]
        public void ConnectionDoesNotHaveAnyDatabase()
        {
            Console.WriteLine("Connection does not have any database!");
//            super.connection_does_not_have_any_database();
        }
    
        [When(@"connection create database: {word}")]
        public void ConnectionCreateDatabase(string name)
        {
            Console.WriteLine("Connection create database!" + name);
//            connection_create_databases(list(name));
        }
        
        [When(@"connection create database(s):")]
        public void ConnectionCreateDatabases(List<string> names)
        {
            foreach (string name in names)
            {
                Console.WriteLine("Connection create database(s): " + name);
//                driver.databases().create(name);
            }
        }

        [When(@"connection create databases in parallel:")]
        public void ConnectionCreateDatabasesInParallel(List<string> names)
        {
            Console.WriteLine("ConnectionCreateDatabasesInParallel: " + names);
//            assertTrue(THREAD_POOL_SIZE >= names.size());
//
//            CompletableFuture[] creations = names.stream()
//                    .map(name -> CompletableFuture.runAsync(() -> driver.databases().create(name), threadPool))
//                    .toArray(CompletableFuture[]::new);
//
//            CompletableFuture.allOf(creations).join();
        }

        [When(@"connection delete database: {word}")]
        public void ConnectionDeleteDatabase(string name)
        {
            Console.WriteLine("ConnectionCreateDatabasesInParallel: " + name);
//            connection_delete_databases(list(name));
        }

        [When(@"connection delete database(s):")]
        public void ConnectionDeleteDatabases(List<string> names)
        {
            foreach (string databaseName in names)
            {
                Console.WriteLine("ConnectionDeleteDatabases: " + databaseName);
//                driver.databases().get(databaseName).delete();
            }
        }

        [Then(@"connection delete database; throws exception: {word}")]
        public void ConnectionDeleteDatabaseThrowsException(string name)
        {
            Console.WriteLine("ConnectionDeleteDatabaseThrowsException: " + name);
//            connection_delete_databases_throws_exception(list(name));
        }

        [Then(@"connection delete database(s); throws exception")]
        public void ConnectionDeleteDatabasesThrowsException(List<string> names)
        {
            foreach (string databaseName in names)
            {
                Console.WriteLine("ConnectionDeleteDatabasesThrowsException: " + databaseName);
//                try {
//                    driver.databases().get(databaseName).delete();
//                    fail();
//                } catch (Exception e) {
//                    // successfully failed
//                }
            }
        }

        [When(@"connection delete databases in parallel:")]
        public void ConnectionDeleteDatabasesInParallel(List<string> names)
        {
            Console.WriteLine("ConnectionDeleteDatabasesInParallel: " + names);
//            assertTrue(THREAD_POOL_SIZE >= names.size());
//
//            CompletableFuture[] deletions = names.stream()
//                    .map(name -> CompletableFuture.runAsync(() -> driver.databases().get(name).delete(), threadPool))
//                    .toArray(CompletableFuture[]::new);
//
//            CompletableFuture.allOf(deletions).join();
        }

        [When(@"connection has database: {word}")]
        public void ConnectionHasDatabase(string name)
        {
            Console.WriteLine("ConnectionHasDatabase: " + name);
//            connection_has_databases(list(name));
        }

        [Then(@"connection has database(s):")]
        public void ConnectionHasDatabases(List<string> names)
        {
            Console.WriteLine("ConnectionHasDatabases: " + names);
//            assertEquals(set(names), driver.databases().all().stream().map(Database::name).collect(Collectors.toSet()));
        }

        [Then(@"connection does not have database: {word}")]
        public void ConnectionDoesNotHaveDatabase(string name)
        {
            Console.WriteLine("ConnectionDoesNotHaveDatabase: " + name);
//            connection_does_not_have_databases(list(name));
        }

        [Then(@"connection does not have database(s):")]
        public void ConnectionDoesNotHaveDatabases(List<string> names)
        {

//            Set<String> databases = driver.databases().all().stream().map(Database::name).collect(Collectors.toSet());
            foreach (string databaseName in names)
            {
                Console.WriteLine("ConnectionDoesNotHaveDatabases: " + databaseName);
//                assertFalse(databases.contains(databaseName));
            }
        }
    }
}
