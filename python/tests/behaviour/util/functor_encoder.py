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

import re
from enum import Enum

from typedb.driver import (
    Constraint, ConstraintVertex, ConstraintVariant, ConstraintExactness,
    Pipeline, PipelineStage, PipelineStageVariant, Fetch, FetchVariant,
    Function, ReturnOperation, ReturnOperationVariant,
    ReduceStage, Reducer, SortStage, VariableAnnotations,
    Type
)

from typing import Iterable, Tuple

class FunctorEncoderContext:
    def __init__(self, pipeline: Pipeline):
        self.pipeline = pipeline


WHITE_SPACE_REGEX = re.compile(r'\s+')
SUBPATTERN_VARIANTS = [ConstraintVariant.Or, ConstraintVariant.Try, ConstraintVariant.Not]


def normalize_functor_for_compare(functor: str) -> str:
    return re.sub(WHITE_SPACE_REGEX, '', functor.lower())

def _make_functor(name: str, *args) -> str:
    args = ", ".join(arg if isinstance(arg, str) else arg.encode_as_functor() for arg in args)
    return f"{name}({args})"


def _may_encode(e: any, context: FunctorEncoderContext) -> str:
    return e if isinstance(e, str) else e.encode_as_functor(context)


def _encode_as_list(elements: Iterable[any], context: FunctorEncoderContext) -> str:
    return "[" + ", ".join(_may_encode(e, context) for e in elements) + "]"


def _encode_as_map(elements: Iterable[Tuple[any, any]], context: FunctorEncoderContext) -> str:
    encoded_kv = sorted(list((_may_encode(k, context), _may_encode(v, context)) for (k, v) in elements))
    return "{" + ",".join("%s : %s" %(k, v) for (k, v) in encoded_kv) + "}"


def _encode_enum_as_name(self: Enum):
    return self.name


def _encode_conjunction_id(conj_id: "ConjunctionID", context: FunctorEncoderContext) -> str:
    conjunction = context.pipeline.conjunction(conj_id)
    return _encode_as_list(conjunction.constraints(), context)


