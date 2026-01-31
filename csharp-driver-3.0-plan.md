# C# Driver 3.0 Upgrade - Implementation Progress

## Overview
Upgrading the TypeDB C# driver from 2.x to 3.0 API compatibility.

## Current Status: Core Tests Passing ✅

Core 3.0 API is complete. The SWIG crash issue has been resolved. Most tests are passing with only known limitations remaining.

### Latest Test Results (2026-01-14)

| Test Suite | Passed | Failed | Skipped | Notes |
|------------|--------|--------|---------|-------|
| Database | 14 | 0 | 0 | ✅ Fully passing |
| Transaction | 43 | 0 | 0 | ✅ Fully passing (DataTable placeholder workaround) |
| Driver Query | 10 | 9 | 0 | Missing steps (analyze, concurrent, structure) |
| Define | ALL | 0 | 0 | ✅ Fully passing |
| Insert | ALL | 0 | 0 | ✅ Fully passing |
| Match | 120 | 4 | 3 | 2 missing steps, 2 native precision issues |
| Fetch | 57 | 9 | 0 | Native JSON precision/format issues |
| Delete | ALL | 0 | 0 | ✅ Fully passing |

### Known Limitations (Not C# Driver Issues)

1. **Missing BDD steps** (deferred):
   - `get answers of typeql analyze` - Analyze API not implemented in SWIG
   - `concurrently get answers of typeql read query N times` - Concurrent step
   - `answers have query structure:` - Query structure inspection
   - `each answer satisfies` / `get answers of templated typeql read query` - Missing steps

2. **Native layer precision issues** (Fetch/Match tests):
   - Double precision: `2.01234567` serialized as `2.01234568` in JSON
   - Datetime-tz named timezone format differences
   - Duration fractional seconds format differences

   These are in the native Rust layer's JSON serialization, not the C# code

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
  - [x] Proper type conversions (native Decimal to C# decimal, Duration, DatetimeTZ with timezone)
- [x] Note: Thing → Instance rename deferred (would require extensive file renames)

### SWIG C# Finalization Bug - RESOLVED ✅
Previously there was a native memory corruption issue in the C# SWIG bindings that caused tests to crash when creating a new driver after tests involving transactions. This has been fixed (commit `388c9f5c Revert broken release_arc implementation`).

Tests now run to completion without SIGABRT/SIGSEGV crashes.

### Milestone 6: User Management ✅
- [x] Verified `IUserManager` interface - already compatible with 3.0 patterns
- [x] Verified `IUser` interface - already compatible with 3.0 patterns
- [x] Verified `UserManager.cs` implementation - uses proper patterns
- [x] Verified `User.cs` implementation - uses NativeObjectWrapper pattern
- Note: User API already follows 3.0 conventions (methods match Java 3.0 driver)

---

## TODO: Cross-Driver Fixes

### Value Equals/HashCode contract (Java driver)
**Issue**: `ConceptImpl.equals()` delegates to Rust's `concept_equals`, while `ValueImpl.hashCode()` uses `get().hashCode()`. Rust's `PartialEq` for `Value::DatetimeTZ` compares by UTC instant only (ignoring timezone), but Java's `ZonedDateTime.hashCode()` includes the zone. This violates the `equals`/`hashCode` contract.

**Fix**: Override `equals` in `ValueImpl` to use typed comparison (matching `hashCode`), same as C# and Python already do:
```java
// In ValueImpl.java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ValueImpl other = (ValueImpl) obj;
    return tryGetValueType().equals(other.tryGetValueType()) && get().equals(other.get());
}
```

**Status**: C# fixed (this branch). Python already correct (`_Value.__eq__` uses `self.get() == other.get()`). Java needs the fix.

**Files**: `java/concept/value/ValueImpl.java`

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
bazel test //csharp/Test/Behaviour/Connection/Database:test-community --test_output=errors

# Transaction tests
bazel test //csharp/Test/Behaviour/Connection/Transaction:test-community --test_output=errors

# Query tests
bazel test //csharp/Test/Behaviour/Driver/Query:test-community --test_output=errors
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
