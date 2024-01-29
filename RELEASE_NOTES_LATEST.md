Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@2.26.6-rc0
```


### Java driver

Available through https://repo.typedb.com
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
        <groupid>com.vaticle.typedb</groupid>
        <artifactid>typedb-driver</artifactid>
        <version>2.26.6-rc0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==2.26.6rc0
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@2.26.6-rc0
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:2.26.6-rc0

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:2.26.6-rc0
Documentation: https://typedb.com/docs/drivers/cpp/overview


## New Features
- **Update to allow Unicode TypeQL variable names**
  
  We update TypeQL and tests to ensure that support for Unicode TypeQL variable names (https://github.com/vaticle/typeql/pull/310) is included.
  
  

## Bugs Fixed


## Code Refactors
- **Transition from standalone typedb-common to typeql/common**
  
  We update Bazel dependencies and target paths following the merging of typedb-common into [vaticle/typeql](https://github.com/vaticle/typeql/) (see https://github.com/vaticle/typeql/pull/313).
  
- **Refactor C++ docs to reflect other docs**
  Refactors generated C++ documentation so the Iterator and Future classes are documented in the answer folder.
  
  
- **Move deployment jobs from Factory CI to CIrcleCI**
  
  We move all snapshot deployment jobs from Factory CI to CIrcleCI to ensure that assembly and deployment succeed in the same environment release deployment is performed in. We also consolidate platform independent jobs into `deploy-release/snapshot-any` to further reduce the amount of CI jobs.
  
- **Update driver Java tests to retrieve TypeDB runners as maven library**
  
  We update Java tests to use the typedb-runner and typedb-cloud-runner libraries, which are now available from Maven instead of via the Git dependency on typedb-common.
  
  

## Other Improvements
- **Only deploy releases to cloudsmith**
  
  We implement the following changes to the release process:
  
  - stop uploading build artifacts to the github releases page;
  - update the release notes documentation links;
  - add C and C++ artifact download link templates to the release notes template.
  
- **Sync dependencies in CI**
  
  We add a sync-dependencies job to be run in CI after successful snapshot and release deployments. The job sends a request to vaticle-bot to update all downstream dependencies.
  
  The snapshot dependencies sync job is run in Factory CI after all behaviour and integration tests pass. The CircleCI tests only verify deployment, which is not extensive enough verification to trigger downstream propagation, and not strictly necessary for sync as that is only affected by git dependencies.
  
  The release dependencies sync job, in contrast, runs in CircleCI and only runs after all deployments have succeeded and been verified, so that all downstream deployments can be safely updated.
  
  Importantly, due to the way sync-dependencies is implemented, we revert `dependencies/vaticle/repositories.bzl` from having a dedicated typedb-protocol version line for the purposes of workspace-status, back to inlined version with the sync-marker. This means that the sync-marker is performing double duty as a workspace status marker.
  
  Note: this PR does _not_ update the `dependencies` repo dependency. It will be updated automatically by the bot during its first pass.
  
- **Refactor npm installation and job limitation to prevent CircleCI OOM**

- **Fix npm installation and Java bootup configurations**

- **Disable diagnostics reporting in CI**

- **Increase CircleCI executor size for OOM**

- **Fix NPM deploy jobs**

- **Migrate artifact hosting to cloudsmith**
  
  Updates artifact deployment & consumption rules to use cloudsmith (repo.typedb.com) instead of the self-hosted sonatype repository (repo.vaticle.com).
  
  
- **Move doc generation tools to bazel distribution repository**
  Moves bazel rules used for generating documentation into the vaticle/bazel-distribution repository
  
  
- **Move dependencies/maven/artifacts.bzl:vaticle_artifacts to dependencies/vaticle/artifacts.bzl:maven_artifacts**

- **Move remotejdk bazelrc option into build/run/test to avoid affecting bazel queries**

- **Apply release pipeline fixes**

    
