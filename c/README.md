# TypeDB C Driver
The TypeDB C driver provides C bindings to the rust driver. 
It also serves as the base on which the other wrapper drivers are built.

## Usage
The driver is distributed as an archive containing the headers & a shared library.
```
|- include/
|  |- typedb_driver.h
|
|- lib/
   |- libtypedb_driver_clib.<ext>
```

As usual, add the include paths to your compile step & the library to your link step. For Windows, the 'import-lib' `typedb_driver_clib.dll.lib` is provided to link against.
A sample [CMakeLists.txt](https://github.com/vaticle/typedb-driver/blob/master/c/tests/assembly/CMakeLists.txt) file is available in the TypeDB Driver repository.
Code examples can be found in the [integration tests](https://github.com/vaticle/typedb-driver/blob/master/c/tests/integration/driver_test.c). 

## Architecture
### Interface
`typedb_driver.h` is a single header containing all the functions needed for a complete TypeDB driver.

Functions parameters & return values are either primitives or pointers to opaque structs, e.g.:
```c
Connection *connection_open_core(const char *address);
``` 

These pointers are then used for further operations:
```c
    char* dbName = "hello";
    Connection *connection = connection_open_core("127.0.0.1:1729");
    DatabaseManager* databaseManager = database_manager_new(connection);
    databases_create(databaseManager, dbName);
    Database* database = databases_get(databaseManager, dbName);
    char* gotName = database_name(database);
    assert(0 == strcmp(gotName, dbName));
    ...
```

### Memory management
All pointers returned by the driver point to memory managed by rust. 
To free the memory, the rust driver provides `*_drop` methods for each type of pointer.
A `char*` returned from rust must be freed using `void string_free(char*)`.
Types which have `*_close` methods will be freed on close. 
```c
    ...
    string_free(gotName);
    database_close(database);
    database_manager_drop(databaseManager);
    connection_close(connection);
```

### Error handling
To check if an error has occurred, use `bool check_error()`
```c
if (check_error()) {
    Error* error = get_last_error();
    char* errcode = error_code(error);
    char* errmsg = error_message(error);
    // Handle error...
    string_free(errmsg);
    string_free(errcode);
    error_drop(error);
}
```

### Iterators and promises
Iterators can be forwarded with the `*_iterator_next` method,
which returns `NULL` when the end has been reached.
```c
    DatabaseIterator* it = databases_all(databaseManager);
    Database* database = NULL;
    while (NULL != (database = database_iterator_next(it))) {
        ...
    }
    database_iterator_drop(it);
```
Promises are resolved with the `*_promise_resolve` method, which also frees the memory.
```c
Transaction* tx = ...;
...
VoidPromise* promise = transaction_commit(tx); 
void_promise_resolve(promise);
if (check_error()) { /* handle */ }
```
**Note**: Server-side errors will only be available when the promise is resolved 
or the first element in the iterator is requested (by calling`_next`). You must do this to 
know whether an operation succeeded, regardless of your interest in the result. 
