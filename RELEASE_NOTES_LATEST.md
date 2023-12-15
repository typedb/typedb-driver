Documentation: https://typedb.com/docs/clients/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver


```sh
cargo add typedb-driver@2.26.1
```


### Java driver

Available through https://repo.vaticle.com
Documentation: https://typedb.com/docs/clients/java-driver

```xml
<repositories>
    <repository>
        <id>repo.vaticle.com</id>
        <url>https://repo.vaticle.com/repository/maven/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupid>com.vaticle.typedb</groupid>
        <artifactid>typedb-driver</artifactid>
        <version>2.26.1</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver

Available through https://pypi.org

```
pip install typedb-driver==2.26.1
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver

```
npm install typedb-driver@
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://github.com/vaticle/typedb-driver/releases/tag/2.26.1

### C driver

Compiled distributions comprising headers and shared libraries available at: https://github.com/vaticle/typedb-driver/releases/tag/2.26.1



## New Features
  
- **Introduce C++ driver**
  Introduce the C++ driver for TypeDB. It is built against the C++17 standard and distributed as an archive containing the headers (under `/include` & a shared library under `/lib`). 
  
  **Usage:** As usual, add the headers paths to your include path in the compile step & the library to your link step. For windows, the 'import-lib' `typedb-driver-cpp-<platform>.if.lib` is included to link against.
  
  **Architecture:** The C++ driver is a thin wrapper around the TypeDB rust driver, introducing classes for a more intuitive interface. Each C++ object holds a unique pointer to the corresponding native rust object and is the unique owner of that rust object. To ensure this, we enforce move-semantics on the C++ objects. The rust object is freed when the C++ object owning it is destructed. Any error encountered will throw a `TypeDB::DriverException`. Note that methods which return `Iterable` or `Future` which encounter a server-side error will only throw when they are evaluated (using `begin` or `get` respectively).
  
  **Example:**
  ```cpp
  // All files are included from typedb.hpp
  #include <typedb.hpp>
  
  int main() {
      TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
      std::string dbName = "move-example";
      TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
  
      driver.databases.create(dbName); 
      TypeDB::Database db = driver.databases.get(dbName);
      // Database db1 = db; // Copying is disabled: Produces a compiler error.
      TypeDB::Database db1 = std::move(db); // Moves ownership from db to db1
      try {
          std::cout << db.name() << std::endl; // db is no longer valid
      } catch (TypeDB::DriverException e) {
          // C++ Internal Error: The object does not have a valid native handle. It may have been:  uninitialised, moved or disposed
          std::cerr << "Caught exception: " << e.message() << std::endl;
      }
      std::cout << "db.name(): " << db1.name() << std::endl; // Ok: Prints 'move-objects'
      return 0;
  }
  ```
  
  

## Bugs Fixed
- **Java JNI library loading: fallback when platform not specified**
  
  Previously, the JNI library would be selected based on if the containing JAR contains the expected platform string. When TypeDB Driver Java is repackaged by the end user, the JNI library is relocated and is likely missing the platform specification in its path. Now, if only one native library candidate is found in classpath, we attempt to use that rather than fail.
  
  

## Code Refactors
- **Replace all instances of 'enterprise' with 'cloud'**
  
  We replace the term 'enterprise' with 'cloud', to reflect the new consistent terminology used throughout Vaticle.

- **C++ driver UX improvements**
  Add a few missing APIs, and easier-to-use function variants.



## Other Improvements
  
- **Fix circleci assembly tests for C++ driver**
  Fix assembly test paths broken in previous commit
  
- **Release pipeline for C++ driver**
  Introduce build targets & jobs for the release pipeline of the C++ driver
  