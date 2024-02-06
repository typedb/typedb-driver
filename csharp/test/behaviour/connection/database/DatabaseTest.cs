using DataTable = Gherkin.Ast.DataTable;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using com.vaticle.typedb.driver.Test.Behaviour.Connection;
using com.vaticle.typedb.driver.Test.Behaviour.Connection.Database;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Database
{
    [FeatureFile("external/vaticle_typedb_behaviour/connection/database.feature")]
    public class DatabaseTest : Feature, IDisposable, IClassFixture<DatabaseSteps>, IClassFixture<ConnectionSteps>
    {
        public DatabaseTest() 
            : base()
        {
            _connectionSteps = new ConnectionSteps();
            _databaseSteps = new DatabaseSteps();
        }
        
        public void Dispose()
        {
            _connectionSteps.Dispose();
        }
        
        [Given(@"typedb starts")]
        [When(@"typedb starts")]
        public void TypeDBStarts()
            => _connectionSteps.TypeDBStarts();

        [Given(@"connection opens with default authentication")]
        [When(@"connection opens with default authentication")]
        public void ConnectionOpensWithDefaultAuthentication()
            => _connectionSteps.ConnectionOpensWithDefaultAuthentication();

        [When(@"connection opens with authentication: {word}, {word}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
            => _connectionSteps.ConnectionOpensWithAuthentication(username, password);

        [Given(@"connection has been opened")]
        public void ConnectionHasBeenOpened()
            => _connectionSteps.ConnectionHasBeenOpened();

        [When(@"connection closes")]
        public void ConnectionCloses()
            => _connectionSteps.ConnectionCloses();

        [Given(@"connection create database: {word}")]
        [When(@"connection create database: {word}")]
        public void ConnectionCreateDatabase(string name)
            => _databaseSteps.ConnectionCreateDatabase(name);

        [Given(@"connection create databases:")]
        [When(@"connection create databases:")]
        public void ConnectionCreateDatabases(DataTable names)
            => _databaseSteps.ConnectionCreateDatabases(names);

        [Given(@"connection create databases in parallel:")]
        [When(@"connection create databases in parallel:")]
        public void ConnectionCreateDatabasesInParallel(DataTable names)
            => _databaseSteps.ConnectionCreateDatabasesInParallel(names);

        [When(@"connection delete database: {word}")]
        public void ConnectionDeleteDatabase(string name)
            => _databaseSteps.ConnectionDeleteDatabase(name);

        [When(@"connection delete databases:")]
        public void ConnectionDeleteDatabases(DataTable names)
            => _databaseSteps.ConnectionDeleteDatabases(names);

        [When(@"connection delete database; throws exception: {word}")]
        public void ConnectionDeleteDatabaseThrowsException(string databaseName)
            => _databaseSteps.ConnectionDeleteDatabaseThrowsException(databaseName);

        [When(@"connection delete databases in parallel:")]
        public void ConnectionDeleteDatabasesInParallel(DataTable names)
            => _databaseSteps.ConnectionDeleteDatabasesInParallel(names);

        [Then(@"connection has database: {word}")]
        public void ConnectionHasDatabase(string name)
            => _databaseSteps.ConnectionHasDatabase(name);

        [Then(@"connection has databases:")]
        public void ConnectionHasDatabases(DataTable names)
            => _databaseSteps.ConnectionHasDatabases(names);

        [Then(@"connection does not have database: {word}")]
        public void ConnectionDoesNotHaveDatabase(string name)
            => _databaseSteps.ConnectionDoesNotHaveDatabase(name);

        [Then(@"connection does not have databases:")]
        public void ConnectionDoesNotHaveDatabases(DataTable names)
            => _databaseSteps.ConnectionDoesNotHaveDatabases(names);

        [Given(@"connection does not have any database")]
        [Then(@"connection does not have any database")]
        public void ConnectionDoesNotHaveAnyDatabase()
            => _databaseSteps.ConnectionDoesNotHaveAnyDatabase();

        private readonly DatabaseSteps _databaseSteps;
        private readonly ConnectionSteps _connectionSteps;
    }
}
