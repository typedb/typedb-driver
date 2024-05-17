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

choco install 7zip.portable --limit-output --yes --no-progress
CALL refreshenv

git rev-parse HEAD > version_temp.txt
set /p VER=<version_temp.txt

bazel --output_user_root=C:\b build @vaticle_typedb_artifact_windows-x86_64//file
powershell -Command "Move-Item -Path bazel-typedb-driver\external\vaticle_typedb_artifact_windows-x86_64\file\typedb-server-windows* -Destination typedb-server-windows.zip"
7z x typedb-server-windows.zip
RD /S /Q typedb-server-windows
powershell -Command "Move-Item -Path typedb-server-windows-* -Destination typedb-server-windows"
START /B "" typedb-server-windows\typedb server --diagnostics.reporting.statistics=false --diagnostics.reporting.errors=false

python3 -m pip install --extra-index-url https://repo.typedb.com/public/public-snapshot/python/simple typedb-driver==0.0.0+%VER%
cd python/tests/deployment
python3 -m unittest test
SET IS_ERROR=%ERRORLEVEL%

REM kill typedb server
wmic process where "commandline like '%%typedb%%'" delete

EXIT /B %IS_ERROR%
