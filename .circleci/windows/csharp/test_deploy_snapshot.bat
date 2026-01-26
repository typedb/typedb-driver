@echo off
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

choco install dotnet-6.0-runtime --limit-output --yes --no-progress
choco install 7zip.portable --limit-output --yes --no-progress
CALL refreshenv

bazel --output_user_root=C:\b build @typedb_artifact_windows-x86_64//file
powershell -Command "Move-Item -Path bazel-typedb-driver\external\typedb_artifact_windows-x86_64\file\typedb-all-windows* -Destination typedb-all-windows.zip"
7z x typedb-all-windows.zip
RD /S /Q typedb-all-windows
powershell -Command "Move-Item -Path typedb-all-windows-* -Destination typedb-all-windows"
START /B "" typedb-all-windows\typedb server --development-mode.enabled=true --server.http.enabled=false

powershell -Command "(gc csharp\Test\Deployment\NugetApplicationTest.csproj) -replace 'DRIVER_CSHARP_VERSION_MARKER', '0.0.0-%CIRCLE_SHA1%' | Out-File -encoding ASCII csharp\Test\Deployment\NugetApplicationTest.csproj"
type csharp\Test\Deployment\NugetApplicationTest.csproj
cd csharp\Test\Deployment
CALL dotnet run NugetApplicationTest.csproj
SET IS_ERROR=%ERRORLEVEL%

REM kill typedb server
wmic process where "commandline like '%%typedb%%'" delete

EXIT %IS_ERROR%
