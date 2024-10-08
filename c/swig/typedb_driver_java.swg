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

%javaconst(1);

%include "various.i"
%include "enums.swg"

#define PACKAGE_ "com.typedb.driver.jni"
#define PACKAGE_PATH_ "com/typedb/driver/jni"

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
            super(e.getMessage());
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
    }
}

%nojavaexception init_logging;

/* simple getters do not throw */
// %nojavaexception options_new;
// %nojavaexception options_get_infer;
// %nojavaexception options_get_trace_inference;
// %nojavaexception options_get_explain;
// %nojavaexception options_get_parallel;
// %nojavaexception options_get_prefetch;
// %nojavaexception options_get_prefetch_size;
// %nojavaexception options_get_session_idle_timeout_millis;
// %nojavaexception options_get_transaction_timeout_millis;
// %nojavaexception options_get_schema_lock_acquire_timeout_millis;
// %nojavaexception options_get_read_any_replica;
// %nojavaexception options_set_infer;
// %nojavaexception options_set_trace_inference;
// %nojavaexception options_set_explain;
// %nojavaexception options_set_parallel;
// %nojavaexception options_set_prefetch;
// %nojavaexception options_set_prefetch_size;
// %nojavaexception options_set_session_idle_timeout_millis;
// %nojavaexception options_set_transaction_timeout_millis;
// %nojavaexception options_set_schema_lock_acquire_timeout_millis;
// %nojavaexception options_set_read_any_replica;
// %nojavaexception options_has_infer;
// %nojavaexception options_has_trace_inference;
// %nojavaexception options_has_explain;
// %nojavaexception options_has_parallel;
// %nojavaexception options_has_prefetch;
// %nojavaexception options_has_prefetch_size;
// %nojavaexception options_has_session_idle_timeout_millis;
// %nojavaexception options_has_transaction_timeout_millis;
// %nojavaexception options_has_schema_lock_acquire_timeout_millis;
// %nojavaexception options_has_read_any_replica;

%nojavaexception error_code;
%nojavaexception error_message;

%nojavaexception driver_is_open;

%nojavaexception transaction_is_open;

//%nojavaexception user_get_username;
//%nojavaexception user_get_password_expiry_seconds;

//%nojavaexception replica_info_get_server;
//%nojavaexception replica_info_is_primary;
//%nojavaexception replica_info_is_preferred;
//%nojavaexception replica_info_get_term;

%nojavaexception database_get_name;
//%nojavaexception database_get_replicas_info;
//%nojavaexception database_get_primary_replica_info;
//%nojavaexception database_get_preferred_replica_info;

%nojavaexception concept_is_entity;
%nojavaexception concept_is_relation;
%nojavaexception concept_is_attribute;
%nojavaexception concept_is_value;
%nojavaexception concept_is_entity_type;
%nojavaexception concept_is_relation_type;
%nojavaexception concept_is_attribute_type;
%nojavaexception concept_is_role_type;
%nojavaexception concept_as_entity;
%nojavaexception concept_as_relation;
%nojavaexception concept_as_attribute;
%nojavaexception concept_as_value;
%nojavaexception concept_as_entity_type;
%nojavaexception concept_as_relation_type;
%nojavaexception concept_as_attribute_type;
%nojavaexception concept_to_string;
%nojavaexception concept_equals;

%nojavaexception entity_get_iid;
%nojavaexception relation_get_iid;
%nojavaexception entity_get_type;
%nojavaexception relation_get_type;
%nojavaexception attribute_get_type;
%nojavaexception attribute_get_value;

%nojavaexception value_get_boolean;
%nojavaexception value_get_date_as_seconds;
%nojavaexception value_get_datetime;
%nojavaexception value_get_datetime_tz;
%nojavaexception value_get_decimal;
%nojavaexception value_get_double;
%nojavaexception value_get_duration;
%nojavaexception value_get_long;
%nojavaexception value_get_string;
%nojavaexception value_get_struct;
%nojavaexception value_is_boolean;
%nojavaexception value_is_date;
%nojavaexception value_is_datetime;
%nojavaexception value_is_datetime_tz;
%nojavaexception value_is_decimal;
%nojavaexception value_is_double;
%nojavaexception value_is_duration;
%nojavaexception value_is_long;
%nojavaexception value_is_string;
%nojavaexception value_is_struct;

%nojavaexception value_get_value_type;
%nojavaexception attribute_type_get_value_type;

