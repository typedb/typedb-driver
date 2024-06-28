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

package main

import "C"
import (
    "fmt"
    "typedb_driver/go/connection"
)

func openCoreFunc() {
    dbName := "access-management-db"
    fmt.Println(dbName)
    serverAddr := "127.0.0.1:1729"

    driver := connection.NewTypeDBDriver(serverAddr)
    fmt.Println("hello")

    driver.Databases().Create(dbName)
    fmt.Println("db created")

    database := driver.Databases().Get(dbName)
    // TODO not stable, occasional runtime error: slice bounds out of range.
    fmt.Println(database)

    database.Delete()

    fmt.Println(driver.Databases().Contains(dbName))

    driver.Close()

}

func main() {
    openCoreFunc()
}
