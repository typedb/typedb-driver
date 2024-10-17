# TypeDB Java Driver

## Driver Architecture
To learn about the mechanism that TypeDB drivers use set up communication with databases running on the TypeDB Server, refer to the [Drivers Overview](https://typedb.com/docs/drivers/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Java, refer to the [API Reference](https://typedb.com/docs/drivers/java/api-reference).

## Import TypeDB Driver for Java through Maven

```xml
<repositories>
    <repository>
        <id>repo.typedb.com</id>
        <url>https://repo.typedb.com/public/public-release/maven/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.typedb</groupId>
        <artifactId>typedb-driver</artifactId>
        <version>{version}</version>
    </dependency>
</dependencies>
```

Further documentation: https://typedb.com/docs/drivers/java/overview

## Build TypeDB Driver for Java from Source

> Note: You don't need to compile TypeDB Driver from source if you just want to use it in your code. See the _"Import TypeDB Driver for Java"_ section above.

1. Make sure you have the following dependencies installed on your machine:
    - Java JDK 11 or higher
    - [Bazel](https://docs.bazel.build/versions/master/install.html)

2. Build the JAR:

   a) to build the native/raw JAR:
   ```
   bazel build //java:driver-java
   ```
   The Java library JAR will be produced at: `bazel-bin/java/libdriver-java.jar`

   b) to build the JAR for a Maven application:
   ```
   bazel build //java/:assemble-maven
   ```
   The Maven JAR and POM will be produced at: 
   ```
   bazel-bin/java/com.typedb:api.jar
   bazel-bin/java/pom.xml
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
