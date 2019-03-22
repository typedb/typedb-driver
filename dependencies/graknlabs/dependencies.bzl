#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def graknlabs_grakn_core():
    # TODO: update to graknlabs/grakn before merging the PR
    git_repository(
        name = "graknlabs_grakn_core",
        remote = "https://github.com/lolski/grakn",
        commit = "923ce85db4fd4852253763456ce87bf0296ca0f4" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_grakn_core
    )

def graknlabs_build_tools():
    # TODO: update to graknlabs/graql before merging the PR
    git_repository(
        name = "graknlabs_build_tools",
        remote = "https://github.com/lolski/build-tools",
        commit = "b50de5fde86b8ba177f64dd401b7b7a6f8eb6cc8", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_build_tools
    )
