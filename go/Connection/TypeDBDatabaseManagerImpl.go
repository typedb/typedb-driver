package Connection

import "C"
import (
	"typedb_driver/go/api/database"
	"typedb_driver/go_wrapper"
)

type TypeDBDatabaseManagerImpl struct {
	nativeConnection typedb_driver.Connection
	nativeObject typedb_driver.DatabaseManager
}

func NewTypeDBDatabaseManagerImpl(nativeConnection typedb_driver.Connection) *TypeDBDatabaseManagerImpl {
	return &TypeDBDatabaseManagerImpl{
		nativeConnection: nativeConnection,
		nativeObject: typedb_driver.Database_manager_new(nativeConnection),
	}
}

func (dbManager *TypeDBDatabaseManagerImpl) Get(name string) database.Database {
	// TODO deal with errors
	return NewTypeDBDatabaseImpl(typedb_driver.Databases_get(dbManager.nativeObject, name))
}

func (dbManager *TypeDBDatabaseManagerImpl) Contains(name string) bool {
	return typedb_driver.Databases_contains(dbManager.nativeObject, name)
}

func (dbManager *TypeDBDatabaseManagerImpl) Create(name string) {
	typedb_driver.Databases_create(dbManager.nativeObject, name)
}

func (dbManager *TypeDBDatabaseManagerImpl) All() []database.Database{
	//return new NativeIterator<>(databases_all(nativeObject)).stream().map(TypeDBDatabaseImpl::new).collect(toList());
	//TODO implement - dealing with iterator
	return nil
}
