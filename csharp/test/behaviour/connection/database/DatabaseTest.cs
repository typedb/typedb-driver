using DataTable = Gherkin.Ast.DataTable;
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
        [Then(@"connection does not have any database")]
        public void ConnectionDoesNotHaveAnyDatabase()
        {
            Console.WriteLine("Connection does not have any database!");
//            super.connection_does_not_have_any_database();
        }

        [Given(@"connection create database: {word}")]
        [When(@"connection create database: {word}")]
        public void ConnectionCreateDatabase(string name)
        {
            Console.WriteLine("Connection create database!" + name);
//            connection_create_databases(list(name));
        }

        [Given(@"connection create databases:")]
        [When(@"connection create databases:")]
        public void ConnectionCreateDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    Console.WriteLine("ConnectionCreateDatabases: " + cell.Value);
                }
            }
//            foreach (string name in names)
//            {
//                driver.databases().create(name);
//            }
        }

        [Given(@"connection create databases in parallel:")]
        [When(@"connection create databases in parallel:")]
        public void ConnectionCreateDatabasesInParallel(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    Console.WriteLine("ConnectionCreateDatabasesInParallel: " + cell.Value);
                }
            }
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

        [When(@"connection delete databases:")]
        public void ConnectionDeleteDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    Console.WriteLine("ConnectionDeleteDatabases: " + cell.Value);
                }
            }
//            foreach (string databaseName in names)
//            {
//                driver.databases().get(databaseName).delete();
//            }
        }

        [When(@"connection delete database; throws exception: {word}")]
        public void ConnectionDeleteDatabaseThrowsException(string name)
        {
            Console.WriteLine("ConnectionDeleteDatabaseThrowsException: " + name);
//            connection_delete_databases_throws_exception(list(name));
        }

        [Then(@"connection delete database(s); throws exception")]
        public void ConnectionDeleteDatabasesThrowsException(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    Console.WriteLine("ConnectionDeleteDatabasesThrowsException: " + cell.Value);
                }
            }
//            foreach (string databaseName in names)
//            {
//                try {
//                    driver.databases().get(databaseName).delete();
//                    fail();
//                } catch (Exception e) {
//                    // successfully failed
//                }
//            }
        }

        [When(@"connection delete databases in parallel:")]
        public void ConnectionDeleteDatabasesInParallel(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    Console.WriteLine("ConnectionDeleteDatabasesInParallel: " + cell.Value);
                }
            }

//            assertTrue(THREAD_POOL_SIZE >= names.size());
//
//            CompletableFuture[] deletions = names.stream()
//                    .map(name -> CompletableFuture.runAsync(() -> driver.databases().get(name).delete(), threadPool))
//                    .toArray(CompletableFuture[]::new);
//
//            CompletableFuture.allOf(deletions).join();
        }

        [Then(@"connection has database: {word}")]
        public void ConnectionHasDatabase(string name)
        {
            Console.WriteLine("ConnectionHasDatabase: " + name);
//            connection_has_databases(list(name));
        }

        [Then(@"connection has databases:")]
        public void ConnectionHasDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    Console.WriteLine("ConnectionHasDatabases: " + cell.Value);
                }
            }
//            assertEquals(set(names), driver.databases().all().stream().map(Database::name).collect(Collectors.toSet()));
        }

        [Then(@"connection does not have database: {word}")]
        public void ConnectionDoesNotHaveDatabase(string name)
        {
            Console.WriteLine("ConnectionDoesNotHaveDatabase: " + name);
//            connection_does_not_have_databases(list(name));
        }

        [Then(@"connection does not have databases:")]
        public void ConnectionDoesNotHaveDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    Console.WriteLine("ConnectionDoesNotHaveDatabases: " + cell.Value);
                }
            }
//            Set<String> databases = driver.databases().all().stream().map(Database::name).collect(Collectors.toSet());
//            foreach (string databaseName in names)
//            {
//                assertFalse(databases.contains(databaseName));
//            }
        }
    }
}
