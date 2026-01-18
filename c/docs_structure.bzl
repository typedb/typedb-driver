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

# Used to map types/functions in the code to a directory & file.
# Keys of dir_mapping are a prefix of the normalised type or function name.
# A name is stripped of underscores, made lowercase and resolved to the longest matching key.
# This key is used as the filename of the file their documentation will be appended to (unless `force_file` overrides it)
# The value is the corresponding directory in which the file must sit.
dir_mapping = {
    # Connection
    "driver": "connection",
    "credential": "connection",
    "driveroptions": "connection",

    # Database
    "database": "connection",
    "databasemanager": "connection",

    # User
    "user": "connection",
    "usermanager": "connection",

    # Transaction
    "transaction": "transaction",
    "transactiontype": "transaction",
    "transactionoptions": "transaction",
    "queryoptions": "transaction",

    # Answer
    "queryanswer": "answer",
    "querytype": "answer",
    "conceptrow": "answer",

    # Concept
    "concept": "concept",
    "datetimeinnanos": "concept",
    "datetimeandtimezone": "concept",
    "stringandoptvalue": "concept",

    # Value types (re-exported from Rust driver)
    "decimal": "value",
    "duration": "value",
    "timezone": "value",
    "kind": "concept",
    "valuetype": "concept",

    # Analyze
    "analyzedquery": "analyze",
    "pipeline": "analyze",
    "pipelinestage": "analyze",
    "conjunction": "analyze",
    "conjunctionid": "analyze",
    "constraint": "analyze",
    "constraintvertex": "analyze",
    "constraintspan": "analyze",
    "constraintwithspan": "analyze",
    "variable": "analyze",
    "variableannotations": "analyze",
    "fetch": "analyze",
    "function": "analyze",
    "returnoperation": "analyze",
    "reducer": "analyze",
    "reduceassignment": "analyze",
    "sortvariable": "analyze",
    "sortorder": "analyze",
    "namedrole": "analyze",
    "comparator": "analyze",

    # Additional types
    "constraintexactness": "analyze",
    "typedbdriver": "connection",

    # Additional function prefixes
    "databases": "connection",
    "users": "connection",
    "entity": "concept",
    "relation": "concept",
    "attribute": "concept",
    "init": "connection",
    "check": "errors",
    "get": "errors",
    "bool": "answer",
    "void": "answer",
    "datetime": "concept",
    "query": "answer",
    "named": "analyze",
    "return": "analyze",
    "sort": "analyze",
    "analyzed": "analyze",
    "string": "answer",
    "reduce": "analyze",

    # Errors
    "error": "errors",
    "checkerror": "errors",
    "getlasterror": "errors",

    # Iterators
    "stringiterator": "answer",
    "conceptiterator": "concept",
    "conceptrowiterator": "answer",
    "functioniterator": "analyze",
    "conjunctioniditerator": "analyze",
    "constraintwithspaniterator": "analyze",
    "constraintvertexiterator": "analyze",
    "pipelinestageiterator": "analyze",
    "sortvariableiterator": "analyze",
    "reduceassignmentiterator": "analyze",
    "reduceriterator": "analyze",
    "variableannotationsiterator": "analyze",
    "variableiterator": "analyze",
    "useriterator": "connection",
    "stringandoptvalueiterator": "concept",

    # Promise types
    "voidpromise": "answer",
    "boolpromise": "answer",
    "stringpromise": "answer",
    "conceptpromise": "answer",
    "analyzedquerypromise": "analyze",
    "queryanswerpromise": "answer",
    "databaseiterator": "connection",

    # Init logging goes to connection
    "initlogging": "connection",
}

# If a function name 'matches', the value here is used as the filename instead.
force_file = {
    "initlogging": "driver",
    "checkerror": "error",
    "getlasterror": "error",
}
