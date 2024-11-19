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

//%dropproxy(Credential, credential)
//%dropproxy(Options, options)

#define typedb_driver_drop driver_close
#define transaction_drop transaction_close
#define database_drop database_close

%dropproxy(TypeDBDriver, typedb_driver)
%dropproxy(Transaction, transaction)

// %dropproxy(DatabaseManager, database_manager);
%dropproxy(Database, database)
%dropproxy(DatabaseIterator, database_iterator)
//%dropproxy(ReplicaInfo, replica_info)
//%dropproxy(ReplicaInfoIterator, replica_info_iterator)

//%dropproxy(UserManager, user_manager);
//%dropproxy(User, user)
//%dropproxy(UserIterator, user_iterator)

%dropproxy(Concept, concept)
%dropproxy(ConceptIterator, concept_iterator)

%dropproxy(ConceptRow, concept_row)
%dropproxy(ConceptRowIterator, concept_row_iterator)

%dropproxydefined(DatetimeAndTimeZone, datetime_and_time_zone)
%dropproxydefined(StringAndOptValue, string_and_opt_value)
%dropproxy(StringAndOptValueIterator, string_and_opt_value_iterator)

%dropproxy(StringIterator, string_iterator)

%dropproxy(QueryAnswer, query_answer)

%dropproxy(ValueGroup, value_group)
%dropproxy(ValueGroupIterator, value_group_iterator)

%define %promiseproxy(Type, function_prefix)
struct Type {};
%newobject function_prefix ## _resolve;
%delobject function_prefix ## _resolve;
%extend Type { ~Type() { function_prefix ## _drop(self); } }
%delobject function_prefix ## _drop;
%enddef

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
#include <unordered_map>
static std::unordered_map<size_t, TransactionCallbackDirector*> transactionOnCloseCallbacks {};
static void transaction_callback_execute(size_t ID, Error* error) {
    try {
        transactionOnCloseCallbacks.at(ID)->callback(error);
        transactionOnCloseCallbacks.erase(ID);
    } catch (std::exception const& e) {
        std::cerr << "[ERROR] " << e.what() << std::endl;
    }
}
%}

%rename(transaction_on_close) transaction_on_close_register;
%ignore transaction_on_close;
%inline %{
#include <atomic>
void transaction_on_close_register(const Transaction* transaction, TransactionCallbackDirector* handler) {
    static std::atomic_size_t nextID;
    std::size_t ID = nextID.fetch_add(1);
    transactionOnCloseCallbacks.insert({ID, handler});
    transaction_on_close(transaction, ID, &transaction_callback_execute);
}
%}

%delobject database_delete;

%delobject transaction_commit;

%typemap(newfree) char* "string_free($1);";
%ignore string_free;

%newobject concept_row_get;
%newobject concept_row_get_column_names;
%newobject concept_row_get_query_type;
%newobject query_answer_get_query_type;
%newobject concept_row_get_concepts;
%newobject concept_row_get_index;
%newobject concept_row_to_string;

%newobject value_group_get_owner;
%newobject value_group_get_value;
%newobject value_group_to_string;

%newobject value_get_string;
%newobject value_get_datetime_tz;

%newobject query_answer_get_rows;

%newobject concept_to_string;

%newobject entity_get_iid;
%newobject relation_get_iid;

%newobject entity_get_type;
%newobject relation_get_type;
%newobject attribute_get_type;

%newobject attribute_get_value;
%newobject attribute_type_get_value_type;
%newobject value_get_value_type;

%newobject entity_type_get_label;
%newobject relation_type_get_label;
%newobject attribute_type_get_label;
%newobject role_type_get_label;

%newobject driver_open_core;
//%newobject driver_open_cloud;
//%newobject driver_open_cloud_translated;

//%newobject credential_new;

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

%newobject get_last_error;
%newobject error_code;
%newobject error_message;

//%newobject options_new;

%newobject concept_iterator_next;
%newobject concept_row_iterator_next;
%newobject database_iterator_next;
%newobject value_group_iterator_next;
%newobject string_iterator_next;
%newobject string_and_opt_value_iterator_next;
//%newobject user_iterator_next;

%newobject transaction_new;
%newobject transaction_query;

//%newobject users_all;
//%newobject users_current_user;
//%newobject users_get;

//%newobject user_get_username;
//%newobject user_manager_new;

%include "typedb_driver.h"
