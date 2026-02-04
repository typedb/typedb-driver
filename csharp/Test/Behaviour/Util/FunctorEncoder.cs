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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

using Pinvoke = TypeDB.Driver.Pinvoke;

namespace TypeDB.Driver.Test.Behaviour
{
    /// <summary>
    /// Encodes analyzed query structures into a functor-based string representation for comparison in tests.
    /// </summary>
    public abstract class FunctorEncoder
    {
        protected readonly Pinvoke.Pipeline Pipeline;

        protected FunctorEncoder(Pinvoke.Pipeline pipeline)
        {
            Pipeline = pipeline;
        }

        private static string EncodeList(IEnumerable<string> elements)
        {
            return $"[{string.Join(",", elements)}]";
        }

        private static string MakeFunctor(string functor, params string[] encodedArgs)
        {
            return $"{functor}({string.Join(",", encodedArgs)})";
        }

        public static string NormalizeForCompare(string functor)
        {
            return Regex.Replace(functor.ToLower(), @"\s+", "");
        }

        // Helper to iterate over native iterators
        protected static IEnumerable<T> ToEnumerable<T>(IEnumerator<T> enumerator)
        {
            while (enumerator.MoveNext())
            {
                yield return enumerator.Current;
            }
        }

        // The common variable encoding
        public string Encode(Pinvoke.Variable variable)
        {
            var name = Pinvoke.typedb_driver.variable_get_name(Pipeline, variable);
            return "$" + (name ?? "_");
        }

        // Helper to get a value as its string representation
        protected static string GetValueAsString(Pinvoke.Concept value)
        {
            if (Pinvoke.typedb_driver.concept_is_boolean(value))
            {
                return Pinvoke.typedb_driver.concept_get_boolean(value).ToString().ToLower();
            }
            else if (Pinvoke.typedb_driver.concept_is_integer(value))
            {
                return Pinvoke.typedb_driver.concept_get_integer(value).ToString();
            }
            else if (Pinvoke.typedb_driver.concept_is_double(value))
            {
                return Pinvoke.typedb_driver.concept_get_double(value).ToString();
            }
            else if (Pinvoke.typedb_driver.concept_is_string(value))
            {
                return Pinvoke.typedb_driver.concept_get_string(value);
            }
            // For other types (date, datetime, decimal, duration, struct), use to_string representation
            return Pinvoke.typedb_driver.concept_to_string(value);
        }

        /// <summary>
        /// Encodes the structure of analyzed queries.
        /// </summary>
        public class StructureEncoder : FunctorEncoder
        {
            public StructureEncoder(Pinvoke.Pipeline pipeline) : base(pipeline) { }

            private string ConstraintFunctor(string variant, Pinvoke.ConstraintExactness exactness, params Pinvoke.ConstraintVertex[] args)
            {
                string variantWithExactness = exactness == Pinvoke.ConstraintExactness.Exact ? variant + "Exact" : variant;
                return MakeFunctor(variantWithExactness, args.Select(Encode).ToArray());
            }

