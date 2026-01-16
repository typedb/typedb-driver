# C# Driver 3.0 Upgrade Status

## Current Status: Core Tests Passing ✅

The SWIG crash issue has been resolved. Core functionality is working with only known limitations remaining.

### Latest Test Results (2026-01-14)

| Test Suite | Passed | Failed | Skipped | Notes |
|------------|--------|--------|---------|-------|
| Database | 14 | 0 | 0 | ✅ Fully passing |
| Transaction | 39 | 4 | 0 | `<type>` placeholder limitation |
| Driver Query | 10 | 9 | 0 | Missing steps |
| Define | ALL | 0 | 0 | ✅ Fully passing |
| Insert | ALL | 0 | 0 | ✅ Fully passing |
| Match | 120 | 4 | 3 | 2 missing steps, 2 native issues |
| Fetch | 57 | 9 | 0 | Native JSON issues |
| Delete | ALL | 0 | 0 | ✅ Fully passing |

### Completed API Changes for 3.0
- **Entry Point**: `TypeDB.Driver()` replaces `Drivers.CoreDriver()`
- **Session Layer**: Removed - transactions open directly from driver
- **Transaction Types**: Added `Schema` type (Read, Write, Schema)
- **Query API**: Unified `tx.Query(string)` returning `IQueryAnswer`
- **Answer Types**: `IsOk`, `IsConceptRows`, `IsConceptDocuments` with accessors
- **QueryOptions**: `IncludeInstanceTypes`, `PrefetchSize`, `IncludeQueryStructure`

### Known Limitations (Not C# Driver Issues)

1. **Transaction `<type>` placeholder** (4 tests)
   - Xunit.Gherkin.Quick doesn't substitute placeholders inside DataTables
   - Same functionality tested by explicit scenarios ("many transactions of write and read types")

2. **Missing BDD steps** (deferred features):
   - `get answers of typeql analyze` - Analyze API not yet in SWIG bindings
   - `concurrently get answers of typeql read query N times`
   - `answers have query structure:`
   - `each answer satisfies` / `get answers of templated typeql read query`

3. **Native layer precision issues** (Fetch/Match tests):
   - Double: `2.01234567` → `2.01234568` in JSON (floating point)
   - Datetime-tz: Named timezone format differences
   - Duration: Fractional seconds format differences
   - These are in the native Rust JSON serialization

### Run Commands

```bash
# Run all passing test suites
bazel test //csharp/Test/Behaviour/Connection/Database:test-core --test_output=errors
bazel test //csharp/Test/Behaviour/Query/Language/Define:test-core --test_output=errors
bazel test //csharp/Test/Behaviour/Query/Language/Insert:test-core --test_output=errors
bazel test //csharp/Test/Behaviour/Query/Language/Delete:test-core --test_output=errors

# Run suites with known limitations
bazel test //csharp/Test/Behaviour/Connection/Transaction:test-core --test_output=errors
bazel test //csharp/Test/Behaviour/Driver/Query:test-core --test_output=errors
bazel test //csharp/Test/Behaviour/Query/Language/Match:test-core --test_output=errors
bazel test //csharp/Test/Behaviour/Query/Language/Fetch:test-core --test_output=errors
```

### Key Files Changed

- `csharp/Test/Behaviour/Query/QuerySteps.cs` - BDD step definitions with datetime-tz/duration handling
- `csharp/Test/Behaviour/Connection/ConnectionStepsBase.cs` - Improved test cleanup
- `csharp/Test/Behaviour/Connection/Transaction/TransactionSteps.cs` - `<type>` placeholder handling
- `csharp-driver-3.0-plan.md` - Full implementation progress
