/*
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

package com.vaticle.typedb.driver.common;

import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.JNI_LIBRARY_NOT_FOUND;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.JNI_PLATFORM_LIBRARY_NOT_FOUND;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.UNRECOGNISED_ARCH;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.UNRECOGNISED_OS;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.UNRECOGNISED_OS_ARCH;

public class Loader {

    private static final String DRIVER_JNI_LIB_RESOURCE = "typedb_driver_jni";
    private static final String DRIVER_JNI_LIBRARY_NAME = System.mapLibraryName(DRIVER_JNI_LIB_RESOURCE);

    private static final Map<Pair<OS, Arch>, String> DRIVER_JNI_JAR_NAME = Map.of(new Pair<>(OS.WINDOWS, Arch.x86_64), "windows-x86_64",
            new Pair<>(OS.MAC, Arch.x86_64), "macosx-x86_64", new Pair<>(OS.MAC, Arch.ARM64), "macosx-arm64", new Pair<>(OS.LINUX, Arch.x86_64),
            "linux-x86_64", new Pair<>(OS.LINUX, Arch.ARM64), "linux-arm64");

    private static boolean loaded = false;

    public static void loadNativeLibraries() {
        if (!loaded) {
            try {
                Path libraryPath = unpackNativeLibrary();
                System.load(libraryPath.toAbsolutePath().toString());
                loaded = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Path unpackNativeLibrary() throws IOException {
        Pair<OS, Arch> platform = new Pair<>(OS.detect(), Arch.detect());
        if (!DRIVER_JNI_JAR_NAME.containsKey(platform)) {
            throw new TypeDBDriverException(UNRECOGNISED_OS_ARCH, platform.first(), platform.second());
        }
        String platformString = DRIVER_JNI_JAR_NAME.get(platform);
        ClassLoader loader = Loader.class.getClassLoader();
        Iterator<URL> resourceIterator = loader.getResources(DRIVER_JNI_LIBRARY_NAME).asIterator();

        if (!resourceIterator.hasNext()) {
            throw new TypeDBDriverException(JNI_LIBRARY_NOT_FOUND, DRIVER_JNI_LIBRARY_NAME);
        }
        URL jniURL = resourceIterator.next();
        if (resourceIterator.hasNext()) {
            if (!jniURL.getPath().contains(platformString)) {
                jniURL = null;
                while (resourceIterator.hasNext()) {
                    URL resource = resourceIterator.next();
                    if (resource.getPath().contains(platformString)) {
                        jniURL = resource;
                        break;
                    }
                }
            }
        }
        if (jniURL == null) {
            throw new TypeDBDriverException(JNI_PLATFORM_LIBRARY_NOT_FOUND, DRIVER_JNI_LIBRARY_NAME, platformString);
        }
        return unpackNativeResources(jniURL);
    }

    private static Path unpackNativeResources(URL resourceURL) throws IOException {
        Path tempPath = Files.createTempDirectory("typedb-driver-lib");
        tempPath.toFile().deleteOnExit();
        Path newPath = tempPath.resolve(DRIVER_JNI_LIBRARY_NAME);
        Files.copy(resourceURL.openStream(), newPath);
        newPath.toFile().deleteOnExit();
        return newPath;
    }

    private enum OS {
        WINDOWS, MAC, LINUX;

        static OS detect() {
            String name = System.getProperty("os.name").toLowerCase();
            if (name.contains("win")) {
                return WINDOWS;
            } else if (name.contains("mac")) {
                return MAC;
            } else if (name.contains("linux")) {
                return LINUX;
            } else {
                throw new TypeDBDriverException(UNRECOGNISED_OS, name);
            }
        }
    }

    private enum Arch {
        ARM64, x86_64;

        static Arch detect() {
            String arch = System.getProperty("os.arch").toLowerCase();
            if (arch.equals("amd64") || arch.equals("x86_64") || arch.contains("x64")) {
                return x86_64;
            } else if (arch.equals("aarch64") || arch.contains("arm64")) {
                return ARM64;
            } else {
                throw new TypeDBDriverException(UNRECOGNISED_ARCH, arch);
            }
        }
    }
}