def _encode_constraint(constraint: Constraint, context: FunctorEncoderContext) -> str:
    variant = constraint.variant()

    def with_exactness(constraint_name: str, exactness: ConstraintExactness) -> str:
        suffix = "Exact" if exactness == ConstraintExactness.Exact else ""
        return constraint_name + suffix

    if constraint.is_isa():
        isa = constraint.as_isa()
        name = with_exactness("Isa", isa.exactness())
        a = _encode_constraint_vertex(isa.instance(), context)
        b = _encode_constraint_vertex(isa.type(), context)
        return _make_functor(name, f"{a},{b}")

    elif constraint.is_has():
        has = constraint.as_has()
        name = with_exactness("Has", has.exactness())
        a = _encode_constraint_vertex(has.owner(), context)
        b = _encode_constraint_vertex(has.attribute(), context)
        return _make_functor(name, f"{a},{b}")

    elif constraint.is_links():
        links = constraint.as_links()
        name = with_exactness("Links", links.exactness())
        a = _encode_constraint_vertex(links.relation(), context)
        b = _encode_constraint_vertex(links.player(), context)
        c = _encode_constraint_vertex(links.role(), context)
        return _make_functor(name, f"{a},{b},{c}")

    elif constraint.is_sub():
        sub = constraint.as_sub()
        name = with_exactness("Sub", sub.exactness())
        a = _encode_constraint_vertex(sub.subtype(), context)
        b = _encode_constraint_vertex(sub.supertype(), context)
        return _make_functor(name, f"{a},{b}")

    elif constraint.is_owns():
        owns = constraint.as_owns()
        name = with_exactness("Owns", owns.exactness())
        a = _encode_constraint_vertex(owns.owner(), context)
        b = _encode_constraint_vertex(owns.attribute(), context)
        return _make_functor(name, f"{a},{b}")

    elif constraint.is_relates():
        relates = constraint.as_relates()
        name = with_exactness("Relates", relates.exactness())
        a = _encode_constraint_vertex(relates.relation(), context)
        b = _encode_constraint_vertex(relates.role(), context)
        return _make_functor(name, f"{a},{b}")

    elif constraint.is_plays():
        plays = constraint.as_plays()
        name = with_exactness("Plays", plays.exactness())
        a = _encode_constraint_vertex(plays.player(), context)
        b = _encode_constraint_vertex(plays.role(), context)
        return _make_functor(name, f"{a},{b}")

    elif constraint.is_function_call():
        fc = constraint.as_function_call()
        assigned = _encode_as_list(fc.assigned(), context)
        args = _encode_as_list(fc.arguments(), context)
        return _make_functor("FunctionCall", f"{fc.name()},{assigned},{args}")

    elif constraint.is_expression():
        ex = constraint.as_expression()
        assigned = _encode_constraint_vertex(ex.assigned(), context)
        args = _encode_as_list(ex.arguments(), context)
        return _make_functor("Expression", f"{ex.text()},{assigned},{args}")

    elif constraint.is_is():
        is_ = constraint.as_is()
        a = _encode_constraint_vertex(is_.lhs(), context)
        b = _encode_constraint_vertex(is_.rhs(), context)
        return _make_functor("Is", f"{a},{b}")

    elif constraint.is_iid():
        iid = constraint.as_iid()
        a = _encode_constraint_vertex(iid.variable(), context)
        return _make_functor("Iid", f"{a},{iid.iid()}")

    elif constraint.is_comparison():
        comp = constraint.as_comparison()
        a = _encode_constraint_vertex(comp.lhs(), context)
        b = _encode_constraint_vertex(comp.rhs(), context)
        comparator = comp.comparator()
        comp_name = comparator.symbol()
        return _make_functor("Comparison", f"{a},{b},{comp_name}")

    elif constraint.is_kind_of():
        k = constraint.as_kind()
        kind_name = getattr(k.kind(), "name", None) or str(k.kind())
        t = _encode_constraint_vertex(k.type(), context)
        return _make_functor("Kind", f"{kind_name},{t}")

    elif constraint.is_label():
        as_label = constraint.as_label()
        variable = as_label.variable().encode_as_functor(context)
        label = as_label.label()
        return _make_functor("Label", variable, label)

    elif constraint.is_value():
        val = constraint.as_value()
        a = _encode_constraint_vertex(val.attribute_type(), context)
        return _make_functor("Value", f"{a},{val.value_type()}")

    elif constraint.is_or():
        or_ = constraint.as_or()
        branches = [_encode_conjunction_id(b, context) for b in or_.branches()]
        return _make_functor("Or", "[" + ",".join(branches) + "]")

    elif constraint.is_not():
        not_ = constraint.as_not()
        conj = _encode_conjunction_id(not_.conjunction(), context)
        return _make_functor("Not", conj)

    elif constraint.is_try():
        try_ = constraint.as_try()
        conj = _encode_conjunction_id(try_.conjunction(), context)
        return _make_functor("Try", conj)
    else:
        raise Exception(f"Unknown constraint variant: " + variant.name)


def _encode_constraint_vertex(constraint_vertex: ConstraintVertex, context: FunctorEncoderContext) -> str:
    if constraint_vertex.is_variable():
        return constraint_vertex.as_variable().encode_as_functor(context)
    elif constraint_vertex.is_label():
        type_ = constraint_vertex.as_label()
        return type_.get_label()
    elif constraint_vertex.is_value():
        value = constraint_vertex.as_value()
        return f"\"{value}\"" if value.is_string() else str(value)
    elif constraint_vertex.is_named_role():
        return constraint_vertex.as_named_role_get_name()


def _encode_variable(self, context: FunctorEncoderContext) -> str:
    name = context.pipeline.get_variable_name(self)
    return "$" + ("_" if name is None else name)


def _encode_sort_variable(self, context: FunctorEncoderContext) -> str:
    order = "Asc" if self.order() == SortStage.SortOrderVariant.Ascending else "Desc"
    return _make_functor(order, self.variable().encode_as_functor(context))


