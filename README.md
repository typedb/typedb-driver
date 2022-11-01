# TypeDB Client for Java

[![Factory](https://factory.vaticle.com/api/status/vaticle/typedb-client-java/badge.svg)](https://factory.vaticle.com/vaticle/typedb-client-java)
[![Discord](https://img.shields.io/discord/665254494820368395?color=7389D8&label=chat&logo=discord&logoColor=ffffff)](https://vaticle.com/discord)
[![Discussion Forum](https://img.shields.io/discourse/https/forum.vaticle.com/topics.svg)](https://forum.vaticle.com)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typedb-796de3.svg)](https://stackoverflow.com/questions/tagged/typedb)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typeql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/typeql)

## Client Architecture
To learn about the mechanism that a TypeDB Client uses to set up communication with databases running on the TypeDB Server, refer to [TypeDB > Client API > Overview](http://docs.vaticle.com/docs/client-api/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Client Java, refer to [TypeDB > Client API > Java > API Reference](http://docs.vaticle.com/docs/client-api/java#api-reference).

## Concept API
To learn about the methods available on the concepts retrieved as the answers to TypeQL queries, refer to [TypeDB > Concept API > Overview](http://docs.vaticle.com/docs/concept-api/overview)

## Import TypeDB Client for Java through Maven

```xml
<repositories>
    <repository>
        <id>repo.vaticle.com</id>
        <url>https://repo.vaticle.com/repository/maven/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.vaticle.typedb</groupId>
        <artifactId>typedb-client</artifactId>
        <version>{version}</version>
    </dependency>
</dependencies>
```

Further documentation: https://docs.vaticle.com/docs/client-api/java

## Build TypeDB Client for Java from Source

> Note: You don't need to compile TypeDB Client from source if you just want to use it in your code. See the _"Import TypeDB Client for Java"_ section above.

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
   bazel-bin/com.vaticle.typedb:api.jar
   bazel-bin/pom.xml
   ```

## FAQs

**Q:** I see a large number of Netty and gRPC log messages. How can I disable them?

**A:** Create a Logback configuration file and set the minimum log level to ERROR. You can do so with the following steps:
1. Create a file in your `resources` path (`src/main/resources` by default in a Maven project) named `logback.xml`.
2. Copy the following document into `logback.xml`:
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="ERROR">
        <appender-ref ref="STDOUT"/>
    </root>

</configuration>
```
