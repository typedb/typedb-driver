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
%include "arrays_csharp.i"

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
%typemap(csbody) Error %{
    private global::System.Runtime.InteropServices.HandleRef swigCPtr;
    protected bool swigCMemOwn;

    internal Error(global::System.IntPtr cPtr, bool cMemoryOwn)
        : base((typedb_driverPINVOKE.error_code(new global::System.Runtime.InteropServices.HandleRef(null, cPtr)) + " "
            + typedb_driverPINVOKE.error_message(new global::System.Runtime.InteropServices.HandleRef(null, cPtr))).Trim())
    {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = new global::System.Runtime.InteropServices.HandleRef(this, cPtr);
    }

    internal static global::System.Runtime.InteropServices.HandleRef getCPtr(Error obj)
    {
        return (obj == null)
            ? new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero)
            : obj.swigCPtr;
    }

    internal static global::System.Runtime.InteropServices.HandleRef swigRelease(Error obj) {
      if (obj != null) {
        if (!obj.swigCMemOwn)
          throw new global::System.ApplicationException("Cannot release ownership as memory is not owned");
        global::System.Runtime.InteropServices.HandleRef ptr = obj.swigCPtr;
        obj.swigCMemOwn = false;
        obj.Dispose();
        return ptr;
      } else {
        return new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero);
      }
    }

    public class Unchecked : System.Exception
    {
        Unchecked(Error e)
            : base(e.ToString())
        {
        }
    }
%}

%insert(runtime) %{
  // Code to handle throwing of C# CustomApplicationException from C/C++ code.
  // The equivalent delegate to the callback, CSharpExceptionCallback_t, is CustomExceptionDelegate
  // and the equivalent customExceptionCallback instance is customDelegate
  typedef void (SWIGSTDCALL* CSharpExceptionCallback_t)(const char*);
  CSharpExceptionCallback_t customExceptionCallback = NULL;

  extern "C" SWIGEXPORT
  void SWIGSTDCALL CustomExceptionRegisterCallback(CSharpExceptionCallback_t customCallback) {
    customExceptionCallback = customCallback;
  }

  // Note that SWIG detects any method calls named starting with
  // SWIG_CSharpSetPendingException for warning 845
  static void SWIG_CSharpSetPendingExceptionCustom(const char* error) {
    customExceptionCallback(error);
  }
%}

%pragma(csharp) imclasscode=%{
// Custom C# Exception
public class SuperCustomExceptionOmg : global::System.Exception {
  public SuperCustomExceptionOmg(string message)
    : base(message.Trim()) {
  }
}
%}

%pragma(csharp) imclasscode=%{
  class CustomExceptionHelper {
    // C# delegate for the C/C++ customExceptionCallback
    public delegate void CustomExceptionDelegate(string error);
    static CustomExceptionDelegate customDelegate =
                                   new CustomExceptionDelegate(SetPendingCustomException);

    [global::System.Runtime.InteropServices.DllImport("$dllimport", EntryPoint="CustomExceptionRegisterCallback")]
    public static extern
           void CustomExceptionRegisterCallback(CustomExceptionDelegate customCallback);

    static void SetPendingCustomException(string error) {
      SWIGPendingException.Set(new SuperCustomExceptionOmg(error));
    }

    static CustomExceptionHelper() {
      CustomExceptionRegisterCallback(customDelegate);
    }
  }
  static CustomExceptionHelper exceptionHelper = new CustomExceptionHelper();
%}


%exception %{
    $action
    if (check_error())
    {
        Error* error = get_last_error();
        const char* error_msg = error_message(error);
        SWIG_CSharpSetPendingExceptionCustom(error_msg);

        // return $null; // TODO: This line is probably needed for potential memory cleanup, but there could be
                         // TODO: dangerous calls after the error check. Delete later.
    }
%}

%noexception init_logging;

/* simple getters do not throw */
%noexception options_new;
%noexception options_get_infer;
%noexception options_get_trace_inference;
%noexception options_get_explain;
%noexception options_get_parallel;
%noexception options_get_prefetch;
%noexception options_get_prefetch_size;
%noexception options_get_session_idle_timeout_millis;
%noexception options_get_transaction_timeout_millis;
%noexception options_get_schema_lock_acquire_timeout_millis;
%noexception options_get_read_any_replica;
%noexception options_set_infer;
%noexception options_set_trace_inference;
%noexception options_set_explain;
%noexception options_set_parallel;
%noexception options_set_prefetch;
%noexception options_set_prefetch_size;
%noexception options_set_session_idle_timeout_millis;
%noexception options_set_transaction_timeout_millis;
%noexception options_set_schema_lock_acquire_timeout_millis;
%noexception options_set_read_any_replica;
%noexception options_has_infer;
%noexception options_has_trace_inference;
%noexception options_has_explain;
%noexception options_has_parallel;
%noexception options_has_prefetch;
%noexception options_has_prefetch_size;
%noexception options_has_session_idle_timeout_millis;
%noexception options_has_transaction_timeout_millis;
%noexception options_has_schema_lock_acquire_timeout_millis;
%noexception options_has_read_any_replica;

