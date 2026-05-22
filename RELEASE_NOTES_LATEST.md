Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Breaking changes
This release breaks backwards compatibility.
This version is only compatible with TypeDB server versions >= 3.11.0.
Connections to older servers will be rejected.

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.11.2
```


### Java driver
q
Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.11.2/a=noarch;xg=com.typedb/)
Documentation: https://typedb.com/docs/drivers/java/overview

```xml
<repositories>
    <repository>
        <id>repo.typedb.com</id>
        <url>https://repo.typedb.com/public/public-release/maven/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupid>com.typedb</groupid>
        <artifactid>typedb-driver</artifactid>
        <version>3.11.2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.11.2
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.11.2" />
</ItemGroup>
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.11.2
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.11.2


## New Features
- **Print TypeDB Studio link and Console command on server startup**
  
  Adds clickable/runnable connect hints to the server startup output:
  ```
    To connect with TypeDB Studio, open: https://studio.typedb.com/connect?address=http://localhost:8000&username=admin
    To connect with TypeDB Console, run: typedb console --address localhost:1729 --tls-disabled --username admin`
  ```
  
  Both substitute `localhost` when the listener binds to an unspecified address (0.0.0.0 / [::]). The Studio scheme and Console invocation follow the encryption setting; the Studio link is omitted if the HTTP endpoint is disabled.
  
  
- **Add default advertise-address retrieved from listen-address**
  
  Remove pre-inserted `advertise-address` values from server endpoints configuration. Instead, auto-generate the advertise addresses based on the listen addresses following these rules:
  * if the listen address is an unspecified address (`0.0.0.0`), substitute the IP with loopback `127.0.0.1`, preserving the original port -- this allows the clients to use `127.0.0.1` instead of `0.0.0.0` for connections, which is usually the intention;
  * otherwise, use the listen address for advertising as is.
  
  This reduces the amount of fields required for modifications in case the user wants to change the default server ports and helps avoid sudden misconfigurations while operating with local servers.
  
  Nothing changed for default configuration:
  
  ```
  ./typedb server
  
  =====================================================================================
       ________ __      __ _____    _______  _____    _____       _______  _______
      |__    __|\  \  /  /|   _  \ |   _   ||   _  \ |   _  \    |   _   ||   _   |
         |  |    \  \/  / |  | |  ||  | |__||  | |  ||  | |  |   |  | |  ||  | |__|
         |  |     \    /  |  |/  / |  |___  |  | |  ||  |/  /    |  | |__||  |___
         |  |      |  |   |   __/  |   ___| |  | |  ||   _  \    |  |  __ |   ___|
         |  |      |  |   |  |     |  |  __ |  | |  ||  | |  |   |  | |  ||  |  __
         |  |      |  |   |  |     |  |_|  ||  |/  / |  |/  /    |  |_|  ||  |_|  |
         |__|      |__|   |__|     |_______||_____/  |_____/     |_______||_______|
  
  =====================================================================================
  
  Running TypeDB CE 3.10.4 in development mode.
  Serving:
    gRPC:  0.0.0.0:1729 (connect via 127.0.0.1:1729)
    HTTP:  0.0.0.0:8000 (connect via 127.0.0.1:8000)
    Admin: 127.0.0.1:1728 (localhost only)
  TLS: disabled
    WARNING: TLS NOT ENABLED. Credentials are transmitted unencrypted in plaintext.
    Drivers must be configured to connect *without TLS*.
  ```
  
  
- **Improved query profiler**
  
  We implement a real nested query profiling structure, which correctly captures sub-pattern timings and executions and eliminates redundant printing.
  
  
- **Extend TypeDB address configuration and reorganize TypeDB services for better extensibility, incl. clusters**
  
  Introduce better extensibility to TypeDB CE, enabling TypeDB services to be extended and modified by distributions with additional functionality (e.g., clustered distributions for TypeDB Enterprise/Cloud).
  
  ### User-facing
  
  - **Change server configuration.** Rename `server.address` and `server.http.address` to `listen-address`es (`address` aliases are still accepted for backwards compatibility). Add `advertise-address` configuration for both gRPC and HTTP endpoints, distinguishing the address the server binds to from the address clients use to connect (address advertised to the clients). Supports NAT, reverse proxy, and load balancer deployments. Specifying both `listen-address` and `address` will be rejected by the system.
  - Add an admin CLI tool (`typedb admin`) bundled with all distributions. The admin tool communicates with the server through a localhost-only gRPC endpoint (default port 1728). Supports interactive REPL mode, one-shot (`--command`), and script (`--script`) execution modes. Commands: `server version`, `server status`, `help`, `exit`.
  - Add `server.admin` configuration section (default: enabled on port 1728).
  - Restructure server startup output to a multi-line format showing each endpoint (gRPC, HTTP, Admin) with TLS status:
  
  ```
  =====================================================================================
       ________ __      __ _____    _______  _____    _____       _______  _______
      |__    __|\  \  /  /|   _  \ |   _   ||   _  \ |   _  \    |   _   ||   _   |
         |  |    \  \/  / |  | |  ||  | |__||  | |  ||  | |  |   |  | |  ||  | |__|
         |  |     \    /  |  |/  / |  |___  |  | |  ||  |/  /    |  | |__||  |___
         |  |      |  |   |   __/  |   ___| |  | |  ||   _  \    |  |  __ |   ___|
         |  |      |  |   |  |     |  |  __ |  | |  ||  | |  |   |  | |  ||  |  __
         |  |      |  |   |  |     |  |_|  ||  |/  / |  |/  /    |  |_|  ||  |_|  |
         |__|      |__|   |__|     |_______||_____/  |_____/     |_______||_______|
  
  =====================================================================================
  
  Running TypeDB CE 3.8.3 in development mode.
  Serving:
    gRPC:  0.0.0.0:1729 (connect via 127.0.0.1:1729)
    HTTP:  0.0.0.0:8000 (connect via http://127.0.0.1:8000)
    Admin: 127.0.0.1:1728 (localhost only)
  TLS: disabled
    WARNING: TLS NOT ENABLED. Credentials are transmitted unencrypted in plaintext.
    Drivers must be configured to connect *without TLS*.
  ```
  
  ### Development
  
  - Expose config extensibility for distribution-specific settings. Cluster distributions need to parse additional config sections (e.g., server.clustering) from the same YAML file without modifying the base Config struct.
  - Add `server_version` and `server_status` RPCs to the admin protocol, returning distribution info and endpoint addresses.
  - Restructure the `server` package, introducing `Operator`s that serve as a middleware between network services and inner TypeDB components (`DatabaseManager`, `UserManager`, etc.). 
  - Introduce `ErrorResponseCategory` for mapping server state errors to appropriate gRPC and HTTP status codes and HTTP 307 redirect support (currently cannot be used in TypeDB for most of the endpoints due to local-only authentication -- instead, an additional `AuthenticatedRedirect` is introduced).
  - Add close signals for `TransactionService`s to extend the ability of the system to interrupt existing transactions without shutting down.
  
  

## Bugs Fixed
- **Compress the content of docker images**
  
  Reduce the size of the Docker image x2 (93 MB -> 53 MB prod, 361 MB -> 141 MB [snapshot](https://hub.docker.com/r/typedb/typedb-snapshot/tags)) by gzipping its layers explicitly in the Bazel configuration.
  
  This change returns the size of the Docker image to the values of the pre-Bazel 8 releases.
  
  
- **Fix connection-level GOAWAY errors under load**
  
  Drivers that sustain transaction churn over a single TypeDB driver connection (e.g. several worker threads opening/committing/closing many small write transactions in a row) eventually saw every in-flight request fail at the same moment with `h2 protocol error: too_many_internal_resets` / `broken pipe`. Server logs showed:
  
      WARN h2/proto/streams/streams.rs:1629:
          locally-reset streams reached limit (1024)
  
  followed by `GOAWAY ENHANCE_YOUR_CALM`. The server itself never panicked however,the gRPC connection was poisoned.
  
  
- **QueryCache tracks statistics & schema commit to avoid caching outdated plans**
  Updates the QueryCache to explicitly track validity requirements and validate insert calls. By tracking the sequence_number of the latest schema commit, we avoid #7787. By tracking statistics, we avoid #7790 
  
  
- **Rewrite reused producible variables in transform phase**
  Introduces a transformation step which rewrites constraints so the variables bound by it are unique.
  E.g.: `$r links (some-role: $r);` is rewritten to `$_i links (some-role: $r); $r is $_i;`
  
  

## Code Refactors
- **Eagerly free inputs of steps which are finished executing**
  Frees fully consumed `FixedBatchIterator`s from the `IntersectionExecutor` to save memory on long pipelines with few answers.
  
  
- **Refactor annotation phase to group annotation context & running variable annotations**
  We reduce the number of arguments in functions in the annotation phase, by grouping related arguments together in structures.
  
  

## Other Improvements
- **Update console artifact to use empty adverise address**
  
  Update console artifact to use an empty adverise address
  
  
- **Remove default advertised address**
  
  Return empty advertised server address (both grpc and http) in case it's not specified instead of using a default value.
  
  This change lets the clients to understand when the server does not enforce any address for connections and make independent decisions. It is especially important in cases of connecting to a docker container or a proxy, when the server used to return a not accessible address, which the client eagerly used and failed to connect regardless of the correctness of the connection address provided by the user.
  
  
- **Prepare release 3.11.1**
  Bump version & prepare release notes
  
  
- **Make studio link a constant**

- **Prepare release 3.11.0-rc1**
  Bump version & update release notes
  
  
- **Escalate concurrent key and unique writes to transaction failure#7800 (#7800)**

- **Disable all warnings on optional return mismatches**
  Disable all warnings on optional return mismatches, till the spec is finalised
  
  
  
    
