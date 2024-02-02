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

%csconst(1);

%include "enums.swg"

//%pragma(csharp) modulecode=%{ // TODO: Using this generates even more warnings, because SWIG doesn't use explicit "?"
//   #nullable enable
//%}

%typemap(cscode) SWIGTYPE %{
    public $csclassname Released()
    {
        var cPtr = swigCPtr.Handle;
        if (swigCMemOwn)
        {
            swigCPtr = new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero);
            swigCMemOwn = false;
        }

        return new $csclassname(cPtr, false);
    }

    public bool IsOwned()
    {
        return swigCMemOwn;
    }
%}

%typemap(csbase) Error "System.Exception";
// TODO: Write new code for C#'s exceptions
//%typemap(csbody) Error %{
//    private transient long swigCPtr;
//    protected transient boolean swigCMemOwn;
//
//    protected $csclassname(long cPtr, boolean cMemoryOwn) {
//        super((typedb_driverJNI.error_code(cPtr, null) + " " + typedb_driverJNI.error_message(cPtr, null)).strip());
//        swigCMemOwn = cMemoryOwn;
//        swigCPtr = cPtr;
//    }
//
//    protected static long getCPtr($csclassname obj) {
//        return (obj == null) ? 0 : obj.swigCPtr;
//    }
//
//    public static class Unchecked extends RuntimeException {
//        Unchecked(Error e) {
//            super(e.getMessage());
//        }
//    }
//%}
//
//%ignore get_last_error;
//%javaexception("Error") {
//    $action
//    if (check_error()) {
//        jenv->ExceptionClear();
//
//        jlong jerror = 0;
//        Error* error = get_last_error();
//        *(Error**)&jerror = error;
//        jclass exceptionClass = jenv->FindClass(PACKAGE_PATH_ "/Error");
//        jmethodID ctor = jenv->GetMethodID(exceptionClass, "<init>", "(JZ)V");
//        jthrowable throwable = (jthrowable)jenv->NewObject(exceptionClass, ctor, jerror, true);
//
//        jenv->Throw(throwable);
//
//        return $null;
//    }
//}
//
//%typemap(csbody) ThrowingSupplier %{
//    protected static long getCPtr($csclassname obj) {
//        return (obj == null) ? 0 : obj.swigCPtr;
//    }
//%}


// TODO: Reimplement for C#
//%define %promise(Type, function_prefix)
//%typemap(csinterfaces) Type ## Promise "java.util.function.Supplier<Type>";
//%typemap(csbody) Type ## Promise %{
//    [ScriptIgnore]
//    private long swigCPtr;
//
//    [ScriptIgnore]
//    protected bool swigCMemOwn;
//
//    protected $csclassname(long cPtr, boolean cMemoryOwn) {
//        swigCMemOwn = cMemoryOwn;
//        swigCPtr = cPtr;
//    }
//
//    protected static long getCPtr($csclassname obj) {
//        return (obj == null) ? 0 : obj.swigCPtr;
//    }
//
//    public override Type get() {
//        swigCMemOwn = false;
//        try {
//            return typedb_driver.function_prefix ## _promise_resolve(this);
//        } catch (Error e) {
//            throw new Error.Unchecked(e);
//        }
//    }
//%}
//%enddef
//
//%promise(Concept, concept)
//%promise(Rule, rule)
//%promise(String, string)

// TODO: Reimplement for C#
///* void promises require special handling */
//%typemap(csinterfaces) VoidPromise "java.util.function.Supplier<Void>";
//%typemap(cscode) VoidPromise %{
//    public override void get() {
//        swigCMemOwn = false;
//        try {
//            typedb_driver.void_promise_resolve(this);
//        } catch (Error e) {
//            throw new Error.Unchecked(e);
//        }
//        return null;
//    }
//%}
//%typemap(csdispose, methodname="delete", methodmodifiers="public [MethodImpl(MethodImplOptions.Synchronized)]") VoidPromise %{{
//    if (swigCPtr != 0) {
//        if (swigCMemOwn) {
//            swigCMemOwn = false;
//            get();
//        }
//        swigCPtr = 0;
//    }
//}%}

// TODO: Reimplement for C#
///* bool promises require special handling */
//%typemap(csinterfaces) BoolPromise "java.util.function.Supplier<Boolean>";
//%typemap(cscode) BoolPromise %{
//    public override bool get() {
//        swigCMemOwn = false;
//        try {
//            return typedb_driver.bool_promise_resolve(this);
//        } catch (Error e) {
//            throw new Error.Unchecked(e);
//        }
//    }
//%}
//%typemap(csdispose, methodname="delete", methodmodifiers="public [MethodImpl(MethodImplOptions.Synchronized)]") BoolPromise %{{
//    if (swigCPtr != 0) {
//        if (swigCMemOwn) {
//            swigCMemOwn = false;
//            get();
//        }
//        swigCPtr = 0;
//    }
//}%}