def _encode_reduce_assignment(self, context: FunctorEncoderContext) -> str:
    return _make_functor(
        "ReduceAssign",
        self.assigned().encode_as_functor(context),
        self.reducer().encode_as_functor(context)
    )


def _encode_reducer(self, context: FunctorEncoderContext) -> str:
    args = _encode_as_list(self.arguments(), context)
    return _make_functor("Reducer", self.name(), args)


def _encode_pipeline_stage(self: PipelineStage, context: FunctorEncoderContext) -> str:
    variant = self.variant()
    name = variant.name
    if variant in [PipelineStageVariant.Match, PipelineStageVariant.Insert, PipelineStageVariant.Put, PipelineStageVariant.Update]:
        return _make_functor(name, _encode_conjunction_id(self.block(), context))
    elif variant == PipelineStageVariant.Delete:
        deleted_vars = _encode_as_list(self.as_delete().deleted_variables(), context)
        block = self.as_delete().block().encode_as_functor(context)
        return _make_functor(name, deleted_vars, block)
    elif variant == PipelineStageVariant.Select:
        vars_ = _encode_as_list(self.as_select().variables(), context)
        return _make_functor(name, vars_)
    elif variant == PipelineStageVariant.Sort:
        return _make_functor(name, _encode_as_list(self.as_sort().variables(), context))
    elif variant == PipelineStageVariant.Require:
        vars_ = _encode_as_list(self.as_require().variables(), context)
        return _make_functor(name, vars_)
    elif variant == PipelineStageVariant.Offset:
        return _make_functor(name, str(self.as_offset().offset()))
    elif variant == PipelineStageVariant.Limit:
        return _make_functor(name, str(self.as_limit().limit()))
    elif variant == PipelineStageVariant.Distinct:
        return _make_functor(name)
    elif variant == PipelineStageVariant.Reduce:
        reduce_assignments = _encode_as_list(self.as_reduce().reduce_assignments(), context)
        group_by = _encode_as_list(self.as_reduce().group_by(), context)
        return _make_functor(name, reduce_assignments, group_by)


def _encode_pipeline(self: Pipeline, context: FunctorEncoderContext) -> str:
    stages = _encode_as_list(self.stages(), context)
    return _make_functor("Pipeline", stages)


def _encode_return_operation(self: ReturnOperation, context: FunctorEncoderContext) -> str:
    variant = self.variant()
    if variant == ReturnOperationVariant.StreamReturn:
        return _make_functor("Stream", _encode_as_list(self.as_stream().variables(), context))
    elif variant == ReturnOperationVariant.SingleReturn:
        return _make_functor("Single", self.as_single().selector(),
                             _encode_as_list(self.as_single().variables(), context))
    elif variant == ReturnOperationVariant.CheckReturn:
        return _make_functor("Check", "")
    elif variant == ReturnOperationVariant.ReduceReturn:
        return _make_functor("Reduce", _encode_as_list(self.as_reduce().reducers(), context))
    else:
        raise Exception(f"Unknown return-operation variant: " + variant.name)


def _encode_function(self: Function, context: FunctorEncoderContext) -> str:
    return _make_functor(
        "Function",
        _encode_as_list(self.argument_variables(), context),
        self.return_operation().encode_as_functor(context),
        self.body().encode_as_functor(context)
    )


def _encode_type(self: Type, context: FunctorEncoderContext) -> str:
    return self.get_label()


def _encode_variable_annotations(self: VariableAnnotations, context: FunctorEncoderContext) -> str:
    if self.is_instance():
        return _make_functor("Instance", _encode_as_list(self.as_instance(), context))
    elif self.is_type():
        return _make_functor("Type", _encode_as_list(self.as_type(), context))
    elif self.is_value():
        return _make_functor("Value", _encode_as_list(self.as_value(), context))
    else:
        raise Exception(f"Unknown VariableAnnoations variant: " + self.variant().name)


