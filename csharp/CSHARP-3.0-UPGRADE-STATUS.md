# C# Driver 3.0 Upgrade Status

## Current Status: BDD Tests Building, SWIG Crash Blocking Full Test Run

### Completed Work

#### API Changes for 3.0
- **Entry Point**: `TypeDB.Driver()` replaces `Drivers.CoreDriver()`
- **Session Layer**: Removed - transactions open directly from driver
- **Transaction Types**: Added `Schema` type (Read, Write, Schema)
- **Query API**: Unified `tx.Query(string)` returning `IQueryAnswer`
- **Answer Types**: `IsOk`, `IsConceptRows`, `IsConceptDocuments` with accessors
- **QueryOptions**: `IncludeInstanceTypes`, `PrefetchSize`, `IncludeQueryStructure`

#### Test Infrastructure Updates
1. **QuerySteps.cs**: Complete rewrite for 3.0 unified query API
   - Schema/Write/Read query execution steps
   - Answer type checking steps (ok, concept rows, concept documents)
   - Row access steps (get entity/attribute/relation by variable/index)
   - Query options steps
   - Parsing fails steps

2. **BUILD Files**: All updated for 3.0
   - Removed Session:steps references (sessions no longer exist)
   - Added Answer and Concept dependencies
   - Removed obsolete Get and RuleValidation tests

3. **Step Definitions Added**:
   - `connection reset database: <name>` - deletes and recreates database
   - `typeql schema/write/read query; parsing fails` - parsing error steps
   - All answer type and row access steps

### Building Successfully
- `//csharp/Test/Behaviour/Connection/Database:test-core`
- `//csharp/Test/Behaviour/Connection/Transaction:test-core`
- `//csharp/Test/Behaviour/Driver/Query:test-core`
- `//csharp/Test/Behaviour/Query/Language/Define:test-core`
- `//csharp/Test/Behaviour/Query/Language/Delete:test-core`
- `//csharp/Test/Behaviour/Query/Language/Expression:test-core`
- `//csharp/Test/Behaviour/Query/Language/Fetch:test-core`
- `//csharp/Test/Behaviour/Query/Language/Insert:test-core`
- `//csharp/Test/Behaviour/Query/Language/Match:test-core`
- `//csharp/Test/Behaviour/Query/Language/Modifiers:test-core`
- `//csharp/Test/Behaviour/Query/Language/Undefine:test-core`
- `//csharp/Test/Behaviour/Query/Language/Update:test-core`

### Test Results
Each test suite runs 1-2 scenarios successfully before native crash:
- **Database tests**: 7-13 pass, crash on test with transactions
- **Transaction tests**: 2 pass, crash on 3rd
- **Define tests**: 1 pass, crash on 2nd
- **Insert tests**: 1 pass, crash on 2nd

### Blocking Issue: SWIG Native Memory Corruption

#### Symptoms
- Tests crash with SIGABRT or SIGSEGV after 1-2 scenarios complete
- Crash occurs during driver creation for subsequent tests
- GC.Collect() makes crash happen sooner (confirms finalization issue)
- Delays between tests don't prevent crash

#### Root Cause Investigation
The crash occurs when:
1. First test creates driver + transactions
2. Test completes, driver/transaction closed
3. .NET GC eventually finalizes SWIG wrapper objects
4. Native library global state corrupted
5. Next test tries to create new driver -> crash

Evidence points to SWIG C# director callback handling and static callback maps in typedb_driver.i (`ThreadSafeTransactionCallbacks`).

#### What We Know
- Same Rust code works correctly with Java driver
- Issue is specific to SWIG C# binding layer
- BackgroundRuntime properly joins worker threads on Drop
- Not a simple race condition - appears to be memory corruption

### Remaining Work

#### Required to Fix SWIG Crash
1. Review `ThreadSafeTransactionCallbacks` implementation in typedb_driver.i
2. Check director callback finalizer ordering
3. Investigate static callback map lifetime vs native object lifetime
4. Consider explicit garbage collection barriers between tests
5. May need SWIG %finalizer directives or custom release patterns

#### After SWIG Fix
1. Add analyze API steps (get answers of typeql analyze)
2. Add concurrent query steps (concurrently get answers N times)
3. Fix Cloud test steps (ITypeDBDriver -> IDriver)
4. Update Concept tests for Instance terminology
5. Run full test suite

### Files Modified

#### Core API
- `csharp/Api/QueryOptions.cs` - Added IncludeQueryStructure
- `csharp/Api/ITypeDBTransaction.cs` - Query() method signature

#### Test Steps
- `csharp/Test/Behaviour/Query/QuerySteps.cs` - Complete rewrite
- `csharp/Test/Behaviour/Connection/ConnectionStepsBase.cs` - Null driver handling
- `csharp/Test/Behaviour/Connection/Database/DatabaseSteps.cs` - Reset database step
- `csharp/Test/Behaviour/Connection/Transaction/TransactionSteps.cs` - Cleaned up

#### BUILD Files (all updated)
- Removed Session:steps references
- Added Answer:answer and Concept:concept deps

### Run Commands
```bash
# Build all test-core targets
bazel build //csharp/Test/Behaviour/Connection/Database:test-core \
  //csharp/Test/Behaviour/Connection/Transaction:test-core \
  //csharp/Test/Behaviour/Driver/Query:test-core \
  //csharp/Test/Behaviour/Query/Language/...:test-core

# Run individual test (will crash after 1-2 scenarios)
bazel test //csharp/Test/Behaviour/Query/Language/Define:test-core --test_output=errors
```
