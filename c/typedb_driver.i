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

%module(threads=1) typedb_driver
%module(directors="1") typedb_driver
%{
extern "C" {
#include "typedb_driver.h"
}
%}
%include "stdint.i"
%include "carrays.i"
%include "typemaps.i"

#ifdef SWIGJAVA
%include "swig/typedb_driver_java.swg"
#endif

#ifdef SWIGPYTHON
%include "swig/typedb_driver_python.swg"
#endif

#ifdef SWIGCSHARP
%include "swig/typedb_driver_csharp.swg"
#endif

%nodefaultctor;

%ignore driver_open; // use `driver_open_with_description`

%define %dropproxy(Type, function_prefix)
struct Type {};
%ignore function_prefix ## _drop;
%extend Type { ~Type() { function_prefix ## _drop(self); } }
%enddef

%define %dropproxydefined(Type, function_prefix)
%ignore function_prefix ## _drop;
%extend Type { ~Type() { function_prefix ## _drop(self); } }
%enddef

%dropproxy(Error, error)

%dropproxy(Credentials, credentials)
%dropproxy(DriverOptions, driver_options)
%dropproxy(TransactionOptions, transaction_options)
%dropproxy(QueryOptions, query_options)

#define typedb_driver_drop driver_close
#define transaction_drop transaction_submit_close
#define database_drop database_close

%dropproxy(TypeDBDriver, typedb_driver)
%dropproxy(Transaction, transaction)

%dropproxy(Database, database)
%dropproxy(DatabaseIterator, database_iterator)
//%dropproxy(ReplicaInfo, replica_info)
//%dropproxy(ReplicaInfoIterator, replica_info_iterator)

%dropproxy(User, user)
%dropproxy(UserIterator, user_iterator)

%dropproxy(Concept, concept)
%dropproxy(ConceptIterator, concept_iterator)

%dropproxy(ConceptRow, concept_row)
%dropproxy(ConceptRowIterator, concept_row_iterator)

%dropproxydefined(DatetimeAndTimeZone, datetime_and_time_zone)
%dropproxydefined(StringAndOptValue, string_and_opt_value)
%dropproxy(StringAndOptValueIterator, string_and_opt_value_iterator)

%dropproxy(StringIterator, string_iterator)

%dropproxy(QueryAnswer, query_answer)

%dropproxy(AnalyzedQuery, analyzed_query)
%dropproxy(Conjunction, conjunction)
%dropproxy(ConjunctionID, conjunction_id)
%dropproxy(ConstraintWithSpan, constraint_with_span)
%dropproxy(ConstraintVertex, constraint_vertex)
%dropproxy(Fetch, fetch)
%dropproxy(Function, function)
%dropproxy(Pipeline, pipeline)
%dropproxy(PipelineStage, pipeline_stage)
%dropproxy(ReduceAssignment, reduce_assignment)
%dropproxy(ReturnOperation, return_operation)
%dropproxy(Reducer, reducer)
%dropproxy(SortVariable, sort_variable)
%dropproxy(VariableAnnotations, variable_annotations)
%dropproxy(Variable, variable)

%dropproxy(ConjunctionIDIterator, conjunction_id_iterator)
%dropproxy(ConstraintWithSpanIterator, constraint_with_span_iterator)
%dropproxy(ConstraintVertexIterator, constraint_vertex_iterator)
%dropproxy(FunctionIterator, function_iterator)
%dropproxy(PipelineStageIterator, pipeline_stage_iterator)
%dropproxy(ReduceAssignmentIterator, reduce_assignment_iterator)
%dropproxy(ReducerIterator, reducer_iterator)
%dropproxy(SortVariableIterator, sort_variable_iterator)
%dropproxy(VariableAnnotationsIterator, variable_annotations_iterator)
%dropproxy(VariableIterator, variable_iterator)

%define %promiseproxy(Type, function_prefix)
struct Type {};
%newobject function_prefix ## _resolve;
%delobject function_prefix ## _resolve;
%extend Type { ~Type() { function_prefix ## _drop(self); } }
%delobject function_prefix ## _drop;
%enddef

%promiseproxy(AnalyzedQueryPromise, analyzed_query_promise)
%promiseproxy(BoolPromise, bool_promise)
%promiseproxy(ConceptPromise, concept_promise)
%promiseproxy(StringPromise, string_promise)
%promiseproxy(QueryAnswerPromise, query_answer_promise)
%promiseproxy(VoidPromise, void_promise)

%feature("director") TransactionCallbackDirector;
%inline %{
struct TransactionCallbackDirector {
    TransactionCallbackDirector() {}
    virtual ~TransactionCallbackDirector() {}
    virtual void callback(Error*) = 0;
};
%}

%{
#include <memory>
#include <iostream>
#include <mutex>
#include <unordered_map>

class ThreadSafeTransactionCallbacks {
private:
    // 1. The static map to protect
    static std::unordered_map<size_t, TransactionCallbackDirector*> s_transactionOnCloseCallbacks;

    // 2. The static mutex to manage access
    static std::mutex s_mutex;

public:
    // Delete copy/move constructors and assignment operators
    // to prevent accidental copying of the singleton-like structure
    ThreadSafeTransactionCallbacks(const ThreadSafeTransactionCallbacks&) = delete;
    ThreadSafeTransactionCallbacks& operator=(const ThreadSafeTransactionCallbacks&) = delete;

    // --- Core Operations ---

    /**
     * @brief Inserts a key-value pair into the map in a thread-safe manner.
     */
    static void insert(size_t key, TransactionCallbackDirector* value) {
        // Lock the mutex for the duration of this scope
        std::lock_guard<std::mutex> lock(s_mutex);

        // Thread-safe insertion
        s_transactionOnCloseCallbacks[key] = value;
    }

    /**
     * @brief Retrieves a value associated with a key in a thread-safe manner.
     * @returns The value pointer, or nullptr if the key is not found.
     */
    static TransactionCallbackDirector* find(size_t key) {
        // Lock the mutex for the duration of this scope
        std::lock_guard<std::mutex> lock(s_mutex);

        // Thread-safe lookup
        auto it = s_transactionOnCloseCallbacks.find(key);
        if (it != s_transactionOnCloseCallbacks.end()) {
            return it->second;
        }
        return nullptr; // Return nullptr if not found
    }

    /**
     * @brief Removes a key-value pair from the map in a thread-safe manner.
     */
    static void remove(size_t key) {
        // Lock the mutex for the duration of this scope
        std::lock_guard<std::mutex> lock(s_mutex);

        // Thread-safe removal
        s_transactionOnCloseCallbacks.erase(key);
    }

    // Add other necessary map operations (e.g., size(), contains(), clear()) here...
};

// Initialize the static members
std::unordered_map<size_t, TransactionCallbackDirector*> ThreadSafeTransactionCallbacks::s_transactionOnCloseCallbacks;
std::mutex ThreadSafeTransactionCallbacks::s_mutex;

static void transaction_callback_execute(size_t ID, Error* error) {
    try {
        auto cb = ThreadSafeTransactionCallbacks::find(ID);
        cb->callback(error);
        ThreadSafeTransactionCallbacks::remove(ID);
    } catch (std::exception const& e) {
        std::cerr << "[ERROR] " << e.what() << std::endl;
    }
}
%}

%rename(transaction_on_close) transaction_on_close_register;
%ignore transaction_on_close;
%inline %{
#include <atomic>
VoidPromise* transaction_on_close_register(const Transaction* transaction, TransactionCallbackDirector* handler) {
    static std::atomic_size_t nextID;
    std::size_t ID = nextID.fetch_add(1);
    ThreadSafeTransactionCallbacks::insert(ID, handler);
    return transaction_on_close(transaction, ID, &transaction_callback_execute);
}
%}

%delobject database_delete;

%delobject transaction_commit;

%typemap(newfree) char* "string_free($1);";
%ignore string_free;

%newobject concept_row_get;
%newobject concept_row_get_column_names;
%newobject concept_row_get_query_type;
%newobject concept_row_involved_conjunctions;
%newobject query_answer_get_query_type;
%newobject concept_row_get_concepts;
%newobject concept_row_get_index;
%newobject concept_row_get_query_structure;
%newobject concept_row_to_string;

%newobject value_get_string;
%newobject value_get_datetime_tz;

%newobject query_answer_into_rows;
%newobject query_answer_into_documents;
%delobject query_answer_into_rows;
%delobject query_answer_into_documents;

%newobject concept_to_string;

%newobject entity_get_type;
%newobject relation_get_type;
%newobject attribute_get_type;

%newobject concept_get_label;
%newobject concept_try_get_label;
%newobject concept_try_get_iid;
%newobject concept_try_get_value_type;
%newobject concept_try_get_value;
%newobject credentials_new;

%newobject driver_open_with_description;
%newobject driver_options_new;

%newobject database_get_name;
%newobject database_schema;
%newobject database_type_schema;

//%newobject database_get_preferred_replica_info;
//%newobject database_get_primary_replica_info;
//%newobject database_get_replicas_info;
//
//%newobject replica_info_get_server;
//%newobject replica_info_iterator_next;

%newobject databases_all;
%newobject databases_get;

%newobject analyzed_query_pipeline;
%newobject analyzed_preamble;
%newobject analyzed_fetch;
%newobject conjunction_get_variable_annotations;
%newobject conjunction_get_constraints;
%newobject constraint_span_begin;
%newobject constraint_span_end;
%newobject constraint_variant;
%newobject constraint_isa_get_instance;
%newobject constraint_isa_get_type;
%newobject constraint_has_get_owner;
%newobject constraint_has_get_attribute;
%newobject constraint_links_get_relation;
%newobject constraint_links_get_player;
%newobject constraint_links_get_role;
%newobject constraint_sub_get_subtype;
%newobject constraint_sub_get_supertype;
%newobject constraint_owns_get_owner;
%newobject constraint_owns_get_attribute;
%newobject constraint_relates_get_relation;
%newobject constraint_relates_get_role;
%newobject constraint_plays_get_player;
%newobject constraint_plays_get_role;
%newobject constraint_isa_get_exactness;
%newobject constraint_has_get_exactness;
%newobject constraint_links_get_exactness;
%newobject constraint_sub_get_exactness;
%newobject constraint_owns_get_exactness;
%newobject constraint_relates_get_exactness;
%newobject constraint_plays_get_exactness;
%newobject constraint_function_call_get_name;
%newobject constraint_function_call_get_assigned;
%newobject constraint_function_call_get_arguments;
%newobject constraint_expression_get_text;
%newobject constraint_expression_get_assigned;
%newobject constraint_expression_get_arguments;
%newobject constraint_is_get_lhs;
%newobject constraint_is_get_rhs;
%newobject constraint_iid_get_variable;
%newobject constraint_iid_get_iid;
%newobject constraint_comparison_get_lhs;
%newobject constraint_comparison_get_rhs;
%newobject constraint_comparison_get_comparator;
%newobject constraint_kind_get_kind;
%newobject constraint_kind_get_type;
%newobject constraint_label_get_variable;
%newobject constraint_label_get_label;
%newobject constraint_value_get_attribute_type;
%newobject constraint_value_get_value_type;
%newobject constraint_or_get_branches;
%newobject constraint_not_get_conjunction;
%newobject constraint_try_get_conjunction;
%newobject constraint_vertex_variant;
%newobject constraint_vertex_as_variable;
%newobject constraint_vertex_as_label;
%newobject constraint_vertex_as_value;
%newobject constraint_vertex_as_named_role_get_variable;
%newobject constraint_vertex_as_named_role_get_name;
%newobject constraint_with_span_iterator_next;
%newobject constraint_with_span_iterator_drop;
%newobject constraint_vertex_iterator_next;
%newobject constraint_vertex_iterator_drop;
%newobject constraint_with_span_drop;
%newobject constraint_vertex_drop;
%newobject function_argument_variables;
%newobject function_argument_annotations;
%newobject function_body;
%newobject function_return_operation;
%newobject function_return_annotations;
%newobject fetch_leaf_annotations;
%newobject fetch_list_element;
%newobject fetch_object_fields;
%newobject fetch_object_get_field;

%newobject pipeline_get_conjunction;
%newobject pipeline_stages;
%newobject pipeline_stage_get_block;
%newobject pipeline_stage_delete_get_deleted_variables;
%newobject pipeline_stage_reduce_get_reducer_assignments;
%newobject pipeline_stage_sort_get_sort_variables;
%newobject pipeline_stage_reduce_get_groupby;
%newobject sort_variable_get_variable;
%newobject reduce_assignment_get_assigned;
%newobject reduce_assignment_get_reducer;
%newobject reducer_get_name;
%newobject reducer_get_arguments;
%newobject variable_get_name;

%newobject function_iterator_next;
%newobject conjunction_id_iterator_next;
%newobject constraint_with_span_iterator_next;
%newobject constraint_vertex_iterator_next;
%newobject pipeline_stage_iterator_next;
%newobject reduce_assignment_iterator_next;
%newobject reducer_iterator_next;
%newobject sort_variable_iterator_next;
%newobject variable_annotations_iterator_next;
%newobject variable_iterator_next;


%newobject get_last_error;
%newobject error_code;
%newobject error_message;

%newobject transaction_options_new;
%newobject query_options_new;

%newobject concept_iterator_next;
%newobject concept_row_iterator_next;
%newobject database_iterator_next;
%newobject string_iterator_next;
%newobject string_and_opt_value_iterator_next;
%newobject user_iterator_next;
%newobject variable_iterator_next;

%newobject transaction_new;
%newobject transaction_query;
%newobject transaction_analyze;

%newobject users_all;
%newobject users_get_current_user;
%newobject users_get;

%newobject user_get_name;
%delobject user_delete;

%include "typedb_driver.h"
