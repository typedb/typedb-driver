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

%javaconst(1);

%include "various.i"
%include "enums.swg"

#define PACKAGE_ "com.vaticle.typedb.driver.jni"
#define PACKAGE_PATH_ "com/vaticle/typedb/driver/jni"

%typemap(javacode) SWIGTYPE %{
    public $javaclassname released() {
        long cptr = swigCPtr;
        if (swigCMemOwn) {
            swigCPtr = 0;
            swigCMemOwn = false;
        }
        return new $javaclassname(cptr, false);
    }

    public boolean isOwned() {
        return swigCMemOwn;
    }
%}

%typemap(javabase) Error "java.lang.Throwable";
%typemap(javabody) Error %{
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected $javaclassname(long cPtr, boolean cMemoryOwn) {
        super((typedb_driverJNI.error_code(cPtr, null) + " " + typedb_driverJNI.error_message(cPtr, null)).strip());
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr($javaclassname obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    } 

    public static class Unchecked extends RuntimeException {
        Unchecked(Error e) {
            super(e);
        }
    }
%}

%ignore get_last_error;
%javaexception("Error") {
    $action
    if (check_error()) {
        jenv->ExceptionClear();

        jlong jerror = 0;
        Error* error = get_last_error();
        *(Error**)&jerror = error;
        jclass exceptionClass = jenv->FindClass(PACKAGE_PATH_ "/Error");
        jmethodID ctor = jenv->GetMethodID(exceptionClass, "<init>", "(JZ)V");
        jthrowable throwable = (jthrowable)jenv->NewObject(exceptionClass, ctor, jerror, true);

        jenv->Throw(throwable);

        return $null;
    }
}

/* simple getters do not throw */
%nojavaexception options_new;
%nojavaexception options_get_infer;
%nojavaexception options_get_trace_inference;
%nojavaexception options_get_explain;
%nojavaexception options_get_parallel;
%nojavaexception options_get_prefetch;
%nojavaexception options_get_prefetch_size;
%nojavaexception options_get_session_idle_timeout_millis;
%nojavaexception options_get_transaction_timeout_millis;
%nojavaexception options_get_schema_lock_acquire_timeout_millis;
%nojavaexception options_get_read_any_replica;
%nojavaexception options_set_infer;
%nojavaexception options_set_trace_inference;
%nojavaexception options_set_explain;
%nojavaexception options_set_parallel;
%nojavaexception options_set_prefetch;
%nojavaexception options_set_prefetch_size;
%nojavaexception options_set_session_idle_timeout_millis;
%nojavaexception options_set_transaction_timeout_millis;
%nojavaexception options_set_schema_lock_acquire_timeout_millis;
%nojavaexception options_set_read_any_replica;
%nojavaexception options_has_infer;
%nojavaexception options_has_trace_inference;
%nojavaexception options_has_explain;
%nojavaexception options_has_parallel;
%nojavaexception options_has_prefetch;
%nojavaexception options_has_prefetch_size;
%nojavaexception options_has_session_idle_timeout_millis;
%nojavaexception options_has_transaction_timeout_millis;
%nojavaexception options_has_schema_lock_acquire_timeout_millis;
%nojavaexception options_has_read_any_replica;

%nojavaexception annotation_new_key;
%nojavaexception annotation_new_unique;
%nojavaexception annotation_is_key;
%nojavaexception annotation_is_unique;
%nojavaexception annotation_to_string;
%nojavaexception annotation_equals;

%nojavaexception error_code;
%nojavaexception error_message;
%nojavaexception schema_exception_code;
%nojavaexception schema_exception_message;

%nojavaexception connection_is_open;

%nojavaexception session_is_open;
%nojavaexception session_get_database_name;

%nojavaexception transaction_is_open;

%nojavaexception user_get_username;
%nojavaexception user_get_password_expiry_seconds;

%nojavaexception replica_info_get_address;
%nojavaexception replica_info_is_primary;
%nojavaexception replica_info_is_preferred;
%nojavaexception replica_info_get_term;

%nojavaexception database_get_name;
%nojavaexception database_get_replicas_info;
%nojavaexception database_get_primary_replica_info;
%nojavaexception database_get_preferred_replica_info;

%nojavaexception concept_is_entity;
%nojavaexception concept_is_relation;
%nojavaexception concept_is_attribute;
%nojavaexception concept_is_value;
%nojavaexception concept_is_root_thing_type;
%nojavaexception concept_is_entity_type;
%nojavaexception concept_is_relation_type;
%nojavaexception concept_is_attribute_type;
%nojavaexception concept_is_role_type;
%nojavaexception concept_as_entity;
%nojavaexception concept_as_relation;
%nojavaexception concept_as_attribute;
%nojavaexception concept_as_value;
%nojavaexception concept_as_root_thing_type;
%nojavaexception concept_as_entity_type;
%nojavaexception concept_as_relation_type;
%nojavaexception concept_as_attribute_type;
%nojavaexception concept_to_string;
%nojavaexception concept_equals;

%nojavaexception concepts_get_root_entity_type;
%nojavaexception concepts_get_root_relation_type;
%nojavaexception concepts_get_root_attribute_type;

%nojavaexception attribute_type_get_value_type;

%nojavaexception role_type_is_root;
%nojavaexception role_type_is_abstract;
%nojavaexception role_type_get_scope;
%nojavaexception role_type_get_name;

%nojavaexception thing_get_iid;
%nojavaexception thing_get_is_inferred;
%nojavaexception entity_get_type;
%nojavaexception relation_get_type;
%nojavaexception attribute_get_type;
%nojavaexception attribute_get_value;

%nojavaexception value_get_boolean;
%nojavaexception value_get_date_time_as_millis;
%nojavaexception value_get_double;
%nojavaexception value_get_long;
%nojavaexception value_get_string;
%nojavaexception value_is_boolean;
%nojavaexception value_is_date_time;
%nojavaexception value_is_double;
%nojavaexception value_is_long;
%nojavaexception value_is_string;
%nojavaexception value_new_boolean;
%nojavaexception value_new_date_time_from_millis;
%nojavaexception value_new_double;
%nojavaexception value_new_long;
%nojavaexception value_new_string;

%nojavaexception role_player_get_role_type;
%nojavaexception role_player_get_player;

%nojavaexception rule_get_label;
%nojavaexception rule_get_when;
%nojavaexception rule_get_then;
%nojavaexception rule_to_string;

%nojavaexception concept_map_equals;
%nojavaexception concept_map_get;
%nojavaexception concept_map_get_explainables;
%nojavaexception concept_map_get_values;
%nojavaexception concept_map_get_variables;
%nojavaexception concept_map_to_string;

%nojavaexception explainable_get_conjunction;
%nojavaexception explainable_get_id;
%nojavaexception explainables_equals;
%nojavaexception explainables_get_attribute;
%nojavaexception explainables_get_attributes_keys;
%nojavaexception explainables_get_ownership;
%nojavaexception explainables_get_ownerships_keys;
%nojavaexception explainables_get_relation;
%nojavaexception explainables_get_relations_keys;
%nojavaexception explainables_to_string;

%nojavaexception explanation_equals;
%nojavaexception explanation_to_string;
%nojavaexception explanation_get_rule;
%nojavaexception explanation_get_conclusion;
%nojavaexception explanation_get_condition;
%nojavaexception explanation_get_mapped_variables;
%nojavaexception explanation_get_mapping;

%nojavaexception concept_map_group_get_owner;
%nojavaexception concept_map_group_get_concept_maps;
%nojavaexception concept_map_group_to_string;
%nojavaexception concept_map_group_equals;

%nojavaexception value_get_double;
%nojavaexception value_get_long;
%nojavaexception value_is_double;
%nojavaexception value_is_long;
%nojavaexception value_is_nan;
%nojavaexception value_to_string;
%nojavaexception value_group_to_string;
%nojavaexception value_group_equals;
%nojavaexception value_group_get_owner;
%nojavaexception value_group_get_value;

%nojavaexception StringPair::_0;
%nojavaexception StringPair::_1;

/* director constructors do not throw */
%nojavaexception SessionCallbackDirector;
%nojavaexception TransactionCallbackDirector;

/* destructors do not throw */
%nojavaexception ~Annotation;
%nojavaexception ~Concept;
%nojavaexception ~ConceptIterator;
%nojavaexception ~ConceptMap;
%nojavaexception ~ConceptMapGroup;
%nojavaexception ~ConceptMapGroupIterator;
%nojavaexception ~ConceptMapIterator;
%nojavaexception ~Connection;
%nojavaexception ~Credential;
%nojavaexception ~Database;
%nojavaexception ~DatabaseIterator;
%nojavaexception ~DatabaseManager;
%nojavaexception ~DatabaseManager;
%nojavaexception ~Error;
%nojavaexception ~Error;
%nojavaexception ~Explainable;
%nojavaexception ~Explainables;
%nojavaexception ~Explanation;
%nojavaexception ~ExplanationIterator;
%nojavaexception ~Value;
%nojavaexception ~ValueGroup;
%nojavaexception ~ValueGroupIterator;
%nojavaexception ~Options;
%nojavaexception ~ReplicaInfo;
%nojavaexception ~ReplicaInfoIterator;
%nojavaexception ~RolePlayer;
%nojavaexception ~RolePlayerIterator;
%nojavaexception ~Rule;
%nojavaexception ~RuleIterator;
%nojavaexception ~SchemaException;
%nojavaexception ~SchemaExceptionIterator;
%nojavaexception ~Session;
%nojavaexception ~SessionCallbackDirector;
%nojavaexception ~StringIterator;
%nojavaexception ~StringPair;
%nojavaexception ~StringPairIterator;
%nojavaexception ~Transaction;
%nojavaexception ~TransactionCallbackDirector;
%nojavaexception ~User;
%nojavaexception ~UserIterator;
%nojavaexception ~UserManager;

/* one-shot network requests do not throw when first constructed, only when resolved */
%nojavaexception concepts_get_entity_type;
%nojavaexception concepts_get_relation_type;
%nojavaexception concepts_get_attribute_type;
%nojavaexception concepts_put_entity_type;
%nojavaexception concepts_put_relation_type;
%nojavaexception concepts_put_attribute_type;
%nojavaexception concepts_get_entity;
%nojavaexception concepts_get_relation;
%nojavaexception concepts_get_attribute;
%nojavaexception thing_delete;
%nojavaexception thing_is_deleted;
%nojavaexception thing_set_has;
%nojavaexception thing_unset_has;
%nojavaexception relation_add_role_player;
%nojavaexception relation_remove_role_player;
%nojavaexception thing_type_is_deleted;
%nojavaexception thing_type_delete;
%nojavaexception thing_type_set_label;
%nojavaexception thing_type_set_abstract;
%nojavaexception thing_type_unset_abstract;
%nojavaexception thing_type_get_owns_overridden;
%nojavaexception thing_type_set_owns;
%nojavaexception thing_type_unset_owns;
%nojavaexception thing_type_get_plays_overridden;
%nojavaexception thing_type_set_plays;
%nojavaexception thing_type_unset_plays;
%nojavaexception thing_type_get_syntax;
%nojavaexception entity_type_create;
%nojavaexception entity_type_get_supertype;
%nojavaexception entity_type_set_supertype;
%nojavaexception relation_type_create;
%nojavaexception relation_type_get_supertype;
%nojavaexception relation_type_set_supertype;
%nojavaexception relation_type_get_relates_for_role_label;
%nojavaexception relation_type_get_relates_overridden;
%nojavaexception relation_type_set_relates;
%nojavaexception relation_type_unset_relates;
%nojavaexception attribute_type_put;
%nojavaexception attribute_type_get;
%nojavaexception attribute_type_get_supertype;
%nojavaexception attribute_type_set_supertype;
%nojavaexception attribute_type_get_regex;
%nojavaexception attribute_type_set_regex;
%nojavaexception attribute_type_unset_regex;
%nojavaexception role_type_is_deleted;
%nojavaexception role_type_get_relation_type;
%nojavaexception role_type_delete;
%nojavaexception role_type_set_label;
%nojavaexception role_type_get_supertype;
%nojavaexception rule_set_label;
%nojavaexception rule_delete;
%nojavaexception rule_is_deleted;
%nojavaexception logic_manager_put_rule;
%nojavaexception logic_manager_get_rule;
%nojavaexception query_define;
%nojavaexception query_undefine;
%nojavaexception query_delete;
%nojavaexception query_get_aggregate;
%nojavaexception transaction_commit;
%nojavaexception transaction_rollback;

%typemap(javabody) ThrowingSupplier %{
    protected static long getCPtr($javaclassname obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    } 
%}


%define %promise(Type, function_prefix)
%typemap(javainterfaces) Type ## Promise "java.util.function.Supplier<Type>";
%nojavaexception ~ ## Type ## Promise;
%typemap(javabody) Type ## Promise %{
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected $javaclassname(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr($javaclassname obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    } 

    @Override
    public Type get() {
        swigCMemOwn = false;
        try {
            return typedb_driver.function_prefix ## _promise_resolve(this);
        } catch (Error e) {
            throw new Error.Unchecked(e);
        }
    }
%}
%enddef

%promise(Concept, concept)
%promise(Rule, rule)
%promise(String, string)

/* void promises require special handling */
%typemap(javainterfaces) VoidPromise "java.util.function.Supplier<Void>";
%nojavaexception ~VoidPromise;
%typemap(javacode) VoidPromise %{
    @Override
    public Void get() {
        swigCMemOwn = false;
        try {
            typedb_driver.void_promise_resolve(this);
        } catch (Error e) {
            throw new Error.Unchecked(e);
        }
        return null;
    }
%}
%typemap(javadestruct, methodname="delete", methodmodifiers="public synchronized") VoidPromise %{{
    if (swigCPtr != 0) {
        if (swigCMemOwn) {
            swigCMemOwn = false;
            get();
        }
        swigCPtr = 0;
    }
}%}

/* bool promises require special handling */
%typemap(javainterfaces) BoolPromise "java.util.function.Supplier<Boolean>";
%nojavaexception ~BoolPromise;
%typemap(javacode) BoolPromise %{
    @Override
    public Boolean get() {
        swigCMemOwn = false;
        try {
            return typedb_driver.bool_promise_resolve(this);
        } catch (Error e) {
            throw new Error.Unchecked(e);
        }
    }
%}
%typemap(javadestruct, methodname="delete", methodmodifiers="public synchronized") BoolPromise %{{
    if (swigCPtr != 0) {
        if (swigCMemOwn) {
            swigCMemOwn = false;
            get();
        }
        swigCPtr = 0;
    }
}%}

%define %iterator(Type, function_prefix)
%typemap(javainterfaces) Type ## Iterator "java.util.Iterator<Type>";
%typemap(javacode) Type ## Iterator %{
    private boolean isFinished = false;
    private Type next = null;

    private Type fetchNext() throws Error {
        next = typedb_driver.function_prefix ## _iterator_next(this);
        return next;
    }

    @Override
    public boolean hasNext() {
        if (isFinished) return false;
        if (next == null) {
            try {
                if (fetchNext() == null) {
                    isFinished = true;
                    return false;
                }
            } catch (Error e) {
                throw new Error.Unchecked(e);
            }
        }
        assert(next != null);
        return true;
    }

    @Override
    public Type next() {
        if (next == null && !hasNext()) return null;
        Type ret = next;
        next = null;
        return ret;
    }
%}
%enddef

