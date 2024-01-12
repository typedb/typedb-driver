Documentation: https://typedb.com/docs/clients/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver


```sh
cargo add typedb-driver@2.26.5
```


### Java driver

Available through https://repo.typedb.com
Documentation: https://typedb.com/docs/clients/java-driver

```xml
<repositories>
    <repository>
        <id>repo.typedb.com</id>
        <url>https://repo.typedb.com/public/public/maven/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupid>com.vaticle.typedb</groupid>
        <artifactid>typedb-driver</artifactid>
        <version>2.26.5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver

Available through https://pypi.org

```
pip install typedb-driver==2.26.5
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver

```
npm install typedb-driver@
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://github.com/vaticle/typedb-driver/releases/tag/2.26.5

### C driver

Compiled distributions comprising headers and shared libraries available at: https://github.com/vaticle/typedb-driver/releases/tag/2.26.5



## New Features
- **Check server URL contains port**
  
  Fail with a sensible error message when attempting to connect to a TypeDB instance using an address without explicit port.
  
  

## Bugs Fixed


## Code Refactors
- **Increase size of Windows executor in CircleCI to XL**
  
  We increase the size of the Windows executor in CircleCI deployment job from medium to xlarge. This change necessitated upgrade from windows orb v2.0.0 to v5.0, and reduced CI time from ~40 minutes to ~20 minutes.
  
  
- **Release from Amazon Linux to support GLIBC 2.26**
  
  We migrate release jobs in CircleCI from Ubuntu-18.04 to Amazon Linux 2 docker image (RedHat-based), in order to downgrade the GLIBC dependency from 2.27 to 2.26. This approach will enable many users who use Amazon Linux 2 to be sure that they can use TypeDB drivers to connect to TypeDB.
  
  
  
- **Merge CircleCI jobs per platform**
  
  We reduce the number of CircleCI jobs by combining all jobs running on the same executor (ie. per platform) into one. This reduces overhead associated with spinning up a new executor for each job and streamlines the release process.
  
  CircleCI now loosely has one deploy job platform, and one test job per platform in both snapshot and release pipelines.
  
  Jobs that do not require native compilation, are unchanged.
  
  

## Other Improvements

- **Merge 2.26.3 release changes to development**
  
  We merge changes made to CI pipelines for the 2.26.3 release back to development.
  
    
