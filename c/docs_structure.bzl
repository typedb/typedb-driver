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
    # Analyze
    "analyzed": "analyze",
    "analyzedquery": "analyze",
    "analyzedquerypromise": "analyze",
    "comparator": "analyze",
    "conjunction": "analyze",
    "conjunctionid": "analyze",
    "conjunctioniditerator": "analyze",
    "constraint": "analyze",
    "constraintexactness": "analyze",
    "constraintspan": "analyze",
    "constraintvertex": "analyze",
    "constraintvertexiterator": "analyze",
    "constraintwithspan": "analyze",
    "constraintwithspaniterator": "analyze",
    "fetch": "analyze",
    "function": "analyze",
    "functioniterator": "analyze",
    "named": "analyze",
    "namedrole": "analyze",
    "pipeline": "analyze",
    "pipelinestage": "analyze",
    "pipelinestageiterator": "analyze",
    "reduce": "analyze",
    "reduceassignment": "analyze",
    "reduceassignmentiterator": "analyze",
    "reducer": "analyze",
    "reduceriterator": "analyze",
    "return": "analyze",
    "returnoperation": "analyze",
    "sort": "analyze",
    "sortorder": "analyze",
    "sortvariable": "analyze",
    "sortvariableiterator": "analyze",
    "variable": "analyze",
    "variableannotations": "analyze",
    "variableannotationsiterator": "analyze",
    "variableiterator": "analyze",

    # Answer
    "bool": "answer",
    "boolpromise": "answer",
    "conceptrowiterator": "answer",
    "query": "answer",
    "queryanswer": "answer",
    "queryanswerpromise": "answer",
    "querytype": "answer",
    "string": "answer",
    "stringiterator": "answer",
    "stringpromise": "answer",
    "void": "answer",
    "voidpromise": "answer",

    # Concept
    "attribute": "concept",
    "concept": "concept",
    "conceptiterator": "concept",
    "datetime": "concept",
    "datetimeandtimezone": "concept",
    "datetimeinnanos": "concept",
    "entity": "concept",
    "kind": "concept",
    "relation": "concept",
    "stringandoptvalue": "concept",
    "stringandoptvalueiterator": "concept",
    "valuetype": "concept",

    # Connection
    "credential": "connection",
    "database": "connection",
    "databaseiterator": "connection",
    "databasemanager": "connection",
    "databases": "connection",
    "driver": "connection",
    "driveroptions": "connection",
    "init": "connection",
    "initlogging": "connection",
    "replicationrole": "connection",
    "server": "connection",
    "serveriterator": "connection",
    "serverrouting": "connection",
    "serverroutingtag": "connection",
    "serverversion": "connection",
    "typedbdriver": "connection",
    "user": "connection",
    "useriterator": "connection",
    "usermanager": "connection",
    "users": "connection",

    # Errors
    "checkerror": "errors",
    "error": "errors",
    "get": "errors",
    "getlasterror": "errors",
    "check": "errors",

    # Transaction
    "queryoptions": "transaction",
    "transaction": "transaction",
    "transactionoptions": "transaction",
    "transactiontype": "transaction",

    # Value
    "decimal": "value",
    "duration": "value",
    "timezone": "value",
}

# If a function name 'matches', the value here is used as the filename instead.
force_file = {
    "checkerror": "error",
    "getlasterror": "error",
    "initlogging": "driver",
}