%iterator(Concept, concept)
%iterator(RolePlayer, role_player) 
%iterator(ConceptMap, concept_map)
%iterator(ConceptMapGroup, concept_map_group)
%iterator(ValueGroup, value_group)
%iterator(Explanation, explanation)
%iterator(String, string)
%iterator(StringPair, string_pair)
%iterator(Rule, rule)
%iterator(User, user)
%iterator(Database, database)
%iterator(ReplicaInfo, replica_info)
%iterator(SchemaException, schema_exception)

%define %array(Type)
%typemap(in) Type ** (jint size) {
    int i = 0;
    size = jenv->GetArrayLength($input);
    $1 = (Type **) malloc((size+1)*sizeof(Type *));
    jlong* array = (jlong*)jenv->GetLongArrayElements($input, JNI_FALSE);
    /* make a copy of each ptr */
    for (i = 0; i<size; i++) {
        $1[i] = (Type*)array[i];
    }
    $1[i] = 0;
}

%typemap(freearg) Type ** { free($1); }

%typemap(jni) Type ** "jlongArray"
%typemap(jtype) Type ** "long[]"
%typemap(jstype) Type ** "Type[]"

%typemap(javain) Type ** "java.util.Arrays.stream($javainput).mapToLong(Type::getCPtr).toArray()"
%typemap(javaout) Type ** {
    return $jnicall;
}
%enddef

