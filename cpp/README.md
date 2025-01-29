# TypeDB C++ Driver
The C++ driver is built against the C++17 standard.

## Usage
The driver is distributed as an archive containing the headers & a shared library.
```
|- README.md
|- include/
|  |- typedb_driver.hpp
|  |- ...
|
|- lib/
   |- typedb-driver-cpp-<platform>.<ext>
```

As usual, Add the include paths to your compile step & the library to your link step. For windows, the 'import-lib' `typedb-driver-cpp-<platform>.if.lib` is provided to link against.
A [sample `CMakeLists`](https://github.com/typedb/typedb-driver/blob/master/cpp/test/assembly/CMakeLists.txt) is available on the TypeDB Driver repository.

## API Reference
To learn about the methods available for executing queries and retrieving their answers using C++, refer to the [API Reference](https://typedb.com/docs/drivers/cpp/api-reference).

## Driver Architecture
The C++ driver is a thin wrapper around the TypeDB rust driver, introducing classes for a more intuitive interface. Each C++ object holds a unique pointer to the corresponding native rust object and is the unique owner of that rust object. To ensure this, we enforce move-semantics on the C++ objects. The rust object is freed when the C++ object owning it is destructed.

Any error encountered will throw a `TypeDB::DriverException`. Note that methods which return `Iterable` or `Future` which encounter a server-side error will only throw when they are evaluated (using `begin` or `get` respectively).


## Build TypeDB Driver for C++ from Source

1. Make sure you have [Bazel](https://docs.bazel.build/versions/master/install.html) installed on your machine.
2. Build the shared-library:

   a) Building just the shared library:
   ```
   bazel build //cpp:typedb-driver-cpp
   ```
   The shared library will be produced at: `bazel-bin/cpp/typedb-driver-cpp.{so,dylib,dll}`
   For windows, you will also need the import library at `bazel-bin/cpp/typedb-driver-cpp.if.lib`
   The headers are located at `cpp/include`.

   b) You can also package these into an archive. The target depends on your architecture.
   ```
   bazel build //cpp/:assemble-linux-x86_64-targz
   bazel build //cpp/:assemble-linux-arm64-targz
   bazel build //cpp/:assemble-mac-x86_64-zip
   bazel build //cpp/:assemble-mac-arm64-zip
   bazel build //cpp/:assemble-windows-x86_64-zip
   ```
   **Note:** The toolchain is not configured to cross-compile.   
   The archive will be produced under `bazel-bin/cpp/typedb-driver-cpp-<os>-<arch>.<ext>` and will contain the `include` and `lib` folders as in the distribution.


## Example usage
```cpp
// All files are included from typedb_driver.hpp
#include <typedb_driver.hpp>

int main() {
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
    ...
    return 0;
}
```


### Move semantics on standard objects
Most C++ methods will return a C++ object.
```cpp
    std::string dbName = "move-objects";
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");

    driver.databases.create(dbName); 
    TypeDB::Database db = driver.databases.get(dbName);
    // Database db1 = db; // Produces a compiler error.
    TypeDB::Database db1 = std::move(db); // Moves ownership from db to db1.
    try {
        std::cout << db.name() << std::endl; // db is no longer valid
    } catch (TypeDB::DriverException e) {
        // C++ Internal Error: The object does not have a valid native handle. It may have been:  uninitialised, moved or disposed
        std::cerr << "Caught exception: " << e.message() << std::endl;
    }
    std::cout << "db.name(): " << db1.name() << std::endl; // Ok: Prints 'move-objects'


```

### Functions returning iterables
```cpp
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
    std::vector<std::string> toCreate = {"iterators-db1", "iterators-db2" };

    for (auto& dbName: toCreate) {
        driver.databases.create(dbName); 
    }

    // One way
    {
        std::vector<TypeDB::Database> databases;
        for (auto& database: driver.databases.all()) {
            databases.push_back(std::move(database)); // Needs to be moved
        }

        assert(std::all_of(toCreate.begin(), toCreate.end(), [&](const std::string& dbName) {
            return std::any_of(databases.begin(), databases.end(), [&](const TypeDB::Database& db){
                return db.name() == dbName;
            });
        }));
    }
    // Other way
    {
        std::vector<TypeDB::Database> databases;
        TypeDB::DatabaseIterable databaseIterable = driver.databases.all();
        for (auto it = databaseIterable.begin(); it != databaseIterable.end(); ++it) { // it++ is deleted. Use ++it
            databases.emplace_back(std::move(*it)); 
        }

        assert(std::all_of(toCreate.begin(), toCreate.end(), [&](const std::string& dbName) {
            return std::any_of(databases.begin(), databases.end(), [&](const TypeDB::Database& db){
                return db.name() == dbName;
            });
        }));
    }

    {
        // Not yet supported:
        // TypeDB::DatabaseIterable databaseIterable = driver.databases.all();
        // std::vector<TypeDB::Database> databases(databaseIterable.begin(), databaseIterable.end());
    }
```

### Functions returning concepts
Functions returning concepts are exceptional in that they return `std::unique_ptr<T>` where T is some subtype of `TypeDB::Concept`. The reason for this is to allow the easy casting to more specific types. Be mindful of ownership when dealing with them (see the last section of the code below)
```cpp
    std::string dbName = "concepts";
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
    
    driver.databases.create(dbName);
    TypeDB::Options options;
    {
        auto sess = driver.session(dbName, TypeDB::SessionType::SCHEMA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::WRITE, options);
        tx.query.define("define my-long sub attribute, value long;", options).get();  // Wait evaluates the future
        tx.commit();
    }

    {
        auto sess = driver.session(dbName, TypeDB::SessionType::DATA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::READ, options);

        TypeDB::ConceptMapIterable result = tx.query.get("match $t sub! attribute; get;", options);
        for (TypeDB::ConceptMap& cm : result) {
            std::unique_ptr<Concept> t = cm.get("t");
            std::cout << t->asAttributeType()->getLabel() << " : " << (t->asAttributeType()->getValueType() == TypeDB::ValueType::LONG) << std::endl;
        }

        // BE CAREFUL NOT TO DO THIS:
        if (false /* or you want to get segfaults */) {
            TypeDB::ConceptMapIterable again = tx.query.get("match $t sub! attribute; get;", options);
            for (TypeDB::ConceptMap& cm : again) {
                AttributeType* attrType = cm.get("t")->asAttributeType(); 
                // Because ownership is not taken, std::unique_ptr is freed immediately, leaving attrType dangling.
                std::cout << attrType->getLabel() << " : " << (attrType->getValueType() == TypeDB::ValueType::LONG) << std::endl;
            }
        }
    }
```

### TypeDB Cluster 
Connect to TypeDB Cluster instances using `TypeDB::Driver::cloudDriver`
```cpp
    std::string dbName = "cloud-database";
    // Since we're using a self-signed certificate for encryption, we pass the path to the root-ca through an environment variable
    TypeDB::Credential creds("admin", "password", true, std::getenv("PATH_TO_ROOT_CA"));
    TypeDB::Driver driver = TypeDB::Driver::cloudDriver({"localhost:11729"}, creds);
    // You can also specify all node addresses TypeDB::Driver::cloudDriver({"localhost:11729, localhost:21729, localhost:31729"}, creds);

    driver.databases.create(dbName);
    TypeDB::Database db = driver.databases.get(dbName);
    std::cout << "Found replicas:" << std::endl;
    for (TypeDB::ReplicaInfo& replica : db.replicas()) {
        std::cout << "- " << replica.address() << " : " << replica.isPrimary() << std::endl;
    }
```
