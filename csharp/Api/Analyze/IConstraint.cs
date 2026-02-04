/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

using System.Collections.Generic;

namespace TypeDB.Driver.Api.Analyze
{
    /// <summary>
    /// A representation of a TypeQL constraint.
    /// </summary>
    public interface IConstraint
    {
        /// <summary>
        /// Gets the variant of this constraint.
        /// </summary>
        Pinvoke.ConstraintVariant Variant { get; }

        /// <summary>
        /// Gets the span of this constraint in the source query.
        /// </summary>
        ISpan Span { get; }

        bool IsIsa { get; }
        bool IsHas { get; }
        bool IsLinks { get; }
        bool IsSub { get; }
        bool IsOwns { get; }
        bool IsRelates { get; }
        bool IsPlays { get; }
        bool IsFunctionCall { get; }
        bool IsExpression { get; }
        bool IsIs { get; }
        bool IsIid { get; }
        bool IsComparison { get; }
        bool IsKind { get; }
        bool IsLabel { get; }
        bool IsValue { get; }
        bool IsOr { get; }
        bool IsNot { get; }
        bool IsTry { get; }

        IIsa AsIsa();
        IHas AsHas();
        ILinks AsLinks();
        ISub AsSub();
        IOwns AsOwns();
        IRelates AsRelates();
        IPlays AsPlays();
        IFunctionCall AsFunctionCall();
        IExpression AsExpression();
        IIs AsIs();
        IIid AsIid();
        IComparison AsComparison();
        IKind AsKind();
        ILabelConstraint AsLabel();
        IValueConstraint AsValue();
        IOr AsOr();
        INot AsNot();
        ITry AsTry();
    }

    /// <summary>
    /// The span of a constraint in the source query.
    /// </summary>
    public interface ISpan
    {
        /// <summary>
        /// Gets the offset of the first character.
        /// </summary>
        long Begin { get; }

        /// <summary>
        /// Gets the offset after the last character.
        /// </summary>
        long End { get; }
    }

    /// <summary>
    /// Represents an "isa" constraint: instance isa(!) type
    /// </summary>
    public interface IIsa : IConstraint
    {
        IConstraintVertex Instance { get; }
        IConstraintVertex Type { get; }
        Pinvoke.ConstraintExactness Exactness { get; }
    }

    /// <summary>
    /// Represents a "has" constraint: owner has attribute
    /// </summary>
    public interface IHas : IConstraint
    {
        IConstraintVertex Owner { get; }
        IConstraintVertex Attribute { get; }
        Pinvoke.ConstraintExactness Exactness { get; }
    }

    /// <summary>
    /// Represents a "links" constraint: relation links (role: player)
    /// </summary>
    public interface ILinks : IConstraint
    {
        IConstraintVertex Relation { get; }
        IConstraintVertex Player { get; }
        IConstraintVertex Role { get; }
        Pinvoke.ConstraintExactness Exactness { get; }
    }

    /// <summary>
    /// Represents a "sub" constraint: subtype sub(!) supertype
    /// </summary>
    public interface ISub : IConstraint
    {
        IConstraintVertex Subtype { get; }
        IConstraintVertex Supertype { get; }
        Pinvoke.ConstraintExactness Exactness { get; }
    }

    /// <summary>
    /// Represents an "owns" constraint: owner owns attribute
    /// </summary>
    public interface IOwns : IConstraint
    {
        IConstraintVertex Owner { get; }
        IConstraintVertex Attribute { get; }
        Pinvoke.ConstraintExactness Exactness { get; }
    }

    /// <summary>
    /// Represents a "relates" constraint: relation relates role
    /// </summary>
    public interface IRelates : IConstraint
    {
        IConstraintVertex Relation { get; }
        IConstraintVertex Role { get; }
        Pinvoke.ConstraintExactness Exactness { get; }
    }

    /// <summary>
    /// Represents a "plays" constraint: player plays role
    /// </summary>
    public interface IPlays : IConstraint
    {
        IConstraintVertex Player { get; }
        IConstraintVertex Role { get; }
        Pinvoke.ConstraintExactness Exactness { get; }
    }

    /// <summary>
    /// Represents a function call: let assigned = name(arguments)
    /// </summary>
    public interface IFunctionCall : IConstraint
    {
        string Name { get; }
        IEnumerable<IConstraintVertex> Arguments { get; }
        IEnumerable<IConstraintVertex> Assigned { get; }
    }

    /// <summary>
    /// Represents an expression: let assigned = expression
    /// </summary>
    public interface IExpression : IConstraint
    {
        string Text { get; }
        IEnumerable<IConstraintVertex> Arguments { get; }
        IConstraintVertex Assigned { get; }
    }

    /// <summary>
    /// Represents an "is" constraint: lhs is rhs
    /// </summary>
    public interface IIs : IConstraint
    {
        IConstraintVertex Lhs { get; }
        IConstraintVertex Rhs { get; }
    }

    /// <summary>
    /// Represents an IID constraint: concept iid iid_value
    /// </summary>
    public interface IIid : IConstraint
    {
        IConstraintVertex Variable { get; }
        string Iid { get; }
    }

    /// <summary>
    /// Represents a comparison: lhs comparator rhs
    /// </summary>
    public interface IComparison : IConstraint
    {
        IConstraintVertex Lhs { get; }
        IConstraintVertex Rhs { get; }
        Pinvoke.Comparator Comparator { get; }
        string ComparatorSymbol { get; }
    }

    /// <summary>
    /// Represents a kind constraint: kind type
    /// </summary>
    public interface IKind : IConstraint
    {
        Pinvoke.Kind Kind { get; }
        IConstraintVertex Type { get; }
    }

    /// <summary>
    /// Represents a label constraint: type label label_value
    /// </summary>
    public interface ILabelConstraint : IConstraint
    {
        IConstraintVertex Variable { get; }
        string LabelValue { get; }
    }

    /// <summary>
    /// Represents a value constraint: attribute_type value value_type
    /// </summary>
    public interface IValueConstraint : IConstraint
    {
        IConstraintVertex AttributeType { get; }
        string ValueType { get; }
    }

    /// <summary>
    /// Represents an "or" constraint: { branches[0] } or { branches[1] } [or ...]
    /// </summary>
    public interface IOr : IConstraint
    {
        IEnumerable<IConjunctionID> Branches { get; }
    }

    /// <summary>
    /// Represents a "not" constraint: not { conjunction }
    /// </summary>
    public interface INot : IConstraint
    {
        IConjunctionID Conjunction { get; }
    }

    /// <summary>
    /// Represents a "try" constraint: try { conjunction }
    /// </summary>
    public interface ITry : IConstraint
    {
        IConjunctionID Conjunction { get; }
    }
}
