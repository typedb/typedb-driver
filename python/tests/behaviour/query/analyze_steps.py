# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
import sys

from behave import *
from hamcrest import *

from tests.behaviour.context import Context

from tests.behaviour.config.parameters import MayError
from tests.behaviour.util.functor_encoder import (
    FunctorEncoderContext, normalize_functor_for_compare,
    _encode_pipeline, _encode_function,
    _encode_pipeline_annotations, _encode_function_annotations, _encode_fetch_annotations
)
from python.tests.behaviour.util.functor_encoder import _encode_function_annotations


@step("get answers of typeql analyze")
def step_impl(context: Context):
    context.analyzed = None
    context.analyzed = context.tx().analyze(context.text).resolve()

@step("typeql analyze{may_error:MayError}")
def step_impl(context, may_error: MayError):
    may_error.check(lambda: context.tx().analyze(context.text).resolve())


@step("analyzed query pipeline structure is")
def step_impl(context: Context):
    pipeline = context.analyzed.pipeline()
    actual_functor = _encode_pipeline(pipeline, FunctorEncoderContext(pipeline))
    assert_that(
        normalize_functor_for_compare(actual_functor),
        is_(equal_to(normalize_functor_for_compare(context.text)))
    )

@step("analyzed query preamble contains")
def step_impl(context):
    expected_functor = normalize_functor_for_compare(context.text)
    preamble_functors_unnormalized = [
        _encode_function(func, FunctorEncoderContext(func.body()))
        for func in context.analyzed.preamble()
    ]
    preamble_functors = [normalize_functor_for_compare(f) for f in preamble_functors_unnormalized]
    assert_that(preamble_functors, has_item(expected_functor))

@step("analyzed query pipeline annotations are")
def step_impl(context):
    pipeline = context.analyzed.pipeline()
    actual_functor = _encode_pipeline_annotations(pipeline, FunctorEncoderContext(pipeline))
    assert_that(
        normalize_functor_for_compare(actual_functor),
        is_(normalize_functor_for_compare(context.text))
    )


@step("analyzed preamble annotations contains")
def step_impl(context):
    expected_functor = normalize_functor_for_compare(context.text)
    preamble_functors_unnormalized = [
        _encode_function_annotations(func, FunctorEncoderContext(func.body()))
        for func in context.analyzed.preamble()
    ]
    preamble_functors = [normalize_functor_for_compare(f) for f in preamble_functors_unnormalized]
    assert_that(preamble_functors, has_item(expected_functor))


@step("analyzed fetch annotations are")
def step_impl(context):
    expected_functor = normalize_functor_for_compare(context.text)
    pipeline = context.analyzed.pipeline()
    fetch = context.analyzed.fetch()
    actual_functor = _encode_fetch_annotations(fetch, FunctorEncoderContext(pipeline))
    assert_that(
        normalize_functor_for_compare(actual_functor),
        is_(normalize_functor_for_compare(expected_functor))
    )
