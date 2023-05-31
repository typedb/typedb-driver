Documentation: http://docs.vaticle.com/docs/client-api/java

## Distribution

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
        <artifactid>typedb-client</artifactid>
        <version>{version}</version>
    </dependency>
</dependencies>
```


## New Features
- **Introduce protocol versioning**
  
  We use a new protocol API to perform a "connection open". This API does server-side protocol version compatibility checks, and also replaces our previous need to do a generic RPC call to check that the network is available.
  
  The user will receive an error about a protocol version mismatch if they are using a client-server combination that are not using exactly compatible protocols. 
  1. The server will raise an error if the client tries to connect with a mismatching protocol version
  2. The client will raise an error if it tries to connect to the server and the server does not have that API
  
  Both errors imply a client-server mismatch and the error will suggest this as a fix.
  
  This change depends on https://github.com/vaticle/typedb-protocol/pull/185, which means that this client now implements protocol version 1.
  
  
- **Introduce Values to support expressions**
  
  Introduce the 'Value' type, which is returned as the result of an expression's computation. This change follows from https://github.com/vaticle/typeql/pull/260, which outlines the capabilities of the new expression syntax.
  
  Values (representing any of Long, Double, Boolean, String, or DateTime) are returned as part of `ConceptMap` answers and are subtypes of `Concept` for the time being. Their main API is made of the `.getValue()` method and `.getValueType()` method, along with all the standard safe downcasting methods to convert a `Concept` into a `Value`, using `Concept.isValue()` and `Concept.asValue()`.
  
  We also move the import location of `ValueType` from being nested in `AttributeType` to `Concept` and remove some APIs such as `isKeyable` from them.
  
  
- **Check BDD value equality using native types**
  
  In BDD steps implementation we checked the equality of all values through the conversion to `String`s. It might be incorrect when comparing `Double`s especially if one of these values is a result of arithmetics. 
  Now we compare `Double`s by their absolute error. 
  For consistency we compare all values using their native types equality.
  
  
- **Support generalised annotations and uniqueness**
  
  We update the Typedb Protocol and TypeQL to the latest versions, which support the uniqueness annotation and generalised `Type` APIs. These generalised APIs allow querying by a set of annotations, rather than just boolean = `true|false`.
  
  For example:
  `AttributeType.getOwners(boolean onlyKey)` becomes `AttributeType.getOwners(Set<TypeQLToken.Annotation> annotations)`
  
  all usages of boolean flags to indicate key-ness should be replaced by using `TypeQLToken.Annotation.KEY`. The new `@unique` annotation is available as the token `TypeQLToken.Annotation.UNIQUE`.
  
  
  
- **Add behaviour test step definition for retrieving the currently connected user**
  
  We've added a behaviour test step `get connected user` in line with changes made in https://github.com/vaticle/typedb-behaviour/pull/247
  
  

## Bugs Fixed


## Code Refactors


## Other Improvements
- **Update release notes workflow**
  
  We integrate the new release notes tooling. The release notes are now to be written by a person and committed to the repo.
  
  
- **Update BDD steps to reflect new annotation clause commas**

- **Add build cache to build-dependency job**

- **Add remote cache setup to ci tools**

- **Use bazel cache for snapshot deployment jobs**

- **Increase JSON serialisation test size**

- **Enable the bazel cache**

    

