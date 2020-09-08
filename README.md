<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Grakn Client for Java

[![CircleCI](https://circleci.com/gh/graknlabs/client-java/tree/master.svg?style=shield)](https://circleci.com/gh/graknlabs/client-java/tree/master)
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
        <artifactId>api</artifactId>
        <version>1.5.0</version>
    </dependency>
</dependencies>
```

Further documentation: https://dev.grakn.ai/docs/client-api/java

## Build Grakn Client for Java from Source

> Note: You don't need to compile Grakn Client from source if you just want to use it in your code. See the _"Import Grakn Client for Java"_ section above.

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
   bazel build //:assemble-maven
   ```
   The Maven JAR and POM will be produced at: 
   ```
   bazel-bin/io.grakn.client:api.jar
   bazel-bin/pom.xml
   ```