%define %iterator(Type, CapitalizedType, FunctionPrefix)
%typemap(csinterfaces) CapitalizedType ## Iterator "System.Collections.Generic.IEnumerator<Type>";
%typemap(cscode) CapitalizedType ## Iterator %{
    private bool _isFinished = false;
    private Type? _current = null;

    object System.Collections.IEnumerator.Current
    {
        get
        {
            return Current;
        }
    }

    public Type Current
    {
        get
        {
            if (_current == null)
            {
                // TODO: Throw here: throw new Error.Unchecked(e);
            }

            return _current;
        }
    }

    public bool MoveNext()
    {
        if (_isFinished)
        {
            return false;
        }

        _current = typedb_driver.FunctionPrefix ## _iterator_next(this);
        if (_current == null)
        {
            _isFinished = true;
            return false;
        }

        return true;
    }

    public void Reset()
    {
        // We don't need and don't support Reset().
        // TODO: Throw here: throw new Error.Unchecked(e);
    }
%}
%enddef

%iterator(Concept, Concept, concept)
%iterator(RolePlayer, RolePlayer, role_player)
%iterator(ConceptMap, ConceptMap, concept_map)
%iterator(ConceptMapGroup, ConceptMapGroup, concept_map_group)
%iterator(ValueGroup, ValueGroup, value_group)
%iterator(Explanation, Explanation, explanation)
%iterator(string, String, string)
%iterator(StringPair, StringPair, string_pair)
%iterator(Rule, Rule, rule)
%iterator(User, User, user)
%iterator(Database, Database, database)
%iterator(ReplicaInfo, ReplicaInfo, replica_info)
%iterator(SchemaException, SchemaException, schema_exception)

// TODO: Arrays?
//%define %array(Type)
//%typemap(in) Type ** (jint size) {
//    int i = 0;
//    size = jenv->GetArrayLength($input);
//    $1 = (Type **) malloc((size+1) * sizeof(Type *));
//    jlong* array = (jlong*)jenv->GetLongArrayElements($input, JNI_FALSE);
//    /* make a copy of each ptr */
//    for (i = 0; i < size; i++) {
//        $1[i] = (Type*)array[i];
//    }
//    $1[i] = 0;
//}
//
//%typemap(freearg) Type ** { free($1); }
//
//%typemap(ctype) Type ** "jlongArray"
//%typemap(imtype) Type ** "long[]"
//%typemap(cstype) Type ** "Type[]"
//
//%typemap(csin) Type ** "java.util.Arrays.stream($csinput).mapToLong(Type::getCPtr).toArray()"
//%typemap(csout) Type ** {
//    return $imcall;
//}
//%enddef
//
//%array(Annotation)
//%array(Concept)
//%array(ValueType)

// TODO: char** ?
///* char** needs special handling */
//%typemap(in) char ** (jint size) {
//    int i = 0;
//    size = jenv->GetArrayLength($input);
//    $1 = (char **) malloc((size+1)*sizeof(char *));
//    /* make a copy of each string */
//    for (i = 0; i<size; i++) {
//        jstring j_string = (jstring)jenv->GetObjectArrayElement($input, i);
//        const char * c_string = jenv->GetStringUTFChars(j_string, 0);
//        $1[i] = (char*)malloc((strlen(c_string)+1)*sizeof(char));
//        strcpy($1[i], c_string);
//        jenv->ReleaseStringUTFChars(j_string, c_string);
//        jenv->DeleteLocalRef(j_string);
//    }
//    $1[i] = 0;
//}
//
//%typemap(freearg) char ** {
//    int i;
//    for (i=0; i<size$argnum-1; i++)
//        free($1[i]);
//    free($1);
//}
//
//%typemap(ctype) char ** "jobjectArray"
//%typemap(imtype) char ** "String[]"
//%typemap(cstype) char ** "String[]"
//
//%typemap(csin) char ** "$csinput"
//%typemap(csout) char ** {
//    return $imcall;
//}

// TODO: ValueType* ?
///* ValueType* needs special handling */
//%typemap(in) enum ValueType * {
//    if ($input >= 0) {
//        $1 = (enum ValueType *) malloc(sizeof(enum ValueType));
//        *$1 = (enum ValueType)$input;
//    }
//}
//
///* This cleans up the memory we malloc'd before the function call */
//%typemap(freearg) enum ValueType * { free($1); }
//
///* These 3 typemaps tell SWIG what JNI and Java types to use */
//%typemap(ctype) enum ValueType * "int"
//%typemap(imtype) enum ValueType * "int"
//%typemap(cstype) enum ValueType * "ValueType"
//
///* These 2 typemaps handle the conversion of the imtype to cstype typemap type
//      and vice versa */
//%typemap(csin) enum ValueType * "$csinput == null ? -1 : $csinput.swigValue()"
//%typemap(csout) enum ValueType * {
//    return $imcall;
//}
