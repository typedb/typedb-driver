Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.4.4
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.4.4/a=noarch;xg=com.typedb/)
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
        <version>3.4.4</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.4.4
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/typedb-driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install typedb-driver-http@3.4.4
```

## New Features

- **Typescript HTTP driver**

  Add a relatively slim typescript driver based on our HTTP API, adapted from code used for this purpose in `typedb-studio`.

  This driver is published to `npm` under the name `typedb-driver-http` through the same process as the old `nodejs` driver.


- **Typescript HTTP driver docs generation**
  
  Introduce docs generation for the Typescript HTTP driver, based on the existing strategy for the NodeJS driver.
  
  We divide the docs into:
  
  - Connection
  - Response
  - Concept
  - Query Structure
  
  within each section, static functions are grouped into a specific `Static Functions` section.
  
  Additionally, `QueryConstraints`, `QueryVertices` in query structure; and `DriverParams` in connection are used as sub-categories (that is, `DriverParams`, `DriverParamsBasic`, and `DriverParamsTranslated` are all grouped under `DriverParams`).
  
  We also implement functionality for handling:
  - type aliases
  - indexable properties (e.g. `[varName: string]: Concept`)
  
  

## Bugs Fixed


## Code Refactors


## Other Improvements
- **Enforce explicit https addresses for TLS connections**
  Drivers return explicit error messages when connection addresses and TLS options are mismatched. TLS connections require addresses to have `https`. Non-TLS connections require addresses not to have `https`.  
  
  
- **Enable TLS by default in Python**
  
  We want to enable a secure-by-default setting in TypeDB Drivers. In Java and Rust, `DriverOptions` have to be explicitly set, and there are no defaults. However, Python features a disabled TLS default. While this is compatible with TypeDB CE, it's an insecure default & not compatible with TypeDB Cloud without explicitly enabling it.
  
  Instead, we set the default to TLS being __enabled__ in Python. This means when using an insecure, plaintext connection the user must explicitly set it, and is more likely to become aware of the plaintext communication.
  
  
- **Ensure PNPM config in CI matches config locally**
  
  We add an `.npmrc` file to ensure the PNPM config in local machines and CI match so that installation succeeds in CI.
  
  
- **Install PNPM deps in CI release pipeline**
  
  The release pipeline in CI now correctly installs PNPM dependencies for the HTTP TS driver.
  
  
- **Make the HTTP TypeScript driver dual-module (CJS+ESM)**
  
  The HTTP TypeScript driver is now dual-module, offering both CommonJS and ES Module support.
  
  
- **Set HTTP Typescript driver dependencies as dev dependencies**
  
  We set all dependencies of the Typescript HTTP driver as dev dependencies, as they aren't required at runtime.
  
  
- **Rename docs antora module**
  
  We rename the docs antora module used to host the generated driver references from `api-ref` to `external-typedb-driver`, to support refactoring in the typedb docs repository.
  
- **Revert the HTTP driver to a CommonJS package**
  
  We convert `typedb-driver-http` back into using `commonjs`. This allows us to also revert `import`/`export` syntax to not require file extensions.
  
  
- **Correct the package and tsconfig for HTTP driver**
  
  We fix issues in `package.json` that prevented `typedb-driver-http` from being used correctly
  
  
- **Fix CircleCI release configuration for HTTP driver**
  
  We fix a typo that made the CircleCI release configuration invalid.

  
- **Use release version of typedb server artifact**

- **Update to latest typedb server artifact**

    