            private string Encode(Pinvoke.PipelineStage stage)
            {
                var variant = Pinvoke.typedb_driver.pipeline_stage_variant(stage);
                switch (variant)
                {
                    case Pinvoke.PipelineStageVariant.Match:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Match", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Insert:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Insert", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Put:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Put", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Update:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Update", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Delete:
                        {
                            var deletedVars = Pinvoke.typedb_driver.pipeline_stage_delete_get_deleted_variables(stage);
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Delete",
                                EncodeList(ToEnumerable(deletedVars).Select(Encode)),
                                Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Select:
                        {
                            var vars = Pinvoke.typedb_driver.pipeline_stage_select_get_variables(stage);
                            return MakeFunctor("Select", EncodeList(ToEnumerable(vars).Select(Encode)));
                        }
                    case Pinvoke.PipelineStageVariant.Sort:
                        {
                            var sortVars = Pinvoke.typedb_driver.pipeline_stage_sort_get_sort_variables(stage);
                            return MakeFunctor("Sort", EncodeList(ToEnumerable(sortVars).Select(Encode)));
                        }
                    case Pinvoke.PipelineStageVariant.Require:
                        {
                            var vars = Pinvoke.typedb_driver.pipeline_stage_require_get_variables(stage);
                            return MakeFunctor("Require", EncodeList(ToEnumerable(vars).Select(Encode)));
                        }
                    case Pinvoke.PipelineStageVariant.Offset:
                        return MakeFunctor("Offset", Pinvoke.typedb_driver.pipeline_stage_offset_get_offset(stage).ToString());
                    case Pinvoke.PipelineStageVariant.Limit:
                        return MakeFunctor("Limit", Pinvoke.typedb_driver.pipeline_stage_limit_get_limit(stage).ToString());
                    case Pinvoke.PipelineStageVariant.Distinct:
                        return MakeFunctor("Distinct");
                    case Pinvoke.PipelineStageVariant.Reduce:
                        {
                            var assignments = Pinvoke.typedb_driver.pipeline_stage_reduce_get_reducer_assignments(stage);
                            var groupBy = Pinvoke.typedb_driver.pipeline_stage_reduce_get_groupby(stage);
                            return MakeFunctor("Reduce",
                                EncodeList(ToEnumerable(assignments).Select(Encode)),
                                EncodeList(ToEnumerable(groupBy).Select(Encode)));
                        }
                    default:
                        throw new ArgumentException($"Unhandled stage variant: {variant}");
                }
            }

            private string Encode(Pinvoke.ReduceAssignment reduceAssignment)
            {
                var assigned = Pinvoke.typedb_driver.reduce_assignment_get_assigned(reduceAssignment);
                var reducer = Pinvoke.typedb_driver.reduce_assignment_get_reducer(reduceAssignment);
                return MakeFunctor("ReduceAssign", Encode(assigned), Encode(reducer));
            }

            private string Encode(Pinvoke.Reducer reducer)
            {
                var name = Pinvoke.typedb_driver.reducer_get_name(reducer);
                var args = Pinvoke.typedb_driver.reducer_get_arguments(reducer);
                return MakeFunctor("Reducer", name, EncodeList(ToEnumerable(args).Select(Encode)));
            }

            private string Encode(Pinvoke.SortVariable sortVariable)
            {
                var variable = Pinvoke.typedb_driver.sort_variable_get_variable(sortVariable);
                var order = Pinvoke.typedb_driver.sort_variable_get_order(sortVariable);
                string orderStr = order == Pinvoke.SortOrder.Ascending ? "Asc" : "Desc";
                return MakeFunctor(orderStr, Encode(variable));
            }

            public string Encode(Pinvoke.ConstraintVertex vertex)
            {
                var variant = Pinvoke.typedb_driver.constraint_vertex_variant(vertex);
                switch (variant)
                {
                    case Pinvoke.ConstraintVertexVariant.VariableVertex:
                        var variable = Pinvoke.typedb_driver.constraint_vertex_as_variable(vertex);
                        return Encode(variable);
                    case Pinvoke.ConstraintVertexVariant.LabelVertex:
                        var label = Pinvoke.typedb_driver.constraint_vertex_as_label(vertex);
                        return Pinvoke.typedb_driver.concept_get_label(label);
                    case Pinvoke.ConstraintVertexVariant.ValueVertex:
                        var value = Pinvoke.typedb_driver.constraint_vertex_as_value(vertex);
                        if (Pinvoke.typedb_driver.concept_is_string(value))
                        {
                            return "\"" + Pinvoke.typedb_driver.concept_get_string(value) + "\"";
                        }
                        return GetValueAsString(value);
                    case Pinvoke.ConstraintVertexVariant.NamedRoleVertex:
                        var namedRole = Pinvoke.typedb_driver.constraint_vertex_as_named_role(vertex);
                        return Pinvoke.typedb_driver.named_role_get_name(namedRole);
                    default:
                        throw new ArgumentException($"Unexpected ConstraintVertex variant: {variant}");
                }
            }

            public string Encode(Pinvoke.ConstraintWithSpan constraint)
            {
                var variant = Pinvoke.typedb_driver.constraint_variant(constraint);
                switch (variant)
                {
                    case Pinvoke.ConstraintVariant.Isa:
                        {
                            var instance = Pinvoke.typedb_driver.constraint_isa_get_instance(constraint);
                            var type = Pinvoke.typedb_driver.constraint_isa_get_type(constraint);
                            var exactness = Pinvoke.typedb_driver.constraint_isa_get_exactness(constraint);
                            return ConstraintFunctor("Isa", exactness, instance, type);
                        }
                    case Pinvoke.ConstraintVariant.Has:
                        {
                            var owner = Pinvoke.typedb_driver.constraint_has_get_owner(constraint);
                            var attr = Pinvoke.typedb_driver.constraint_has_get_attribute(constraint);
                            var exactness = Pinvoke.typedb_driver.constraint_has_get_exactness(constraint);
                            return ConstraintFunctor("Has", exactness, owner, attr);
                        }
                    case Pinvoke.ConstraintVariant.Links:
                        {
                            var relation = Pinvoke.typedb_driver.constraint_links_get_relation(constraint);
                            var player = Pinvoke.typedb_driver.constraint_links_get_player(constraint);
                            var role = Pinvoke.typedb_driver.constraint_links_get_role(constraint);
                            var exactness = Pinvoke.typedb_driver.constraint_links_get_exactness(constraint);
                            return ConstraintFunctor("Links", exactness, relation, player, role);
                        }
                    case Pinvoke.ConstraintVariant.Sub:
                        {
                            var subtype = Pinvoke.typedb_driver.constraint_sub_get_subtype(constraint);
                            var supertype = Pinvoke.typedb_driver.constraint_sub_get_supertype(constraint);
                            var exactness = Pinvoke.typedb_driver.constraint_sub_get_exactness(constraint);
                            return ConstraintFunctor("Sub", exactness, subtype, supertype);
                        }
                    case Pinvoke.ConstraintVariant.Owns:
                        {
                            var owner = Pinvoke.typedb_driver.constraint_owns_get_owner(constraint);
                            var attr = Pinvoke.typedb_driver.constraint_owns_get_attribute(constraint);
                            var exactness = Pinvoke.typedb_driver.constraint_owns_get_exactness(constraint);
                            return ConstraintFunctor("Owns", exactness, owner, attr);
                        }
                    case Pinvoke.ConstraintVariant.Relates:
                        {
                            var relation = Pinvoke.typedb_driver.constraint_relates_get_relation(constraint);
                            var role = Pinvoke.typedb_driver.constraint_relates_get_role(constraint);
                            var exactness = Pinvoke.typedb_driver.constraint_relates_get_exactness(constraint);
                            return ConstraintFunctor("Relates", exactness, relation, role);
                        }
                    case Pinvoke.ConstraintVariant.Plays:
                        {
                            var player = Pinvoke.typedb_driver.constraint_plays_get_player(constraint);
                            var role = Pinvoke.typedb_driver.constraint_plays_get_role(constraint);
                            var exactness = Pinvoke.typedb_driver.constraint_plays_get_exactness(constraint);
                            return ConstraintFunctor("Plays", exactness, player, role);
                        }
                    case Pinvoke.ConstraintVariant.FunctionCall:
                        {
                            var name = Pinvoke.typedb_driver.constraint_function_call_get_name(constraint);
                            var assigned = Pinvoke.typedb_driver.constraint_function_call_get_assigned(constraint);
                            var args = Pinvoke.typedb_driver.constraint_function_call_get_arguments(constraint);
                            return MakeFunctor("FunctionCall", name,
                                EncodeList(ToEnumerable(assigned).Select(Encode)),
                                EncodeList(ToEnumerable(args).Select(Encode)));
                        }
                    case Pinvoke.ConstraintVariant.Expression:
                        {
                            var text = Pinvoke.typedb_driver.constraint_expression_get_text(constraint);
                            var assigned = Pinvoke.typedb_driver.constraint_expression_get_assigned(constraint);
                            var args = Pinvoke.typedb_driver.constraint_expression_get_arguments(constraint);
                            return MakeFunctor("Expression", text, Encode(assigned),
                                EncodeList(ToEnumerable(args).Select(Encode)));
                        }
                    case Pinvoke.ConstraintVariant.Is:
                        {
                            var lhs = Pinvoke.typedb_driver.constraint_is_get_lhs(constraint);
                            var rhs = Pinvoke.typedb_driver.constraint_is_get_rhs(constraint);
                            return MakeFunctor("Is", Encode(lhs), Encode(rhs));
                        }
                    case Pinvoke.ConstraintVariant.Iid:
                        {
                            var variable = Pinvoke.typedb_driver.constraint_iid_get_variable(constraint);
                            var iid = Pinvoke.typedb_driver.constraint_iid_get_iid(constraint);
                            return MakeFunctor("Iid", Encode(variable), iid);
                        }
                    case Pinvoke.ConstraintVariant.Comparison:
                        {
                            var lhs = Pinvoke.typedb_driver.constraint_comparison_get_lhs(constraint);
                            var rhs = Pinvoke.typedb_driver.constraint_comparison_get_rhs(constraint);
                            var comparator = Pinvoke.typedb_driver.constraint_comparison_get_comparator(constraint);
                            var symbol = Pinvoke.typedb_driver.comparator_get_name(comparator);
                            return MakeFunctor("Comparison", Encode(lhs), Encode(rhs), symbol);
                        }
                    case Pinvoke.ConstraintVariant.KindOf:
                        {
                            var kind = Pinvoke.typedb_driver.constraint_kind_get_kind(constraint);
                            var type = Pinvoke.typedb_driver.constraint_kind_get_type(constraint);
                            return MakeFunctor("Kind", kind.ToString(), Encode(type));
                        }
                    case Pinvoke.ConstraintVariant.Label:
                        {
                            var variable = Pinvoke.typedb_driver.constraint_label_get_variable(constraint);
                            var label = Pinvoke.typedb_driver.constraint_label_get_label(constraint);
                            return MakeFunctor("Label", Encode(variable), label);
                        }
                    case Pinvoke.ConstraintVariant.Value:
                        {
                            var attrType = Pinvoke.typedb_driver.constraint_value_get_attribute_type(constraint);
                            var valueType = Pinvoke.typedb_driver.constraint_value_get_value_type(constraint);
                            return MakeFunctor("Value", Encode(attrType), valueType);
                        }
                    case Pinvoke.ConstraintVariant.Or:
                        {
                            var branches = Pinvoke.typedb_driver.constraint_or_get_branches(constraint);
                            var branchesEncoded = ToEnumerable(branches).Select(conjunctionId =>
                            {
                                var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, conjunctionId);
                                return Encode(conjunction);
                            });
                            return MakeFunctor("Or", EncodeList(branchesEncoded));
                        }
                    case Pinvoke.ConstraintVariant.Not:
                        {
                            var conjId = Pinvoke.typedb_driver.constraint_not_get_conjunction(constraint);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, conjId);
                            return MakeFunctor("Not", Encode(conjunction));
                        }
                    case Pinvoke.ConstraintVariant.Try:
                        {
                            var conjId = Pinvoke.typedb_driver.constraint_try_get_conjunction(constraint);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, conjId);
                            return MakeFunctor("Try", Encode(conjunction));
                        }
                    default:
                        throw new ArgumentException($"Unhandled constraint variant: {variant}");
                }
            }

