#
# Copyright (C) 2022 Vaticle
#
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
#

from behave import *
from hamcrest import *
from typedb.client import *

from tests.behaviour.config.parameters import parse_bool, parse_list, RootLabel, parse_label
from tests.behaviour.context import Context


@step("put {root_label:RootLabel} type: {type_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    if root_label == RootLabel.ENTITY:
        context.tx().concepts.put_entity_type(type_label)
    elif root_label == RootLabel.RELATION:
        context.tx().concepts.put_relation_type(type_label)
    else:
        raise ValueError("Unrecognised value")


@step("delete {root_label:RootLabel} type: {type_label}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    try:
        context.get_thing_type(root_label, type_label).delete(context.tx())
        assert False
    except TypeDBClientException:
        pass


@step("delete {root_label:RootLabel} type: {type_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    context.get_thing_type(root_label, type_label).delete(context.tx())


@step("{root_label:RootLabel}({type_label:Label}) is null: {is_null}")
def step_impl(context: Context, root_label: RootLabel, type_label: Label, is_null):
    is_null = parse_bool(is_null)
    assert_that(context.get_thing_type(root_label, type_label.name) is None, is_(is_null))


@step("{root_label:RootLabel}({type_label}) set label: {new_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, new_label: str):
    context.get_thing_type(root_label, type_label).set_label(context.tx(), new_label)


@step("{root_label:RootLabel}({type_label:Label}) get label: {get_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: Label, get_label: str):
    assert_that(context.get_thing_type(root_label, type_label.name).get_label().name, is_(get_label))


@step("{root_label:RootLabel}({type_label}) set abstract: {is_abstract}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, is_abstract: str):
    is_abstract = parse_bool(is_abstract)
    thing_type = context.get_thing_type(root_label, type_label)
    try:
        if is_abstract:
            thing_type.set_abstract(context.tx())
        else:
            thing_type.unset_abstract(context.tx())
        assert False
    except TypeDBClientException:
        pass


@step("{root_label:RootLabel}({type_label}) set abstract: {is_abstract}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, is_abstract: str):
    is_abstract = parse_bool(is_abstract)
    thing_type = context.get_thing_type(root_label, type_label)
    if is_abstract:
        thing_type.set_abstract(context.tx())
    else:
        thing_type.unset_abstract(context.tx())


@step("{root_label:RootLabel}({type_label}) is abstract: {is_abstract}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, is_abstract: str):
    is_abstract = parse_bool(is_abstract)
    assert_that(context.get_thing_type(root_label, type_label).is_abstract(), is_(is_abstract))


@step("{root_label:RootLabel}({type_label}) set supertype: {super_label}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, super_label: str):
    if root_label == RootLabel.ENTITY:
        entity_supertype = context.tx().concepts.get_entity_type(super_label)
        try:
            context.tx().concepts.get_entity_type(type_label).set_supertype(context.tx(), entity_supertype)
            assert False
        except TypeDBClientException:
            pass
    elif root_label == RootLabel.ATTRIBUTE:
        attribute_supertype = context.tx().concepts.get_attribute_type(super_label)
        try:
            context.tx().concepts.get_attribute_type(type_label).set_supertype(context.tx(), attribute_supertype)
            assert False
        except TypeDBClientException:
            pass
    elif root_label == RootLabel.RELATION:
        relation_supertype = context.tx().concepts.get_relation_type(super_label)
        try:
            context.tx().concepts.get_relation_type(type_label).set_supertype(context.tx(), relation_supertype)
            assert False
        except TypeDBClientException:
            pass
    else:
        raise ValueError("Unrecognised value")


@step("{root_label:RootLabel}({type_label}) set supertype: {super_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, super_label: str):
    if root_label == RootLabel.ENTITY:
        entity_supertype = context.tx().concepts.get_entity_type(super_label)
        context.tx().concepts.get_entity_type(type_label).set_supertype(context.tx(), entity_supertype)
    elif root_label == RootLabel.ATTRIBUTE:
        attribute_supertype = context.tx().concepts.get_attribute_type(super_label)
        context.tx().concepts.get_attribute_type(type_label).set_supertype(context.tx(), attribute_supertype)
    elif root_label == RootLabel.RELATION:
        relation_supertype = context.tx().concepts.get_relation_type(super_label)
        context.tx().concepts.get_relation_type(type_label).set_supertype(context.tx(), relation_supertype)
    else:
        raise ValueError("Unrecognised value")


@step("{root_label:RootLabel}({type_label}) get supertype: {super_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, super_label: str):
    supertype = context.get_thing_type(root_label, super_label)
    assert_that(context.get_thing_type(root_label, type_label).get_supertype(context.tx()), is_(supertype))


@step("{root_label:RootLabel}({type_label}) get supertypes contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    super_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = [t.get_label() for t in context.get_thing_type(root_label, type_label).get_supertypes(context.tx())]
    for super_label in super_labels:
        assert_that(actuals, has_item(super_label))


@step("{root_label:RootLabel}({type_label}) get supertypes do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    super_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = [t.get_label() for t in context.get_thing_type(root_label, type_label).get_supertypes(context.tx())]
    for super_label in super_labels:
        assert_that(actuals, not_(has_item(super_label)))


@step("{root_label:RootLabel}({type_label}) get subtypes contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    sub_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = [t.get_label() for t in context.get_thing_type(root_label, type_label).get_subtypes(context.tx())]
    for sub_label in sub_labels:
        assert_that(actuals, has_item(sub_label))


@step("{root_label:RootLabel}({type_label}) get subtypes do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    sub_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = [t.get_label() for t in context.get_thing_type(root_label, type_label).get_subtypes(context.tx())]
    for sub_label in sub_labels:
        assert_that(actuals, not_(has_item(sub_label)))


def set_owns_attribute_type_as_type_with_annotations_throws_exception(context: Context, root_label: RootLabel, type_label: str,
                                                                      att_type_label: str, overridden_label: str,
                                                                      annotations: set["Annotation"]):
    attribute_type = context.tx().concepts.get_attribute_type(att_type_label)
    overridden_type = context.tx().concepts.get_attribute_type(overridden_label)
    try:
        context.get_thing_type(root_label, type_label).set_owns(context.tx(), attribute_type, overridden_type, annotations=annotations)
        assert False
    except TypeDBClientException:
        pass


@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label} as {overridden_label}, with annotations: {annotations:Annotations}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, overridden_label: str, annotations: set["Annotation"]):
    set_owns_attribute_type_as_type_with_annotations_throws_exception(context, root_label, type_label, att_type_label, overridden_label, annotations)


@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label} as {overridden_label}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, overridden_label: str):
    set_owns_attribute_type_as_type_with_annotations_throws_exception(context, root_label, type_label, att_type_label, overridden_label, set())


def set_owns_attribute_type_with_annotations_throws_exception(context: Context, root_label: RootLabel, type_label: str,
                                                              att_type_label: str, annotations: set["Annotation"]):
    attribute_type = context.tx().concepts.get_attribute_type(att_type_label)
    try:
        context.get_thing_type(root_label, type_label).set_owns(context.tx(), attribute_type, annotations=annotations)
        assert False
    except TypeDBClientException:
        pass

@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label}, with annotations: {annotations:Annotations}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, annotations: set["Annotation"]):
    set_owns_attribute_type_with_annotations_throws_exception(context, root_label, type_label, att_type_label, annotations)


@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str):
    set_owns_attribute_type_with_annotations_throws_exception(context, root_label, type_label, att_type_label, set())


def set_owns_attribute_type_as_type_with_annotations(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, overridden_label: str, annotations: set["Annotation"]):
    attribute_type = context.tx().concepts.get_attribute_type(att_type_label)
    overridden_type = context.tx().concepts.get_attribute_type(overridden_label)
    context.get_thing_type(root_label, type_label).set_owns(context.tx(), attribute_type, overridden_type, annotations=annotations)


@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label} as {overridden_label}, with annotations: {annotations:Annotations}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, overridden_label: str, annotations: set["Annotation"]):
    set_owns_attribute_type_as_type_with_annotations(context, root_label, type_label, att_type_label, overridden_label, annotations)


@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label} as {overridden_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, overridden_label: str):
    set_owns_attribute_type_as_type_with_annotations(context, root_label, type_label, att_type_label, overridden_label, set())


def set_owns_attribute_type_with_annotations(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, annotations: set["Annotation"]):
    attribute_type = context.tx().concepts.get_attribute_type(att_type_label)
    context.get_thing_type(root_label, type_label).set_owns(context.tx(), attribute_type, annotations=annotations)


@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label}, with annotations: {annotations:Annotations}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str, annotations: set["Annotation"]):
    set_owns_attribute_type_with_annotations(context, root_label, type_label, att_type_label, annotations)


@step("{root_label:RootLabel}({type_label}) set owns attribute type: {att_type_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str):
    set_owns_attribute_type_with_annotations(context, root_label, type_label, att_type_label, set())


@step("{root_label:RootLabel}({type_label}) unset owns attribute type: {att_type_label}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str):
    attribute_type = context.tx().concepts.get_attribute_type(att_type_label)
    try:
        context.get_thing_type(root_label, type_label).unset_owns(context.tx(), attribute_type)
        assert False
    except TypeDBClientException:
        pass


@step("{root_label:RootLabel}({type_label}) unset owns attribute type: {att_type_label}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, att_type_label: str):
    attribute_type = context.tx().concepts.get_attribute_type(att_type_label)
    context.get_thing_type(root_label, type_label).unset_owns(context.tx(), attribute_type)


def get_actual_owns_types(context: Context, root_label: RootLabel, type_label: str, annotations):
    return [t.get_label() for t in context.get_thing_type(root_label, type_label).get_owns(context.tx(), annotations=annotations)]


def get_owns_attribute_types_with_annotations_contains(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    attribute_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_owns_types(context, root_label, type_label, annotations)
    for attribute_label in attribute_labels:
        assert_that(actuals, has_item(attribute_label))

@step("{root_label:RootLabel}({type_label}) get owns attribute types, with annotations: {annotations:Annotations}; contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    get_owns_attribute_types_with_annotations_contains(context, root_label, type_label, annotations)


@step("{root_label:RootLabel}({type_label}) get owns attribute types contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    get_owns_attribute_types_with_annotations_contains(context, root_label, type_label, set())


def get_owns_attribute_types_with_annotations_do_not_contain(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    attribute_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_owns_types(context, root_label, type_label, annotations)
    for attribute_label in attribute_labels:
        assert_that(actuals, not_(has_item(attribute_label)))

@step("{root_label:RootLabel}({type_label}) get owns attribute types, with annotations: {annotations:Annotations}; do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    get_owns_attribute_types_with_annotations_do_not_contain(context, root_label, type_label, annotations)


@step("{root_label:RootLabel}({type_label}) get owns attribute types do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    get_owns_attribute_types_with_annotations_do_not_contain(context, root_label, type_label, set())


def get_owns_explicit_attribute_types_with_annotations_contain(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    attribute_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = [t.get_label()
               for t in context.get_thing_type(root_label, type_label).get_owns(context.tx(), annotations=annotations,
                                                                                transitivity=Transitivity.EXPLICIT)]
    for attribute_label in attribute_labels:
        assert_that(actuals, has_item(attribute_label))


@step("{root_label:RootLabel}({type_label}) get owns explicit attribute types, with annotations: {annotations:Annotations}; contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    get_owns_explicit_attribute_types_with_annotations_contain(context, root_label, type_label, annotations)


@step("{root_label:RootLabel}({type_label}) get owns explicit attribute types contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    get_owns_explicit_attribute_types_with_annotations_contain(context, root_label, type_label, set())


def get_owns_explicit_attribute_types_with_annotations_do_not_contain(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    attribute_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = [t.get_label()
               for t in context.get_thing_type(root_label, type_label).get_owns(context.tx(), annotations=annotations,
                                                                                transitivity=Transitivity.EXPLICIT)]
    for attribute_label in attribute_labels:
        assert_that(actuals, not_(has_item(attribute_label)))


@step("{root_label:RootLabel}({type_label}) get owns explicit attribute types, with annotations: {annotations:Annotations}; do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str, annotations: set["Annotation"]):
    get_owns_explicit_attribute_types_with_annotations_do_not_contain(context, root_label, type_label, annotations)


@step("{root_label:RootLabel}({type_label}) get owns explicit attribute types do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    get_owns_explicit_attribute_types_with_annotations_do_not_contain(context, root_label, type_label, set())


@step("{root_label:RootLabel}({type_label:Label}) get owns overridden attribute({attr_type_label}) is null: {is_null}")
def step_impl(context: Context, root_label: RootLabel, type_label: Label, attr_type_label: str, is_null):
    is_null = parse_bool(is_null)
    attribute_type = context.tx().concepts.get_attribute_type(attr_type_label)
    assert_that(context.get_thing_type(root_label, type_label.name).get_owns_overridden(context.tx(), attribute_type) is None, is_(is_null))


@step("{root_label:RootLabel}({type_label:Label}) get owns overridden attribute({attr_type_label}) get label: {label}")
def step_impl(context: Context, root_label: RootLabel, type_label: Label, attr_type_label: str, label: str):
    attribute_type = context.tx().concepts.get_attribute_type(attr_type_label)
    assert_that(context.get_thing_type(root_label, type_label.name).get_owns_overridden(context.tx(), attribute_type).get_label().name, is_(label))


@step("{root_label:RootLabel}({type_label}) set plays role: {role_label:ScopedLabel} as {overridden_label:ScopedLabel}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, role_label: Label, overridden_label: Label):
    role_type = context.tx().concepts.get_relation_type(role_label.scope).get_relates(context.tx(), role_label.name)
    overridden_type = context.tx().concepts.get_relation_type(overridden_label.scope).get_relates(context.tx(), overridden_label.name)
    try:
        context.get_thing_type(root_label, type_label).set_plays(context.tx(), role_type, overridden_type)
        assert False
    except TypeDBClientException:
        pass


@step("{root_label:RootLabel}({type_label}) set plays role: {role_label:ScopedLabel} as {overridden_label:ScopedLabel}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, role_label: Label, overridden_label: Label):
    role_type = context.tx().concepts.get_relation_type(role_label.scope).get_relates(context.tx(), role_label.name)
    overridden_type = context.tx().concepts.get_relation_type(overridden_label.scope).get_relates(context.tx(), overridden_label.name)
    context.get_thing_type(root_label, type_label).set_plays(context.tx(), role_type, overridden_type)


@step("{root_label:RootLabel}({type_label}) set plays role: {role_label:ScopedLabel}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, role_label: Label):
    role_type = context.tx().concepts.get_relation_type(role_label.scope).get_relates(context.tx(), role_label.name)
    try:
        context.get_thing_type(root_label, type_label).set_plays(context.tx(), role_type)
        assert False
    except TypeDBClientException:
        pass


@step("{root_label:RootLabel}({type_label}) set plays role: {role_label:ScopedLabel}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, role_label: Label):
    role_type = context.tx().concepts.get_relation_type(role_label.scope).get_relates(context.tx(), role_label.name)
    context.get_thing_type(root_label, type_label).set_plays(context.tx(), role_type)


@step("{root_label:RootLabel}({type_label}) unset plays role: {role_label:ScopedLabel}; throws exception")
def step_impl(context: Context, root_label: RootLabel, type_label: str, role_label: Label):
    role_type = context.tx().concepts.get_relation_type(role_label.scope).get_relates(context.tx(), role_label.name)
    try:
        context.get_thing_type(root_label, type_label).unset_plays(context.tx(), role_type)
        assert False
    except TypeDBClientException:
        pass


@step("{root_label:RootLabel}({type_label}) unset plays role: {role_label:ScopedLabel}")
def step_impl(context: Context, root_label: RootLabel, type_label: str, role_label: Label):
    role_type = context.tx().concepts.get_relation_type(role_label.scope).get_relates(context.tx(), role_label.name)
    context.get_thing_type(root_label, type_label).unset_plays(context.tx(), role_type)


def get_actual_plays(context: Context, root_label: RootLabel, type_label: str):
    return [t.get_label() for t in context.get_thing_type(root_label, type_label).get_plays(context.tx())]


@step("{root_label:RootLabel}({type_label}) get playing roles contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_plays(context, root_label, type_label)
    for role_label in role_labels:
        assert_that(role_label, is_in(actuals))


@step("{root_label:RootLabel}({type_label}) get playing roles do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_plays(context, root_label, type_label)
    for role_label in role_labels:
        assert_that(role_label, not_(is_in(actuals)))


def get_actual_plays_explicit(context: Context, root_label: RootLabel, type_label: str):
    return [t.get_label() for t in context.get_thing_type(root_label, type_label).get_plays(context.tx(),
                                                                                            Transitivity.EXPLICIT)]


@step("{root_label:RootLabel}({type_label}) get playing roles explicit contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_plays_explicit(context, root_label, type_label)
    for role_label in role_labels:
        assert_that(role_label, is_in(actuals))


@step("{root_label:RootLabel}({type_label}) get playing roles explicit do not contain")
def step_impl(context: Context, root_label: RootLabel, type_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_plays_explicit(context, root_label, type_label)
    for role_label in role_labels:
        assert_that(role_label, not_(is_in(actuals)))
