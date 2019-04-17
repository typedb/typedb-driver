# Grakn Client for Java

[![CircleCI](https://circleci.com/gh/graknlabs/client-java/tree/master.svg?style=shield)](https://circleci.com/gh/graknlabs/client-java/tree/master)
[![Slack Status](http://grakn-slackin.herokuapp.com/badge.svg)](https://grakn.ai/slack)
[![Discussion Forum](https://img.shields.io/discourse/https/discuss.grakn.ai/topics.svg)](https://discuss.grakn.ai)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-grakn-796de3.svg)](https://stackoverflow.com/questions/tagged/grakn)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-graql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/graql)

## Client Architecture
To learn about the mechanism that a Grakn Client uses to set up communication with keyspaces running on the Grakn Server, refer to [Grakn > Client API > Overview](http://dev.grakn.ai/docs/client-api/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Client Java, refer to [Grakn > Client API > Java > API Reference](http://dev.grakn.ai/docs/client-api/java#api-reference).

## Concept API
To learn about the methods available on the concepts retrieved as the answers to Graql queries, refer to [Grakn > Concept API > Overview](http://dev.grakn.ai/docs/concept-api/overview)

## Importing Grakn Client for Java through Maven

```xml
<repositories>
    <repository>
        <id>repo.grakn.ai</id>
        <url>https://repo.grakn.ai/repository/maven/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.grakn.client</groupId>
        <artifactId>api</artifactId>
        <version>1.5.0</version>
    </dependency>
</dependencies>
```

Further documentation: https://dev.grakn.ai/docs/client-api/java

## Building Grakn Client for Java from Source

> Note: You don't need to compile Graql from source if you just want to use Graql. See the _"Importing Graql"_ section above.

1. Make sure you have the following dependencies installed on your machine:
    - Java 8
    - [Bazel](https://docs.bazel.build/versions/master/install.html)

2. Build the JAR:

   a) to build the native/raw JAR:
   ```
   bazel build //:client-java
   ```
   The Java library JAR will be produced at: `bazel-bin/libclient-java.jar`

   b) to build the JAR for a Maven application:
   ```
   bazel build //:assembl-maven
   ```
   The Maven JAR will be produced at: 
   ```
   bazel-bin/io.grakn.client:api.jar
   bazel-bin/pom.xml
   ```
