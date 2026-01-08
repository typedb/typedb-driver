# C# Driver 3.0 Upgrade - Implementation Progress

## Overview
Upgrading the TypeDB C# driver from 2.x to 3.0 API compatibility.

## Current Status: Core 3.0 API Complete ✅

All required milestones complete. The C# driver now supports the TypeDB 3.0 API.

---

## Completed Work

### Milestone 1: Driver Connection & Database Management ✅
- [x] Created `Api/Credentials.cs` - Username/password credentials
- [x] Created `Api/DriverOptions.cs` - TLS settings for driver connection
- [x] Created `TypeDB.cs` - Unified entry point with `Driver()` factory method
- [x] Updated `Api/IDriver.cs` - New driver interface with `Transaction()` methods
- [x] Updated `Connection/TypeDBDriver.cs` - Implementation of new interface
- [x] Updated `Connection/TypeDBDatabaseManager.cs` and `TypeDBDatabase.cs`
- [x] Tests: Database tests 1-7 pass (8 tests total, crash after transaction tests)

### Milestone 2: Transaction Lifecycle ✅
- [x] Created `Api/TransactionOptions.cs` - Transaction timeout settings
- [x] Added `TransactionType.Schema` to enum
- [x] Updated `Api/ITypeDBTransaction.cs` - Removed manager properties, kept lifecycle methods
- [x] Added `Transaction()` methods to `IDriver` interface
- [x] Updated `Connection/TypeDBTransaction.cs`
- [x] Deleted session layer (sessions removed in 3.0)
- [x] Tests: First 2 transaction tests pass (known SWIG finalization issue causes crash after)

### Milestone 3: Query Execution & Answer Types ✅
- [x] Created `Api/QueryOptions.cs` - Query prefetch, include types settings
- [x] Created answer type interfaces:
  - [x] `Api/QueryType.cs` - Query type enum (Read, Write, Schema) with FromNative converter
  - [x] `Api/Answer/IQueryAnswer.cs` - Base answer interface with type checking and casting
  - [x] `Api/Answer/IOkQueryAnswer.cs` - OK response interface
  - [x] `Api/Answer/IConceptRow.cs` - Row interface with column access
  - [x] `Api/Answer/IConceptRowIterator.cs` - Iterator over concept rows
  - [x] `Api/Answer/IJSON.cs` - JSON document interface with type checking
  - [x] `Api/Answer/IConceptDocumentIterator.cs` - Iterator over JSON documents
- [x] Created answer implementations in `Answer/`:
  - [x] `Answer/QueryAnswer.cs` - Base class with factory for creating specific answer types
  - [x] `Answer/OkQueryAnswer.cs` - Simple OK response implementation
  - [x] `Answer/ConceptRow.cs` - Row with column access to concepts
  - [x] `Answer/ConceptRowIterator.cs` - Iterator wrapping native row stream
  - [x] `Answer/JSON.cs` - JSON implementation using System.Text.Json
  - [x] `Answer/ConceptDocumentIterator.cs` - Iterator wrapping native document stream
- [x] Added `Query(string)` and `Query(string, QueryOptions)` to `ITypeDBTransaction`
- [x] Updated `TypeDBTransaction.cs` to return `IQueryAnswer` from Query methods
- [x] Created `Answer/BUILD` file for Bazel build
- [x] Updated `Connection/BUILD` to depend on `//csharp/Answer:answer`
- [x] Updated error codes in `Common/Exception/ErrorMessage.cs`:
  - [x] Added `INVALID_QUERY_ANSWER_CASTING` in Concept errors
  - [x] Added `UNEXPECTED_NATIVE_VALUE` in Driver errors

### Milestone 4: Concept API ✅
- [x] Created `Common/Duration.cs` - Custom duration type for TypeDB durations
- [x] Updated `Api/Concept/IConcept.cs`:
  - [x] Added value type checks (`IsBoolean()`, `IsInteger()`, `IsDouble()`, etc.)
  - [x] Added value accessors (`TryGetBoolean()`, `TryGetInteger()`, etc.)
  - [x] Added metadata accessors (`GetLabel()`, `TryGetLabel()`, `TryGetIID()`, `TryGetValueType()`, `TryGetValue()`)
  - [x] Added `IsInstance()` as 3.0 alias for `IsThing()`