            private string Encode(Pinvoke.Conjunction conjunction)
            {
                var constraints = Pinvoke.typedb_driver.conjunction_get_constraints(conjunction);
                return EncodeList(ToEnumerable(constraints).Select(Encode));
            }

            public string Encode(Pinvoke.Pipeline pipeline)
            {
                var stages = Pinvoke.typedb_driver.pipeline_stages(pipeline);
                return MakeFunctor("Pipeline", EncodeList(ToEnumerable(stages).Select(Encode)));
            }

            public string Encode(Pinvoke.Function func)
            {
                var argVars = Pinvoke.typedb_driver.function_argument_variables(func);
                var returnOp = Pinvoke.typedb_driver.function_return_operation(func);
                var body = Pinvoke.typedb_driver.function_body(func);
                return MakeFunctor("Function",
                    EncodeList(ToEnumerable(argVars).Select(Encode)),
                    Encode(returnOp),
                    Encode(body));
            }

            private string Encode(Pinvoke.ReturnOperation returnOp)
            {
                var variant = Pinvoke.typedb_driver.return_operation_variant(returnOp);
                switch (variant)
                {
                    case Pinvoke.ReturnOperationVariant.StreamReturn:
                        {
                            var vars = Pinvoke.typedb_driver.return_operation_stream_variables(returnOp);
                            return MakeFunctor("Stream", EncodeList(ToEnumerable(vars).Select(Encode)));
                        }
                    case Pinvoke.ReturnOperationVariant.SingleReturn:
                        {
                            var selector = Pinvoke.typedb_driver.return_operation_single_selector(returnOp);
                            var vars = Pinvoke.typedb_driver.return_operation_single_variables(returnOp);
                            return MakeFunctor("Single", selector, EncodeList(ToEnumerable(vars).Select(Encode)));
                        }
                    case Pinvoke.ReturnOperationVariant.CheckReturn:
                        return MakeFunctor("Check");
                    case Pinvoke.ReturnOperationVariant.ReduceReturn:
                        {
                            var reducers = Pinvoke.typedb_driver.return_operation_reducers(returnOp);
                            return MakeFunctor("Reduce", EncodeList(ToEnumerable(reducers).Select(Encode)));
                        }
                    default:
                        throw new ArgumentException($"Unhandled return operation variant: {variant}");
                }
            }
        }

        /// <summary>
        /// Encodes the annotations of analyzed queries.
        /// </summary>
        public class AnnotationsEncoder : FunctorEncoder
        {
            public AnnotationsEncoder(Pinvoke.Pipeline pipeline) : base(pipeline) { }

            public string Encode(Pinvoke.Pipeline pipeline)
            {
                var stages = Pinvoke.typedb_driver.pipeline_stages(pipeline);
                return MakeFunctor("Pipeline", EncodeList(ToEnumerable(stages).Select(Encode)));
            }

            public string Encode(Pinvoke.PipelineStage stage)
            {
                var variant = Pinvoke.typedb_driver.pipeline_stage_variant(stage);
                switch (variant)
                {
                    case Pinvoke.PipelineStageVariant.Match:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Match", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Insert:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Insert", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Put:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Put", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Update:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Update", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Delete:
                        {
                            var block = Pinvoke.typedb_driver.pipeline_stage_get_block(stage);
                            var conjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, block);
                            return MakeFunctor("Delete", Encode(conjunction));
                        }
                    case Pinvoke.PipelineStageVariant.Select:
                        return MakeFunctor("Select");
                    case Pinvoke.PipelineStageVariant.Sort:
                        return MakeFunctor("Sort");
                    case Pinvoke.PipelineStageVariant.Require:
                        return MakeFunctor("Require");
                    case Pinvoke.PipelineStageVariant.Offset:
                        return MakeFunctor("Offset");
                    case Pinvoke.PipelineStageVariant.Limit:
                        return MakeFunctor("Limit");
                    case Pinvoke.PipelineStageVariant.Distinct:
                        return MakeFunctor("Distinct");
                    case Pinvoke.PipelineStageVariant.Reduce:
                        return MakeFunctor("Reduce");
                    default:
                        throw new ArgumentException($"Unhandled stage variant: {variant}");
                }
            }

            private string Encode(Pinvoke.VariableAnnotations variableAnnotations)
            {
                var variant = Pinvoke.typedb_driver.variable_annotations_variant(variableAnnotations);
                switch (variant)
                {
                    case Pinvoke.VariableAnnotationsVariant.InstanceAnnotations:
                        {
                            var types = Pinvoke.typedb_driver.variable_annotations_instance(variableAnnotations);
                            return MakeFunctor("Instance", EncodeList(ToEnumerable(types).Select(c => Pinvoke.typedb_driver.concept_get_label(c))));
                        }
                    case Pinvoke.VariableAnnotationsVariant.TypeAnnotations:
                        {
                            var types = Pinvoke.typedb_driver.variable_annotations_type(variableAnnotations);
                            return MakeFunctor("Type", EncodeList(ToEnumerable(types).Select(c => Pinvoke.typedb_driver.concept_get_label(c))));
                        }
                    case Pinvoke.VariableAnnotationsVariant.ValueAnnotations:
                        {
                            var valueTypes = Pinvoke.typedb_driver.variable_annotations_value(variableAnnotations);
                            return MakeFunctor("Value", EncodeList(ToEnumerable(valueTypes)));
                        }
                    default:
                        throw new ArgumentException($"Unhandled variable annotations variant: {variant}");
                }
            }

            private string Encode(Pinvoke.Conjunction conjunction)
            {
                var annotatedVars = Pinvoke.typedb_driver.conjunction_get_annotated_variables(conjunction);
                var trunkAnnotations = ToEnumerable(annotatedVars)
                    .Select(v =>
                    {
                        var annotations = Pinvoke.typedb_driver.conjunction_get_variable_annotations(conjunction, v);
                        return base.Encode(v) + ":" + Encode(annotations);
                    })
                    .OrderBy(x => x);
                string trunk = "{" + string.Join(",", trunkAnnotations) + "}";

                var constraints = Pinvoke.typedb_driver.conjunction_get_constraints(conjunction);
                var branches = ToEnumerable(constraints)
                    .Select(constraint =>
                    {
                        var cVariant = Pinvoke.typedb_driver.constraint_variant(constraint);
                        switch (cVariant)
                        {
                            case Pinvoke.ConstraintVariant.Or:
                                {
                                    var orBranches = Pinvoke.typedb_driver.constraint_or_get_branches(constraint);
                                    return MakeFunctor("Or", EncodeList(ToEnumerable(orBranches).Select(b =>
                                    {
                                        var conj = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, b);
                                        return Encode(conj);
                                    })));
                                }
                            case Pinvoke.ConstraintVariant.Not:
                                {
                                    var notConj = Pinvoke.typedb_driver.constraint_not_get_conjunction(constraint);
                                    var conj = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, notConj);
                                    return MakeFunctor("Not", Encode(conj));
                                }
                            case Pinvoke.ConstraintVariant.Try:
                                {
                                    var tryConj = Pinvoke.typedb_driver.constraint_try_get_conjunction(constraint);
                                    var conj = Pinvoke.typedb_driver.pipeline_get_conjunction(Pipeline, tryConj);
                                    return MakeFunctor("Try", Encode(conj));
                                }
                            default:
                                return null;
                        }
                    })
                    .Where(x => x != null)
                    .Select(x => x!);

                return MakeFunctor("And", trunk, EncodeList(branches));
            }

            public string Encode(Pinvoke.Function func)
            {
                var returnOp = Pinvoke.typedb_driver.function_return_operation(func);
                var variant = Pinvoke.typedb_driver.return_operation_variant(returnOp);
                string returnStreamOrSingle = variant == Pinvoke.ReturnOperationVariant.StreamReturn ? "Stream" : "Single";

                var argAnnotations = Pinvoke.typedb_driver.function_argument_annotations(func);
                var returnAnnotations = Pinvoke.typedb_driver.function_return_annotations(func);
                var body = Pinvoke.typedb_driver.function_body(func);

                return MakeFunctor("Function",
                    EncodeList(ToEnumerable(argAnnotations).Select(Encode)),
                    MakeFunctor(returnStreamOrSingle, EncodeList(ToEnumerable(returnAnnotations).Select(Encode))),
                    Encode(body));
            }

            public string Encode(Pinvoke.Fetch fetch)
            {
                var variant = Pinvoke.typedb_driver.fetch_variant(fetch);
                switch (variant)
                {
                    case Pinvoke.FetchVariant.LeafDocument:
                        {
                            var annotations = Pinvoke.typedb_driver.fetch_leaf_annotations(fetch);
                            return EncodeList(ToEnumerable(annotations));
                        }
                    case Pinvoke.FetchVariant.ListDocument:
                        {
                            var element = Pinvoke.typedb_driver.fetch_list_element(fetch);
                            return MakeFunctor("List", Encode(element));
                        }
                    case Pinvoke.FetchVariant.ObjectDocument:
                        {
                            var keys = Pinvoke.typedb_driver.fetch_object_fields(fetch);
                            var fields = ToEnumerable(keys).Select(field =>
                            {
                                var value = Pinvoke.typedb_driver.fetch_object_get_field(fetch, field);
                                return field + ":" + Encode(value);
                            }).OrderBy(x => x);
                            return "{" + string.Join(",", fields) + "}";
                        }
                    default:
                        throw new ArgumentException($"Unhandled Fetch variant: {variant}");
                }
            }
        }
    }
}
