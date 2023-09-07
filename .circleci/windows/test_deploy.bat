REM
REM Copyright (C) 2021 Vaticle
REM
REM This program is free software: you can redistribute it and/or modify
REM it under the terms of the GNU Affero General Public License as
REM published by the Free Software Foundation, either version 3 of the
REM License, or (at your option) any later version.
REM
REM This program is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM GNU Affero General Public License for more details.
REM
REM You should have received a copy of the GNU Affero General Public License
REM along with this program.  If not, see <https://www.gnu.org/licenses/>.
REM

REM needs to be called such that software installed
REM by Chocolatey in prepare.bat is accessible
CALL refreshenv

ECHO Testing deployed pip package...
SET DEPLOY_PIP_USERNAME=%REPO_VATICLE_USERNAME%
SET DEPLOY_PIP_PASSWORD=%REPO_VATICLE_PASSWORD%
SET RUST_BACKTRACE=1
git rev-parse HEAD > version_temp.txt
set /p VER=<version_temp.txt
bazel --output_user_root=C:/tmp run --verbose_failures //python/tests:typedb-extractor -- typedb-all
call START /B typedb-all/typedb server
python3 -m pip install --extra-index-url https://repo.vaticle.com/repository/pypi-snapshot/simple typedb-client==0.0.0+%VER%
cd python/tests/deployment
python3 -m unittest test
taskkill "typedb.exe"
IF %errorlevel% NEQ 0 EXIT /b %errorlevel%