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

REM needs to be called such that software installed
REM by Chocolatey in prepare.bat is accessible
CALL refreshenv

ECHO Building and deploying windows package...
SET DEPLOY_PIP_USERNAME=%REPO_PYPI_USERNAME%
SET DEPLOY_PIP_PASSWORD=%REPO_PYPI_PASSWORD%
python.exe -m pip install twine
SET /p VER=<VERSION

bazel --output_user_root=C:/tmp run --verbose_failures --define version=%VER% //python:deploy-pip39 -- release
IF %errorlevel% NEQ 0 EXIT /b %errorlevel%

bazel --output_user_root=C:/tmp run --verbose_failures --define version=%VER% //python:deploy-pip310 -- release
IF %errorlevel% NEQ 0 EXIT /b %errorlevel%

bazel --output_user_root=C:/tmp run --verbose_failures --define version=%VER% //python:deploy-pip311 -- release
IF %errorlevel% NEQ 0 EXIT /b %errorlevel%

MD dist
COPY bazel-bin\python\typedb-driver39.whl dist\typedb_driver-py39-none-win_amd64.whl
COPY bazel-bin\python\typedb-driver310.whl dist\typedb_driver-py310-none-win_amd64.whl
COPY bazel-bin\python\typedb-driver311.whl dist\typedb_driver-py311-none-win_amd64.whl