%nojavaexception attribute_type_is_boolean;
%nojavaexception attribute_type_is_date;
%nojavaexception attribute_type_is_datetime;
%nojavaexception attribute_type_is_datetime_tz;
%nojavaexception attribute_type_is_decimal;
%nojavaexception attribute_type_is_double;
%nojavaexception attribute_type_is_duration;
%nojavaexception attribute_type_is_long;
%nojavaexception attribute_type_is_string;
%nojavaexception attribute_type_is_struct;
%nojavaexception attribute_type_is_untyped;

%nojavaexception concept_row_equals;
%nojavaexception concept_row_get;
%nojavaexception concept_row_get_column_names;
%nojavaexception concept_row_get_query_type;
%nojavaexception concept_row_get_concepts;
%nojavaexception concept_row_get_index;
%nojavaexception concept_row_to_string;

%nojavaexception query_answer_get_rows;
%nojavaexception query_answer_is_ok;
%nojavaexception query_answer_is_concept_rows_stream;
%nojavaexception query_answer_is_concept_trees_stream;

%nojavaexception value_group_to_string;
%nojavaexception value_group_equals;
%nojavaexception value_group_get_owner;
%nojavaexception value_group_get_value;

%nojavaexception StringPair::_0;
%nojavaexception StringPair::_1;

%nojavaexception DatetimeAndZoneId::datetime_in_nanos;
%nojavaexception DatetimeAndZoneId::zone_id;

%nojavaexception DatetimeInNanos::seconds;
%nojavaexception DatetimeInNanos::subsec_nanos;

%nojavaexception StringAndOptValue::string;
%nojavaexception StringAndOptValue::value;

%nojavaexception Decimal::integer;
%nojavaexception Decimal::fractional;

%nojavaexception Duration::months;
%nojavaexception Duration::days;
%nojavaexception Duration::nanos;

/* director constructors do not throw */
%nojavaexception TransactionCallbackDirector;

/* destructors do not throw */
%nojavaexception ~Concept;
%nojavaexception ~ConceptIterator;
%nojavaexception ~ConceptRow;
%nojavaexception ~ConceptRowIterator;
//%nojavaexception ~Credential;
%nojavaexception ~Database;
%nojavaexception ~DatabaseIterator;
%nojavaexception ~DatabaseManager;
%nojavaexception ~DatetimeAndZoneId;
%nojavaexception ~DatetimeInNanos;
%nojavaexception ~Decimal;
%nojavaexception ~Duration;
%nojavaexception ~Error;
//%nojavaexception ~Options;
//%nojavaexception ~ReplicaInfo;
%nojavaexception ~StringIterator;
%nojavaexception ~StringAndOptValue;
%nojavaexception ~StringAndOptValueIterator;
%nojavaexception ~Transaction;
%nojavaexception ~TransactionCallbackDirector;
%nojavaexception ~TypeDBDriver;
%nojavaexception ~QueryAnswer;
%nojavaexception ~ValueType;
//%nojavaexception ~User;
//%nojavaexception ~UserIterator;
//%nojavaexception ~UserManager;
%nojavaexception ~Value;
%nojavaexception ~ValueGroup;
%nojavaexception ~ValueGroupIterator;

/* one-shot network requests do not throw when first constructed, only when resolved */
%nojavaexception entity_type_get_label;
%nojavaexception relation_type_get_label;
%nojavaexception attribute_type_get_label;
%nojavaexception role_type_get_label;
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
%typemap(javadestruct, methodname="delete", methodmodifiers="public synchronized") Type ## Promise %{{
    if (swigCPtr != 0) {
        if (swigCMemOwn) {
            swigCMemOwn = false;
            get();
        }
        swigCPtr = 0;
    }
}%}
%enddef

%promise(QueryAnswer, query_answer)
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
%iterator(ConceptRow, concept_row)
%iterator(ValueGroup, value_group)
%iterator(String, string)
%iterator(StringAndOptValue, string_and_opt_value)
//%iterator(User, user)
%iterator(Database, database)
//%iterator(ReplicaInfo, replica_info)

%typemap(javabody) QueryAnswer %{
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected $javaclassname(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr($javaclassname obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    public ConceptRowIterator intoRows() {
        swigCMemOwn = false;
        try {
            return typedb_driver.query_answer_into_rows(this);
        } catch (Error e) {
            throw new Error.Unchecked(e);
        }
    }
%}

%typemap(javadestruct, methodname="delete", methodmodifiers="public synchronized") QueryAnswer %{{
    if (swigCPtr != 0) {
        if (swigCMemOwn) {
            swigCMemOwn = false;
            typedb_driverJNI.delete_QueryAnswer(swigCPtr);
        }
        swigCPtr = 0;
    }
}%}

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

%array(Concept)

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
