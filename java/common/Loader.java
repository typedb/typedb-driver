/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.vaticle.typedb.client.common;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

public class Loader {
    private static boolean loaded = false;

    public static void loadNativeLibraries() {
        if (!loaded) {
            try {
                Path tempPath = getNativeResourceURI();
                System.load(tempPath.resolve(System.mapLibraryName("typedb_client_jni")).toString());
                loaded = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Path getNativeResourceURI() throws IOException {
        ClassLoader loader = Loader.class.getClassLoader();
        String resource = System.mapLibraryName("typedb_client_jni");
        URL resourceURL = loader.getResource(resource);
        Objects.requireNonNull(resourceURL, String.format("Resource %s was not found in ClassLoader %s", resource, loader));

        try {
            return unpackNativeResources(resourceURL.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private static Path unpackNativeResources(URI resourceURI) throws IOException {
        Path tempPath = Files.createTempDirectory("typedb-driver");
        tempPath.toFile().deleteOnExit();

        FileSystem fs = FileSystems.newFileSystem(resourceURI, Collections.emptyMap());
        Path p = fs.provider().getPath(resourceURI);
        Path newPath = tempPath.resolve(p.getParent().relativize(p).toString());
        Files.copy(p, newPath);
        newPath.toFile().deleteOnExit();

        return tempPath;
    }
}