%array(Annotation)
%array(Concept)
%array(ValueType)

/* char** needs special handling */
%typemap(in) char ** (jint size) {
    int i = 0;
    size = jenv->GetArrayLength($input);
    $1 = (char **) malloc((size+1)*sizeof(char *));
    /* make a copy of each string */
    for (i = 0; i<size; i++) {
        jstring j_string = (jstring)jenv->GetObjectArrayElement($input, i);
        const char * c_string = jenv->GetStringUTFChars(j_string, 0);
        $1[i] = (char*)malloc((strlen(c_string)+1)*sizeof(char));
        strcpy($1[i], c_string);
        jenv->ReleaseStringUTFChars(j_string, c_string);
        jenv->DeleteLocalRef(j_string);
    }
    $1[i] = 0;
}

%typemap(freearg) char ** {
    int i;
    for (i=0; i<size$argnum-1; i++)
        free($1[i]);
    free($1);
}

%typemap(jni) char ** "jobjectArray"
%typemap(jtype) char ** "String[]"
%typemap(jstype) char ** "String[]"

%typemap(javain) char ** "$javainput"
%typemap(javaout) char ** {
    return $jnicall;
}

/* ValueType* needs special handling */
%typemap(in) enum ValueType * {
    if ($input >= 0) {
        $1 = (enum ValueType *) malloc(sizeof(enum ValueType));
        *$1 = (enum ValueType)$input;
    }
}

/* This cleans up the memory we malloc'd before the function call */
%typemap(freearg) enum ValueType * { free($1); }

/* These 3 typemaps tell SWIG what JNI and Java types to use */
%typemap(jni) enum ValueType * "jlong"
%typemap(jtype) enum ValueType * "long"
%typemap(jstype) enum ValueType * "ValueType"

/* These 2 typemaps handle the conversion of the jtype to jstype typemap type
      and vice versa */
%typemap(javain) enum ValueType * "$javainput == null ? -1 : $javainput.swigValue()"
%typemap(javaout) enum ValueType * {
    return $jnicall;
}
