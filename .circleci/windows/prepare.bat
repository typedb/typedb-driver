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

REM shorten the workspace name so that we can avoid the long path restriction
git apply .circleci\windows\git.patch
IF %ERRORLEVEL% NEQ 0 EXIT /B %ERRORLEVEL%

REM uninstall Java 12 installed by CircleCI
choco uninstall openjdk --limit-output --yes --no-progress

REM install dependencies needed for build
choco install .circleci\windows\dependencies.config  --limit-output --yes --no-progress

REM create a symlink python3.exe and make it available in %PATH%
mklink C:\Python312\python3.exe C:\Python312\python.exe
set PATH=%PATH%;C:\Python312

REM install runtime dependency for the build
C:\Python311\python.exe -m pip install wheel

REM permanently set variables for Bazel build
SETX BAZEL_SH "C:\Program Files\Git\usr\bin\bash.exe"
SETX BAZEL_PYTHON C:\Python312\python.exe
SETX BAZEL_VC "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC"
