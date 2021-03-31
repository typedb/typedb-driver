# Grakn Client for Java

[![Grabl](https://grabl.io/api/status/graknlabs/client-java/badge.svg)](https://grabl.io/graknlabs/client-java)
[![Discord](https://img.shields.io/discord/665254494820368395?color=7389D8&label=chat&logo=discord&logoColor=ffffff)](https://grakn.ai/discord)
[![Discussion Forum](https://img.shields.io/discourse/https/discuss.grakn.ai/topics.svg)](https://discuss.grakn.ai)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-grakn-796de3.svg)](https://stackoverflow.com/questions/tagged/grakn)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-graql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/graql)

## Client Architecture
To learn about the mechanism that a Grakn Client uses to set up communication with databases running on the Grakn Server, refer to [Grakn > Client API > Overview](http://dev.grakn.ai/docs/client-api/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Client Java, refer to [Grakn > Client API > Java > API Reference](http://dev.grakn.ai/docs/client-api/java#api-reference).

## Concept API
To learn about the methods available on the concepts retrieved as the answers to Graql queries, refer to [Grakn > Concept API > Overview](http://dev.grakn.ai/docs/concept-api/overview)

## Import Grakn Client for Java through Maven

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
        <artifactId>grakn-client</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

Further documentation: https://dev.grakn.ai/docs/client-api/java

## Build Grakn Client for Java from Source

> Note: You don't need to compile Grakn Client from source if you just want to use it in your code. See the _"Import Grakn Client for Java"_ section above.

1. Make sure you have the following dependencies installed on your machine:
    - Java JDK 11 or higher
    - [Bazel](https://docs.bazel.build/versions/master/install.html)

2. Build the JAR:

   a) to build the native/raw JAR:
   ```
   bazel build //:client-java
   ```
   The Java library JAR will be produced at: `bazel-bin/libclient-java.jar`

   b) to build the JAR for a Maven application:
   ```
   bazel build //:assemble-maven
   ```
   The Maven JAR and POM will be produced at: 
   ```
   bazel-bin/io.grakn.client:api.jar
   bazel-bin/pom.xml
   ```
