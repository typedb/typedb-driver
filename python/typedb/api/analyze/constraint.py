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

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING, Iterator
from enum import IntEnum, IntEnum

from typedb.native_driver_wrapper import (
    comparator_get_name,

    Isa as NativeIsa,
    Has as NativeHas,
    Links as NativeLinks,
    Sub as NativeSub,
    Owns as NativeOwns,
    Relates as NativeRelates,
    Plays as NativePlays,
    FunctionCall as NativeFunctionCall,
    Expression as NativeExpression,
    Is as NativeIs,
    Iid as NativeIid,
    Comparison as NativeComparison,
    KindOf as NativeKindOf,
    Label as NativeLabel,
    Value as NativeValue,
    Or as NativeOr,
    Not as NativeNot,
    Try as NativeTry,

    Exact as NativeExact,
    Subtypes as NativeSubtypes,

    Equal as NativeEqual,
    NotEqual as NativeNotEqual,
    LessThan as NativeLessThan,
    LessOrEqual as NativeLessOrEqual,
    Greater as NativeGreater,
    GreaterOrEqual as NativeGreaterOrEqual,
    Like as NativeLike,
    Contains as NativeContains,
)

if TYPE_CHECKING:
    from typedb.api.analyze.constraint_vertex import ConstraintVertex

class ConstraintExactness(IntEnum):
    Exact = NativeExact
    Subtypes = NativeSubtypes

class Comparator(IntEnum):
    Equal = NativeEqual
    NotEqual = NativeNotEqual
    LessThan = NativeLessThan
    LessOrEqual = NativeLessOrEqual
    Greater = NativeGreater
    GreaterOrEqual = NativeGreaterOrEqual
    Like = NativeLike
    Contains = NativeContains

    def symbol(self):
        return comparator_get_name(self)

    def __str__(self):
        return self.symbol()

class ConstraintVariant(IntEnum):
    Isa = NativeIsa
    Has = NativeHas
    Links = NativeLinks
    Sub = NativeSub
    Owns = NativeOwns
    Relates = NativeRelates
    Plays = NativePlays
    FunctionCall = NativeFunctionCall
    Expression = NativeExpression
    Is = NativeIs
    Iid = NativeIid
    Comparison = NativeComparison
    KindOf = NativeKindOf
    Label = NativeLabel
    Value = NativeValue
    Or = NativeOr
    Not = NativeNot
    Try = NativeTry


