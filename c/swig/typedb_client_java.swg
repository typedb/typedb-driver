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

#define PACKAGE_ "com.vaticle.typedb.client.jni"
#define PACKAGE_PATH_ "com/vaticle/typedb/client/jni"

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

%nojavaexception ~Connection;
%nojavaexception ~DatabaseManager;
%nojavaexception ~Database;
%nojavaexception ~Error;
%nojavaexception error_code;
%nojavaexception error_message;
%nojavaexception database_manager_new;
%nojavaexception database_get_name;

%typemap(javabase) Error "java.lang.RuntimeException";
%typemap(javabody) Error %{
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected $javaclassname(long cPtr, boolean cMemoryOwn) {
        super((typedb_clientJNI.error_code(cPtr, null) + " " + typedb_clientJNI.error_message(cPtr, null)).strip());
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr($javaclassname obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    } 
%}

%define %iterator(Type, function_prefix)
%typemap(javaimports) Type ## Iterator "
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
"
%typemap(javainterfaces) Type ## Iterator "java.util.Iterator<Type>";
%typemap(javacode) Type ## Iterator %{
    private boolean isFinished = false;
    private Type next = null;

    private Type fetchNext() {
        next = typedb_client.function_prefix ## _iterator_next(this);
        return next;
    }

    @Override
    public boolean hasNext() {
        if (isFinished) return false;
        if (next == null) {
            if (fetchNext() == null) {
                isFinished = true;
                return false;
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

    public Stream<Type> stream() {
        return StreamSupport.stream(
            spliteratorUnknownSize(this, NONNULL | IMMUTABLE | ORDERED),
            false
        );
    }
%}
%enddef

%iterator(Concept, concept)
%iterator(RolePlayer, role_player) 
%iterator(ConceptMap, concept_map)
%iterator(ConceptMapGroup, concept_map_group)
%iterator(NumericGroup, numeric_group)
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
