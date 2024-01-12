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
SET DEPLOY_MAVEN_USERNAME=%REPO_TYPEDB_USERNAME%
SET DEPLOY_MAVEN_PASSWORD=%REPO_TYPEDB_PASSWORD%

SET /p VER=<VERSION
bazel --output_user_root=C:/bazel run --verbose_failures --define version=%VER% //java:deploy-maven-jni --compilation_mode=opt -- release
IF %errorlevel% NEQ 0 EXIT /b %errorlevel%

MD dist
COPY bazel-bin\java\com.vaticle.typedb-typedb-driver-jni-windows-x86_64.jar dist\typedb-driver-jni-windows-x86_64.jar