def _encode_subpattern_annotations(self: Constraint, context: FunctorEncoderContext) -> str:
    if self.is_or():
        branches = [_encode_conjunction_annotations(branch, context) for branch in self.as_or().branches()]
        return _make_functor("Or", _encode_as_list(branches, context))
    elif self.is_not():
        inner_annotations = _encode_conjunction_annotations(self.as_not().conjunction(), context)
        return _make_functor("Not", inner_annotations)
    elif self.is_try():
        inner_annotations = _encode_conjunction_annotations(self.as_try().conjunction(), context)
        return _make_functor("Try", inner_annotations)
    else:
        raise Exception(f"Illegal Subpattern constraint variant: " + self.variant().name)



def _encode_conjunction_annotations(self: "ConjunctionID", context: FunctorEncoderContext) -> str:
    conj = context.pipeline.conjunction(self)

    trunk_annotations = ((var, conj.variable_annotations(var)) for var in conj.annotated_variables())

    subpatterns = [c for c in conj.constraints() if c.variant() in SUBPATTERN_VARIANTS]
    subpattern_annotations = [_encode_subpattern_annotations(c, context) for c in subpatterns]

    return _make_functor(
        "And",
        _encode_as_map(((k, v) for (k, v) in trunk_annotations), context),
        _encode_as_list((s for s in subpattern_annotations), context)
    )


def _encode_stage_annotations(self: PipelineStage, context: FunctorEncoderContext) -> str:
    variant = self.variant()
    if variant in [PipelineStageVariant.Match, PipelineStageVariant.Insert, PipelineStageVariant.Update,
                   PipelineStageVariant.Put, PipelineStageVariant.Delete]:
        return _make_functor(variant.name, _encode_conjunction_annotations(self.block(), context))
    else:
        return _make_functor(variant.name, "")


def _encode_pipeline_annotations(self: Pipeline, context: FunctorEncoderContext) -> str:
    stage_annotations = [_encode_stage_annotations(s, context) for s in self.stages()]
    return _make_functor("Pipeline", _encode_as_list((s for s in stage_annotations), context))


def _encode_function_annotations(self: Function, context: FunctorEncoderContext) -> str:
    argument_annotations = _encode_as_list(self.argument_annotations(), context)
    return_op_name = self.return_operation().variant().name.replace("Return", "")
    return_annotations = _make_functor(return_op_name, _encode_as_list(self.return_annotations(), context))
    body_annotations = _encode_pipeline_annotations(self.body(), context)
    return _make_functor("Function", argument_annotations, return_annotations, body_annotations)


def _encode_fetch_annotations(self: Fetch, context: FunctorEncoderContext) -> str:
    if self.is_leaf():
        return _encode_as_list(self.as_leaf().annotations(), context)
    elif self.is_list():
        return _make_functor("List", self.as_list().element().encode_as_functor(context))
    if self.is_object():
        as_object = self.as_object()
        kv = [(k, self.get(k)) for k in as_object.keys()]
        return _encode_as_map(kv, context)
    else:
        raise Exception(f"Unknown fetch variant: " + self.variant().name)


# When this file is imported, we inject the encode_as_functor implementations
def monkey_patch_all():
    import typedb
    Enum.encode_as_functor = _encode_enum_as_name

    Constraint.encode_as_functor = _encode_constraint
    ConstraintVertex.encode_as_functor = _encode_constraint_vertex
    Fetch.encode_as_functor = _encode_fetch_annotations
    Function.encode_as_functor = _encode_function
    Pipeline.encode_as_functor = _encode_pipeline
    PipelineStage.encode_as_functor = _encode_pipeline_stage
    ReturnOperation.encode_as_functor = _encode_return_operation
    typedb.native_driver_wrapper.ConjunctionID.encode_as_functor = _encode_conjunction_id
    ReduceStage.ReduceAssignment.encode_as_functor = _encode_reduce_assignment
    Reducer.encode_as_functor = _encode_reducer
    SortStage.SortVariable.encode_as_functor = _encode_sort_variable
    Type.encode_as_functor = _encode_type
    typedb.native_driver_wrapper.Variable.encode_as_functor = _encode_variable
    VariableAnnotations.encode_as_functor = _encode_variable_annotations

monkey_patch_all()
