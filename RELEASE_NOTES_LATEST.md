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
        <version>{version}</version>
    </dependency>
</dependencies>
```

## NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://docs.vaticle.com/docs/driver-api/nodejs

### Installation

```
npm install typedb-driver@{version}
```

## New Features

- **Rearchitect Rust Driver to support full TypeDB feature set**

- **Extend Rust Driver to support FFI**

- **Create SWIG rules to generate C compatibility layer & C Driver**
 
- **Create SWIG rules for Python and Java**

- **Reimplement Java Driver using JNI over Rust**

- **Reimplement Python Driver using FFI over Rust**

- **Reimplement Python Driver using FFI over Rust**


## Bugs Fixed

## Code Refactors

- **Create unified network API for Core and Enterprise**

- **Simplify Concept API by parametrizing methods with enum arguments**

- **Delete Remote Concept API**



## Other Improvements

- **Move all drivers into sub-packages in this centralised repository**

