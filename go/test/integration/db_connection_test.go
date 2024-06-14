package main

import (
	"testing"
	"typedb_driver/go/connection"
)

func TestDBConnection(t *testing.T) {
	dbName := "test-db"
	serverAddr := "127.0.0.1:1729"

	driver := connection.NewTypeDBDriver(serverAddr)

	driver.Databases().Create(dbName)
	if !driver.Databases().Contains(dbName) {
		t.Errorf("Expected driver.Databases to contain '%s'", dbName)
	}

	database := driver.Databases().Get(dbName)
	if database.Name() != "dbName" {
		t.Errorf("Expected databaseName to be '%s', got '%s", dbName, database.Name())
	}

	database.Delete()

	if driver.Databases().Contains(dbName) {
		t.Errorf("Expected database to be deleted, but exists as: '%s'", dbName)
	}

	driver.Close()
}