%noexception annotation_new_key;
%noexception annotation_new_unique;
%noexception annotation_is_key;
%noexception annotation_is_unique;
%noexception annotation_to_string;
%noexception annotation_equals;

%noexception error_code;
%noexception error_message;
%noexception schema_exception_code;
%noexception schema_exception_message;

%noexception connection_is_open;

%noexception session_is_open;
%noexception session_get_database_name;

%noexception transaction_is_open;

%noexception user_get_username;
%noexception user_get_password_expiry_seconds;

%noexception replica_info_get_address;
%noexception replica_info_is_primary;
%noexception replica_info_is_preferred;
%noexception replica_info_get_term;

%noexception database_get_name;
%noexception database_get_replicas_info;
%noexception database_get_primary_replica_info;
%noexception database_get_preferred_replica_info;

%noexception concept_is_entity;
%noexception concept_is_relation;
%noexception concept_is_attribute;
%noexception concept_is_value;
%noexception concept_is_root_thing_type;
%noexception concept_is_entity_type;
%noexception concept_is_relation_type;
%noexception concept_is_attribute_type;
%noexception concept_is_role_type;
%noexception concept_as_entity;
%noexception concept_as_relation;
%noexception concept_as_attribute;
%noexception concept_as_value;
%noexception concept_as_root_thing_type;
%noexception concept_as_entity_type;
%noexception concept_as_relation_type;
%noexception concept_as_attribute_type;
%noexception concept_to_string;
%noexception concept_equals;

%noexception concepts_get_root_entity_type;
%noexception concepts_get_root_relation_type;
%noexception concepts_get_root_attribute_type;

%noexception attribute_type_get_value_type;

%noexception role_type_is_root;
%noexception role_type_is_abstract;
%noexception role_type_get_scope;
%noexception role_type_get_name;

%noexception thing_get_iid;
%noexception thing_get_is_inferred;
%noexception entity_get_type;
%noexception relation_get_type;
%noexception attribute_get_type;
%noexception attribute_get_value;

%noexception value_get_boolean;
%noexception value_get_date_time_as_millis;
%noexception value_get_double;
%noexception value_get_long;
%noexception value_get_string;
%noexception value_is_boolean;
%noexception value_is_date_time;
%noexception value_is_double;
%noexception value_is_long;
%noexception value_is_string;
%noexception value_new_boolean;
%noexception value_new_date_time_from_millis;
%noexception value_new_double;
%noexception value_new_long;
%noexception value_new_string;

%noexception role_player_get_role_type;
%noexception role_player_get_player;

%noexception rule_get_label;
%noexception rule_get_when;
%noexception rule_get_then;
%noexception rule_to_string;

%noexception concept_map_equals;
%noexception concept_map_get;
%noexception concept_map_get_explainables;
%noexception concept_map_get_values;
%noexception concept_map_get_variables;
%noexception concept_map_to_string;

%noexception explainable_get_conjunction;
%noexception explainable_get_id;
%noexception explainables_equals;
%noexception explainables_get_attribute;
%noexception explainables_get_attributes_keys;
%noexception explainables_get_ownership;
%noexception explainables_get_ownerships_keys;
%noexception explainables_get_relation;
%noexception explainables_get_relations_keys;
%noexception explainables_to_string;

%noexception explanation_equals;
%noexception explanation_to_string;
%noexception explanation_get_rule;
%noexception explanation_get_conclusion;
%noexception explanation_get_condition;
%noexception explanation_get_mapped_variables;
%noexception explanation_get_mapping;

%noexception concept_map_group_get_owner;
%noexception concept_map_group_get_concept_maps;
%noexception concept_map_group_to_string;
%noexception concept_map_group_equals;

%noexception value_group_to_string;
%noexception value_group_equals;
%noexception value_group_get_owner;
%noexception value_group_get_value;

%noexception StringPair::_0;
%noexception StringPair::_1;

/* director constructors do not throw */
%noexception SessionCallbackDirector;
%noexception TransactionCallbackDirector;