class Constraint(ABC):
    """
    A representation of a TypeQL constraint.
    """

    @abstractmethod
    def variant(self) -> ConstraintVariant:
        """Gets the variant of this constraint."""
        raise NotImplementedError

    @abstractmethod
    def span(self) -> "Span":
        """Gets the span of this constraint in the source query."""
        raise NotImplementedError

    @abstractmethod
    def is_isa(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_has(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_links(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_sub(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_owns(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_relates(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_plays(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_function_call(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_expression(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_is(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_iid(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_comparison(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_kind_of(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_label(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_value(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_or(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_not(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_try(self) -> bool:
        raise NotImplementedError

    # Down-cast accessors for each concrete constraint variant
    @abstractmethod
    def as_isa(self) -> "Isa":
        raise NotImplementedError

    @abstractmethod
    def as_has(self) -> "Has":
        raise NotImplementedError

    @abstractmethod
    def as_links(self) -> "Links":
        raise NotImplementedError

    @abstractmethod
    def as_sub(self) -> "Sub":
        raise NotImplementedError

    @abstractmethod
    def as_owns(self) -> "Owns":
        raise NotImplementedError

    @abstractmethod
    def as_relates(self) -> "Relates":
        raise NotImplementedError

    @abstractmethod
    def as_plays(self) -> "Plays":
        raise NotImplementedError

    @abstractmethod
    def as_function_call(self) -> "FunctionCall":
        raise NotImplementedError

    @abstractmethod
    def as_expression(self) -> "Expression":
        raise NotImplementedError

    @abstractmethod
    def as_is(self) -> "Is":
        raise NotImplementedError

    @abstractmethod
    def as_iid(self) -> "Iid":
        raise NotImplementedError

    @abstractmethod
    def as_comparison(self) -> "Comparison":
        raise NotImplementedError

    @abstractmethod
    def as_kind(self) -> "KindOf":
        raise NotImplementedError

    @abstractmethod
    def as_label(self) -> "Label":
        raise NotImplementedError

    @abstractmethod
    def as_value(self) -> "Value":
        raise NotImplementedError

    @abstractmethod
    def as_or(self) -> "Or":
        raise NotImplementedError

    @abstractmethod
    def as_not(self) -> "Not":
        raise NotImplementedError

    @abstractmethod
    def as_try(self) -> "Try":
        raise NotImplementedError

    # Nested constraint subtype interfaces

class Isa(Constraint, ABC):
    """Represents an 'isa' constraint: <instance> isa(!) <type>"""

    @abstractmethod
    def instance(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def type(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def exactness(self) -> "ConstraintExactness":
        raise NotImplementedError

class Has(Constraint, ABC):
    """Represents a 'has' constraint: <owner> has <attribute>"""

    @abstractmethod
    def owner(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def attribute(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def exactness(self) -> "ConstraintExactness":
        raise NotImplementedError

class Links(Constraint, ABC):
    """Represents a 'links' constraint: <relation> links (<role>: <player>)"""

    @abstractmethod
    def relation(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def player(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def role(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def exactness(self) -> "ConstraintExactness":
        raise NotImplementedError

class Sub(Constraint, ABC):
    """Represents a 'sub' constraint: <subtype> sub(!) <supertype>"""

    @abstractmethod
    def subtype(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def supertype(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def exactness(self) -> "ConstraintExactness":
        raise NotImplementedError

class Owns(Constraint, ABC):
    """Represents an 'owns' constraint: <owner> owns <attribute>"""

    @abstractmethod
    def owner(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def attribute(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def exactness(self) -> "ConstraintExactness":
        raise NotImplementedError

class Relates(Constraint, ABC):
    """Represents a 'relates' constraint: <relation> relates <role>"""

    @abstractmethod
    def relation(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def role(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def exactness(self) -> "ConstraintExactness":
        raise NotImplementedError

class Plays(Constraint, ABC):
    """Represents a 'plays' constraint: <player> plays <role>"""

    @abstractmethod
    def player(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def role(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def exactness(self) -> "ConstraintExactness":
        raise NotImplementedError

class FunctionCall(Constraint, ABC):
    """Represents a function call: let <assigned> = name(<arguments>)"""

    @abstractmethod
    def name(self) -> str:
        raise NotImplementedError

    @abstractmethod
    def arguments(self) -> Iterator["ConstraintVertex"]:
        raise NotImplementedError

    @abstractmethod
    def assigned(self) -> Iterator["ConstraintVertex"]:
        raise NotImplementedError

class Expression(Constraint, ABC):
    """Represents an expression: let <assigned> = <expression>"""

    @abstractmethod
    def text(self) -> str:
        raise NotImplementedError

    @abstractmethod
    def arguments(self) -> Iterator["ConstraintVertex"]:
        raise NotImplementedError

    @abstractmethod
    def assigned(self) -> "ConstraintVertex":
        raise NotImplementedError

class Is(Constraint, ABC):
    """Represents an 'is' constraint: <lhs> is <rhs>"""

    @abstractmethod
    def lhs(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def rhs(self) -> "ConstraintVertex":
        raise NotImplementedError

class Iid(Constraint, ABC):
    """Represents an IID constraint: <concept> iid <iid>"""

    @abstractmethod
    def variable(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def iid(self) -> str:
        raise NotImplementedError

class Comparison(Constraint, ABC):
    """Represents a comparison: <lhs> <comparator> <rhs>"""

    @abstractmethod
    def lhs(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def rhs(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def comparator(self) -> "Comparator":
        raise NotImplementedError

    @staticmethod
    def comparator_name(comparator: "Comparator") -> str:
        """
        Returns the name of the comparator. Implementation may delegate to JNI helper.
        """
        # If a JNI helper is available it may be used here; otherwise override in concrete impl.
        raise NotImplementedError

class KindOf(Constraint, ABC):
    """Represents a kind constraint: <kind> <type>"""

    @abstractmethod
    def kind(self) -> "KindOf":
        raise NotImplementedError

    @abstractmethod
    def type(self) -> "ConstraintVertex":
        raise NotImplementedError

class Label(Constraint, ABC):
    """Represents a label constraint: <type> label <label>"""

    @abstractmethod
    def variable(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def label(self) -> str:
        raise NotImplementedError

class Value(Constraint, ABC):
    """Represents a value constraint: <attribute_type> value <value_type>"""

    @abstractmethod
    def attribute_type(self) -> "ConstraintVertex":
        raise NotImplementedError

    @abstractmethod
    def value_type(self) -> str:
        raise NotImplementedError

class Or(Constraint, ABC):
    """Represents an 'or' constraint: { <branches[0]> } or { <branches[1]> } [or ...]"""

    @abstractmethod
    def branches(self) -> Iterator["ConjunctionID"]:
        """
        Index into Pipeline.conjunctions
        """
        raise NotImplementedError

class Not(Constraint, ABC):
    """Represents a 'not' constraint: not { <conjunction> }"""

    @abstractmethod
    def conjunction(self) -> "ConjunctionID":
        """
        Index into Pipeline.conjunctions
        """
        raise NotImplementedError

class Try(Constraint, ABC):
    """Represents a 'try' constraint: try { <conjunction> }"""

    @abstractmethod
    def conjunction(self) -> "ConjunctionID":
        """
        Index into Pipeline.conjunctions
        """
        raise NotImplementedError

class Span(ABC):
    """The span of a constraint in the source query."""

    @abstractmethod
    def begin(self) -> int:
        """The offset of the first character."""
        raise NotImplementedError

    @abstractmethod
    def end(self) -> int:
        """The offset after the last character."""
        raise NotImplementedError