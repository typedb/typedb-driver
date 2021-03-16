#!/usr/bin/env bash

set -x
bazel test $(bazel query "filter('^.*(?<!cluster)$', kind(.*_test, $1))") "${@:2}"
