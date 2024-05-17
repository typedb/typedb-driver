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

REM needs to be called such that software installed
REM by Chocolatey in prepare.bat is accessible
choco install 7zip.portable --limit-output --yes --no-progress
choco install cmake.install --version 3.27.0 --installargs '"ADD_CMAKE_TO_PATH=User"' --limit-output --yes --no-progress
CALL refreshenv

bazel --output_user_root=C:\b build @vaticle_typedb_artifact_windows-x86_64//file
powershell -Command "Move-Item -Path bazel-typedb-driver\external\vaticle_typedb_artifact_windows-x86_64\file\typedb-server-windows* -Destination typedb-server-windows.zip"
7z x typedb-server-windows.zip
powershell -Command "Move-Item -Path typedb-server-windows-* -Destination typedb-server-windows"

bazel --output_user_root=C:\b build //cpp:assemble-windows-x86_64-zip
mkdir test_assembly_cpp
pushd test_assembly_cpp
7z x ..\bazel-bin\cpp\typedb-driver-cpp-windows-x86_64.zip
cmake ..\cpp\test\assembly -DTYPEDB_ASSEMBLY=%cd%\typedb-driver-cpp-windows-x86_64 
cmake --build . --config release
popd
set PATH=%cd%\test_assembly_cpp\typedb-driver-cpp-windows-x86_64\lib;%PATH%;

START /B "" typedb-server-windows\typedb server --diagnostics.reporting.statistics=false --diagnostics.reporting.errors=false
powershell -Command "Start-Sleep -Seconds 10"

test_assembly_cpp\Release\test_assembly.exe
SET IS_ERROR=%ERRORLEVEL%

REM kill typedb server
wmic process where "commandline like '%%typedb%%'" delete

EXIT %IS_ERROR%