/* destructors do not throw */
%noexception ~Annotation;
%noexception ~Concept;
%noexception ~ConceptIterator;
%noexception ~ConceptMap;
%noexception ~ConceptMapGroup;
%noexception ~ConceptMapGroupIterator;
%noexception ~ConceptMapIterator;
%noexception ~Connection;
%noexception ~Credential;
%noexception ~Database;
%noexception ~DatabaseIterator;
%noexception ~DatabaseManager;
%noexception ~DatabaseManager;
%noexception ~Error;
%noexception ~Error;
%noexception ~Explainable;
%noexception ~Explainables;
%noexception ~Explanation;
%noexception ~ExplanationIterator;
%noexception ~Options;
%noexception ~ReplicaInfo;
%noexception ~ReplicaInfoIterator;
%noexception ~RolePlayer;
%noexception ~RolePlayerIterator;
%noexception ~Rule;
%noexception ~RuleIterator;
%noexception ~SchemaException;
%noexception ~SchemaExceptionIterator;
%noexception ~Session;
%noexception ~SessionCallbackDirector;
%noexception ~StringIterator;
%noexception ~StringPair;
%noexception ~StringPairIterator;
%noexception ~Transaction;
%noexception ~TransactionCallbackDirector;
%noexception ~User;
%noexception ~UserIterator;
%noexception ~UserManager;
%noexception ~Value;
%noexception ~ValueGroup;
%noexception ~ValueGroupIterator;

/* one-shot network requests do not throw when first constructed, only when resolved */
%noexception concepts_get_entity_type;
%noexception concepts_get_relation_type;
%noexception concepts_get_attribute_type;
%noexception concepts_put_entity_type;
%noexception concepts_put_relation_type;
%noexception concepts_put_attribute_type;
%noexception concepts_get_entity;
%noexception concepts_get_relation;
%noexception concepts_get_attribute;
%noexception thing_delete;
%noexception thing_is_deleted;
%noexception thing_set_has;
%noexception thing_unset_has;
%noexception relation_add_role_player;
%noexception relation_remove_role_player;
%noexception thing_type_is_deleted;
%noexception thing_type_delete;
%noexception thing_type_set_label;
%noexception thing_type_set_abstract;
%noexception thing_type_unset_abstract;
%noexception thing_type_get_owns_overridden;
%noexception thing_type_set_owns;
%noexception thing_type_unset_owns;
%noexception thing_type_get_plays_overridden;
%noexception thing_type_set_plays;
%noexception thing_type_unset_plays;
%noexception thing_type_get_syntax;
%noexception entity_type_create;
%noexception entity_type_get_supertype;
%noexception entity_type_set_supertype;
%noexception relation_type_create;
%noexception relation_type_get_supertype;
%noexception relation_type_set_supertype;
%noexception relation_type_get_relates_for_role_label;
%noexception relation_type_get_relates_overridden;
%noexception relation_type_set_relates;
%noexception relation_type_unset_relates;
%noexception attribute_type_put;
%noexception attribute_type_get;
%noexception attribute_type_get_supertype;
%noexception attribute_type_set_supertype;
%noexception attribute_type_get_regex;
%noexception attribute_type_set_regex;
%noexception attribute_type_unset_regex;
%noexception role_type_is_deleted;
%noexception role_type_get_relation_type;
%noexception role_type_delete;
%noexception role_type_set_label;
%noexception role_type_get_supertype;
%noexception rule_set_label;
%noexception rule_delete;
%noexception rule_is_deleted;
%noexception logic_manager_put_rule;
%noexception logic_manager_get_rule;
%noexception query_define;
%noexception query_undefine;
%noexception query_delete;
%noexception query_get_aggregate;
%noexception transaction_commit;
%noexception transaction_rollback;


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

%typemap(ctype) char ** "char**" // "void*" ?
%typemap(cstype) char ** "string[]" // "global::System.IntPtr" ?
%typemap(
    imtype,
    inattributes = "[
        global::System.Runtime.InteropServices.In,
        global::System.Runtime.InteropServices.Out,
        global::System.Runtime.InteropServices.MarshalAs(
            global::System.Runtime.InteropServices.UnmanagedType.LPArray,
            ArraySubType=global::System.Runtime.InteropServices.UnmanagedType.LPStr)]"
    ) char** "string[]"

%typemap(csin) char ** "$csinput"
%typemap(csout) char **
{
    return $imcall;
}

%typemap(in)      char ** "$1 = $input;" // TODO: No memory work for sure?
%typemap(freearg) char ** "" // TODO: No memory work for sure?

/* ValueType* needs special handling */
%typemap(in) enum ValueType * {
    if ($input >= 0)
    {
        $1 = (enum ValueType *)malloc(sizeof(enum ValueType));
        *$1 = (enum ValueType)$input;
    }
}

/* This cleans up the memory we malloc'd before the function call */
%typemap(freearg) enum ValueType * { free($1); }

/* These 3 typemaps tell SWIG what intermediate (pinvoke) and C# types to use */
%typemap(ctype) enum ValueType * "int"
%typemap(imtype) enum ValueType * "int"
%typemap(cstype) enum ValueType * "ValueType"

/* These 2 typemaps handle the conversion of the imtype to cstype typemap type
      and vice versa */
%typemap(csin) enum ValueType * "(int)$csinput"
%typemap(csout) enum ValueType * {
    return $imcall;
}