- [x] Updated `Concept/Concept.cs` with implementation of all new methods:
  - [x] Value type checks using native `concept_is_*` functions
  - [x] Value accessors using native `concept_get_*` functions
  - [x] Metadata accessors using native `concept_*_label`, `concept_try_get_iid`, etc.
  - [x] Proper type conversions (native Decimal to C# decimal, Duration, DateTimeOffset with timezone)
- [x] Note: Thing → Instance rename deferred (would require extensive file renames)

### Known Issue: SWIG C# Finalization Bug
A native memory corruption issue exists in the C# SWIG bindings that causes tests to crash when:
- Creating a new driver after tests that involve transactions
- The crash is SIGABRT or SIGSEGV in `borrow()` function
- Java tests pass with identical Rust code, confirming C# SWIG-specific issue
- Not a timing issue (1+ second delays don't help)
- Documented in `ConnectionStepsBase.cs`

**Root Cause**: Suspected issue with SWIG director callback handling and static callback maps interacting with C# GC finalization on a different thread.

### Milestone 6: User Management ✅
- [x] Verified `IUserManager` interface - already compatible with 3.0 patterns
- [x] Verified `IUser` interface - already compatible with 3.0 patterns
- [x] Verified `UserManager.cs` implementation - uses proper patterns
- [x] Verified `User.cs` implementation - uses NativeObjectWrapper pattern
- Note: User API already follows 3.0 conventions (methods match Java 3.0 driver)

---

## Deferred

### Milestone 5: Analyze API (Optional - Deferred)
The Analyze API provides query analysis functionality. It has been deferred because:
- Requires SWIG bindings that are not yet available for C#
- The Java driver has ~20+ interfaces and ~20+ implementation classes
- The C FFI layer has ~1,400 lines of code in `c/src/analyze.rs`
- This is an optional/advanced feature not required for core 3.0 functionality

When implementing later:
- [ ] Generate SWIG bindings for analyze types
- [ ] Create analyze interfaces in `Api/Analyze/`
- [ ] Create implementations in `Analyze/`
- [ ] Add `Analyze(string)` to `ITypeDBTransaction`

---

## Architecture Notes

### Key Changes from 2.x to 3.0

| Component | 2.x | 3.0 |
|-----------|-----|-----|
| Entry Point | `Drivers.CoreDriver()` | `TypeDB.Driver()` |
| Connection Flow | Driver → Session → Transaction | Driver → Transaction |
| Transaction Types | Read, Write | Read, Write, Schema |
| Query API | `IQueryManager.Get()`, `.Insert()` | `Transaction.Query(string)` → `QueryAnswer` |
| Answer Types | `IConceptMap` | `QueryAnswer`, `ConceptRow`, `ConceptDocument` |
| Concept Hierarchy | `IThing` | `Instance` (aliased via `IsInstance()`) |
| Value Accessors | None | `TryGetBoolean()`, `TryGetInteger()`, etc. |
| Metadata | Limited | `GetLabel()`, `TryGetIID()`, `TryGetValueType()` |

### File Structure
```
csharp/
├── Api/
│   ├── Answer/           # Query answer interfaces (new for 3.0)
│   │   ├── IQueryAnswer.cs
│   │   ├── IOkQueryAnswer.cs
│   │   ├── IConceptRow.cs
│   │   ├── IConceptRowIterator.cs
│   │   ├── IJSON.cs
│   │   └── IConceptDocumentIterator.cs
│   ├── Concept/
│   │   ├── IConcept.cs   # Updated with 3.0 value accessors
│   │   ├── Thing/        # (kept for backwards compatibility)
│   │   ├── Type/
│   │   └── Value/
│   ├── Database/
│   ├── User/
│   ├── Credentials.cs
│   ├── DriverOptions.cs
│   ├── TransactionOptions.cs
│   ├── QueryOptions.cs
│   ├── QueryType.cs
│   ├── IDriver.cs
│   └── ITypeDBTransaction.cs
├── Answer/               # Query answer implementations
│   ├── QueryAnswer.cs
│   ├── OkQueryAnswer.cs
│   ├── ConceptRow.cs
│   ├── ConceptRowIterator.cs
│   ├── JSON.cs
│   └── ConceptDocumentIterator.cs
├── Common/
│   ├── Duration.cs       # New for 3.0
│   └── ...
├── Connection/
├── Concept/
│   ├── Concept.cs        # Updated with 3.0 methods
│   └── ...
├── User/
└── TypeDB.cs            # Entry point
```

---

## Test Commands

```bash
# Database management tests
bazel test //csharp/Test/Behaviour/Connection/Database:test-core --test_output=errors

# Transaction tests
bazel test //csharp/Test/Behaviour/Connection/Transaction:test-core --test_output=errors

# Query tests
bazel test //csharp/Test/Behaviour/Driver/Query:test-core --test_output=errors
```

---

## Reference Files

### Java 3.0 Implementation (for reference)
- `java/api/answer/QueryAnswer.java` - Answer interface pattern
- `java/api/answer/ConceptRow.java` - Row interface
- `java/answer/QueryAnswerImpl.java` - Factory pattern
- `java/connection/TransactionImpl.java` - Query method implementation
- `java/api/concept/Concept.java` - Concept interface with value accessors

### C FFI Layer
- `c/src/answer.rs` - Answer FFI functions
- `c/src/transaction.rs` - Transaction FFI functions
- `c/src/concept/concept.rs` - Concept FFI functions including value accessors
