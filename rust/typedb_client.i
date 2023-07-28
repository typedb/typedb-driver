/*
 * Copyright (C) 2022 Vaticle
 *
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

%module(directors="1") typedb_client
%{
extern "C" {
#include "typedb_client.h"
}
%}
%include "stdint.i"
%include "carrays.i"
%include "typemaps.i"

#ifdef SWIGJAVA
%include "swig/typedb_client_java.swg"
#endif

#ifdef SWIGPYTHON
%include "swig/typedb_client_python.swg"
#endif

%nodefaultctor;

%define %dropproxy(Type, function_prefix)
struct Type {};
%ignore function_prefix ## _drop;
%extend Type { ~Type() { function_prefix ## _drop(self); } }
%enddef

%dropproxy(Error, error)
%dropproxy(SchemaException, schema_exception)
%dropproxy(SchemaExceptionIterator, schema_exception_iterator)

%dropproxy(Credential, credential)
%dropproxy(Options, options)

#define connection_drop connection_close

%dropproxy(Connection, connection)
%dropproxy(Session, session)
%dropproxy(Transaction, transaction)

%dropproxy(DatabaseManager, database_manager);
%dropproxy(Database, database)
%dropproxy(DatabaseIterator, database_iterator)
%dropproxy(ReplicaInfo, replica_info)
%dropproxy(ReplicaInfoIterator, replica_info_iterator)

%dropproxy(UserManager, user_manager);
%dropproxy(User, user)
%dropproxy(UserIterator, user_iterator)

%dropproxy(Concept, concept)
%dropproxy(ConceptIterator, concept_iterator)

%dropproxy(Annotation, annotation)

%dropproxy(RolePlayer, role_player)
%dropproxy(RolePlayerIterator, role_player_iterator)

%dropproxy(ConceptMap, concept_map)
%dropproxy(ConceptMapIterator, concept_map_iterator)
%dropproxy(Explainables, explainables)
%dropproxy(Explainable, explainable)

%dropproxy(ConceptMapGroup, concept_map_group)
%dropproxy(ConceptMapGroupIterator, concept_map_group_iterator)

%dropproxy(StringIterator, string_iterator)
%dropproxy(StringPairIterator, string_pair_iterator)

%dropproxy(Numeric, numeric)

%dropproxy(NumericGroup, numeric_group)
%dropproxy(NumericGroupIterator, numeric_group_iterator)

%dropproxy(Explanation, explanation)
%dropproxy(ExplanationIterator, explanation_iterator)

%dropproxy(Rule, rule)
%dropproxy(RuleIterator, rule_iterator)

%feature("director") SessionCallbackDirector;
%inline %{
struct SessionCallbackDirector {
    SessionCallbackDirector() {}
    virtual ~SessionCallbackDirector() {}
    virtual void callback() = 0;
};
%}

%{
#include <memory>
#include <unordered_map>
static std::unordered_map<size_t, SessionCallbackDirector*> sessionOnCloseCallbacks {};
static void session_callback_execute(size_t ID) {
    sessionOnCloseCallbacks.at(ID)->callback();
    sessionOnCloseCallbacks.erase(ID);
}
%}

%rename(session_on_close) session_on_close_register;
%ignore session_on_close;
%inline %{
#include <atomic>
void session_on_close_register(const Session* session, SessionCallbackDirector* handler) {
    static std::atomic_size_t nextID;
    std::size_t ID = nextID.fetch_add(1);
    sessionOnCloseCallbacks.insert({ID, handler});
    session_on_close(session, ID, &session_callback_execute);
}
%}

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
#include <unordered_map>
static std::unordered_map<size_t, TransactionCallbackDirector*> transactionOnCloseCallbacks {};
static void transaction_callback_execute(size_t ID, Error* error) {
    transactionOnCloseCallbacks.at(ID)->callback(error);
    transactionOnCloseCallbacks.erase(ID);
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

%newobject concept_map_get_variables;
%newobject concept_map_get_values;
%newobject concept_map_get;
%newobject concept_map_get_explainables;
%newobject concept_map_to_string;

%newobject explainables_get_relation;
%newobject explainables_get_attribute;
%newobject explainables_get_ownership;
%newobject explainables_get_relations_keys;
%newobject explainables_get_attributes_keys;
%newobject explainables_get_ownerships_keys;
%newobject explainables_to_string;
%newobject explanation_to_string;

%newobject explainable_get_conjunction;

%newobject explanation_get_rule;
%newobject explanation_get_conclusion;
%newobject explanation_get_condition;

%newobject concept_map_group_get_owner;
%newobject concept_map_group_get_concept_maps;

%newobject numeric_group_get_owner;
%newobject numeric_group_get_numeric;

%newobject string_iterator_next;

%newobject string_pair_iterator_next;

%newobject value_new_boolean;
%newobject value_new_long;
%newobject value_new_double;
%newobject value_new_string;
%newobject value_new_date_time_from_millis;
%newobject value_get_string;

%newobject annotation_new_key;
%newobject annotation_new_unique;
%newobject annotation_to_string;

%newobject concept_to_string;

%newobject concepts_get_entity_type;
%newobject concepts_get_relation_type;
%newobject concepts_get_attribute_type;
%newobject concepts_put_entity_type;
%newobject concepts_put_relation_type;
%newobject concepts_put_attribute_type;
%newobject concepts_get_entity;
%newobject concepts_get_relation;
%newobject concepts_get_attribute;
%newobject concepts_get_schema_exceptions;

%newobject concept_iterator_next;

%newobject role_player_iterator_next;

%newobject role_player_get_role_type;
%newobject role_player_get_player;

%newobject thing_get_iid;

%newobject entity_get_type;
%newobject relation_get_type;
%newobject attribute_get_type;

%newobject thing_get_has;
%newobject thing_get_relations;
%newobject thing_get_playing;

%newobject relation_get_players_by_role_type;
%newobject relation_get_role_players;
%newobject relation_get_relating;

%newobject attribute_get_value;
%newobject attribute_get_owners;

%newobject thing_type_get_label;
%newobject thing_type_get_owns;
%newobject thing_type_get_owns_overridden;
%newobject thing_type_get_plays;
%newobject thing_type_get_plays_overridden;
%newobject thing_type_get_syntax;

%newobject entity_type_create;
%newobject entity_type_get_supertype;
%newobject entity_type_get_supertypes;
%newobject entity_type_get_subtypes;
%newobject entity_type_get_instances;

%newobject relation_type_create;
%newobject relation_type_get_supertype;
%newobject relation_type_get_supertypes;
%newobject relation_type_get_subtypes;
%newobject relation_type_get_instances;
%newobject relation_type_get_relates;
%newobject relation_type_get_relates_for_role_label;
%newobject relation_type_get_relates_overridden;

%newobject attribute_type_put;
%newobject attribute_type_get;
%newobject attribute_type_get_supertype;
%newobject attribute_type_get_supertypes;
%newobject attribute_type_get_subtypes;
%newobject attribute_type_get_subtypes_with_value_type;
%newobject attribute_type_get_instances;
%newobject attribute_type_get_regex;
%newobject attribute_type_get_owners;

%newobject role_type_get_relation_type;
%newobject role_type_get_scope;
%newobject role_type_get_name;
%newobject role_type_get_supertype;
%newobject role_type_get_supertypes;
%newobject role_type_get_subtypes;
%newobject role_type_get_relation_types;
%newobject role_type_get_player_types;
%newobject role_type_get_relation_instances;
%newobject role_type_get_player_instances;

%newobject connection_open_plaintext;
%newobject connection_open_encrypted;

%newobject credential_new;

%newobject database_get_name;
%newobject database_schema;
%newobject database_type_schema;
%newobject database_rule_schema;
%newobject database_manager_new;
%newobject database_iterator_next;

%newobject database_get_preferred_replica_info;
%newobject database_get_primary_replica_info;
%newobject database_get_replicas_info;

%newobject replica_info_get_address;
%newobject replica_info_iterator_next;

%newobject databases_all;
%newobject databases_get;

%newobject get_last_error;
%newobject error_code;
%newobject error_message;

%newobject schema_exception_code;
%newobject schema_exception_iterator_next;
%newobject schema_exception_message;

%newobject rule_get_label;
%newobject rule_get_when;
%newobject rule_get_then;
%newobject rule_to_string;

%newobject logic_manager_put_rule;
%newobject logic_manager_get_rule;

%newobject rule_iterator_next;

%newobject logic_manager_get_rules;

%newobject options_new;

%newobject concept_map_iterator_next;

%newobject query_match;
%newobject query_insert;
%newobject query_update;
%newobject query_match_aggregate;
%newobject query_match_group;
%newobject query_match_group_aggregate;
%newobject query_explain;

%newobject concept_map_group_iterator_next;
%newobject numeric_group_iterator_next;
%newobject explanation_get_mapping;
%newobject explanation_iterator_next;

%newobject session_new;
%newobject session_get_database_name;

%newobject transaction_new;

%newobject users_all;
%newobject users_current_user;
%newobject users_get;

%newobject user_get_username;
%newobject user_iterator_next;
%newobject user_manager_new;

%include "typedb_client.h"
