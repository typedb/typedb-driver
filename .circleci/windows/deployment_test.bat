@echo off
REM Copyright (C) 2022 Vaticle
REM
REM Licensed to the Apache Software Foundation (ASF) under one
REM or more contributor license agreements.  See the NOTICE file
REM distributed with this work for additional information
REM regarding copyright ownership.  The ASF licenses this file
REM to you under the Apache License, Version 2.0 (the
REM "License"); you may not use this file except in compliance
REM with the License.  You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.
REM

choco install maven --limit-output --yes --no-progress
choco install 7zip.portable --limit-output --yes --no-progress
CALL refreshenv

bazel --output_user_root=C:\bazel build @vaticle_typedb_artifact_windows//file
powershell -Command "Move-Item -Path bazel-typedb-driver-java\external\vaticle_typedb_artifact_windows\file\typedb-server-windows* -Destination typedb-server-windows.zip"
7z x typedb-server-windows.zip
powershell -Command "Move-Item -Path typedb-server-windows-* -Destination typedb-server-windows"
START /B "" typedb-server-windows\typedb server

powershell -Command "(gc java\test\deployment\pom.xml) -replace 'CLIENT_JAVA_VERSION_MARKER', '%CIRCLE_SHA1%' | Out-File -encoding ASCII java\test\deployment\pom.xml"
type java\test\deployment\pom.xml
cd java\test\deployment
CALL mvn test
SET IS_ERROR=%ERRORLEVEL%

REM kill typedb server
wmic process where "commandline like '%%typedb%%'" delete

EXIT %IS_ERROR%
