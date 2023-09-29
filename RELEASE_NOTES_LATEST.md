#### Rust driver

Available from https://crates.io/crates/typedb-driver

```sh
cargo add typedb-driver@2.24.5
```

## Java driver

Documentation: http://docs.vaticle.com/docs/driver-api/java

### Distribution

Available through https://repo.vaticle.com

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
        <version>2.24.5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://docs.vaticle.com/docs/driver-api/python

Available through https://pypi.org

```
pip install typedb-driver==2.24.5
```

## NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://docs.vaticle.com/docs/driver-api/nodejs

### Installation

```
npm install typedb-driver@2.24.5
```

## Architectural Changes

**We have centralised all TypeDB Driver libraries into this repository**. This will make maintanance and development much simpler across the wide surface area exposed by the drivers.

## New Features

- **Rearchitect Rust Driver to support full TypeDB feature set**

- **Extend Rust Driver to support FFI**

- **Create SWIG rules to generate C compatibility layer & C Driver**
 
- **Create SWIG rules for Python and Java**

- **Reimplement Java Driver using JNI over Rust**

- **Reimplement Python Driver using FFI over Rust**

- **Update TypeDB NodeJS Driver to the latest feature set**


## Bugs Fixed

## Code Refactors

- **Create unified network API for Core and Enterprise**

- **Simplify Concept API by parametrizing methods with enum arguments**

- **Delete Remote Concept API**



## Other Improvements
