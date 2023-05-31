NPM package: https://www.npmjs.com/package/typedb-client
Documentation: https://docs.vaticle.com/docs/client-api/nodejs

## Installation

```
npm install typedb-client@{version}
```


## New Features
- **Introduce Values to support expressions**
  
  Introduce the 'Value' type, which is returned as the result of an expression's computation. This change follows from https://github.com/vaticle/typeql/pull/260, which outlines the capabilities of the new expression syntax.
  
  Values (representing any of Long, Double, Boolean, String, or DateTime) are returned as part of `ConceptMap` answers and are subtypes of `Concept` for the time being. Their main API is made of the `.getValue()` method and `.getValueType()` method, along with all the standard safe downcasting methods to convert a `Concept` into a `Value`, using `Concept.isValue()` and `Concept.asValue()`.
  
  We also move the import location of `ValueType` from being nested in `AttributeType` to `Concept`, and we remove the 
  
  
- **Support generalised annotations and uniqueness**
  
  We update the Typedb Protocol and TypeQL to the latest versions, which support the uniqueness annotation and generalised `Type` APIs. These generalised APIs allow querying by a set of annotations, rather than just boolean = `true|false`.
  
  For example, the API: `AttributeType.getOwners(onlyKey: boolean)` has  become: `AttributeType.getOwners(annotations: Annotation[])`
  
  All usages of boolean flags to indicate key-ness should be replaced by using arrays of `ThingType.Annotation.KEY`. The new `@unique` annotation is available as `ThingType.Annotation.KEY`, and is usable within all the APIs that accept array of annotations.
  
  
  
- **Add method to Cluster client to retrieve current user**
  
  Add an API to be able retrieve the currently authenticated user.
  
  

## Bugs Fixed


## Code Refactors
- **Protocol versioning**
  
  We use a new protocol API to perform a "connection open". This API does server-side protocol version compatibility checks, and replaces our previous need to get all databases to check that the connection is available.
  
  The `TypeDB.coreClient()` entry point now must be `await`ed, the same as the `TypeDB.clusterClient` entry point.
  
  

## Other Improvements
- **Update release notes workflow**
  
  We integrate the new release notes tooling. The release notes are now to be written by a person and committed to the repo.
  
  
- **Make unimplemented error message more friendly**

    

