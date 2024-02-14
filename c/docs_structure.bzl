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

# Keys are a prefix of the type name or function name. A type/function is resolved to the longest matching key.
# Unless overridden in `force_file`, These are stripped of underscores, made lowercase and used as the filename.
# The value is the corresponding directory in which the file must sit.
# The exception is that all types are automatically put in 'types.adoc' in the specified directory.
dir_mapping = {

    "connection": "connection",
    "credential" : "connection",
    "replica": "connection",
    "user": "connection",
    "database": "connection",
    "init_logging": "connection",

    "session" : "session",
    "options": "session",

    "transaction": "transaction",
    "query": "transaction",

    "SchemaException" : "errors",
    "schema_exception" : "errors",
    "check_error" : "errors",
    "get_last_error" : "errors",
    "error" : "errors",

    "Annotation" : "schema",
    "Transitivity": "schema",
    "ValueType": "schema",
    "attribute_type" : "schema",
    "entity_type" : "schema",
    "relation_type" : "schema",
    "role_type" : "schema",


    "rule" : "logic",
    "logic" : "logic",

    "ConceptMap": "answer",
    "BoolPromise" : "answer",
    "VoidPromise" : "answer",
    "StringPair" : "answer",
    "StringPairIterator" : "answer",
    "StringIterator" : "answer",
    "StringPromise" : "answer",
    "string_" : "answer",
    "bool_promise" : "answer",
    "void_promise" : "answer",


    "OwnerAttributePair" : "answer",
    "ValueGroup" : "answer",
    "concept_map": "answer",
    "explain": "answer",
    "explanation": "answer",

    "concept" : "concept",
    "attribute" : "concept",
    "entity" : "concept",
    "relation" : "concept",
    "role_player" : "concept",
    "RolePlayer" : "concept",
    "thing" : "concept",
    "value" : "concept",
}

force_file = {
    "check_error" : "error",

}