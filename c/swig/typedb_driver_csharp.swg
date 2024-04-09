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

%csconst(1);

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

    internal static global::System.Runtime.InteropServices.HandleRef swigRelease(Error obj)
    {
      if (obj == null)
      {
          return new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero);
      }

      if (!obj.swigCMemOwn)
      {
        throw new global::System.ApplicationException("Cannot release ownership as memory is not owned");
      }

      global::System.Runtime.InteropServices.HandleRef ptr = obj.swigCPtr;
      obj.swigCMemOwn = false;
      obj.Dispose();

      return ptr;
    }
%}

%insert(runtime) %{
    // Code to handle throwing of C# CustomApplicationException from C/C++ code.
    // The equivalent delegate to the callback, CSharpExceptionCallback_t, is NativeExceptionDelegate
    // and the equivalent nativeExceptionCallback instance is nativeDelegate
    typedef void (SWIGSTDCALL* CSharpExceptionCallback_t)(int*);
    CSharpExceptionCallback_t nativeExceptionCallback = NULL;

    extern "C" SWIGEXPORT
    void SWIGSTDCALL NativeExceptionRegisterCallback(CSharpExceptionCallback_t nativeCallback)
    {
        nativeExceptionCallback = nativeCallback;
    }

    // Note that SWIG detects any method calls named starting with
    // SWIG_CSharpSetPendingException and warns if its exception is not used where needed.
    static void SWIG_CSharpSetPendingExceptionNative(int* error)
    {
        nativeExceptionCallback(error);
    }
%}

%pragma(csharp) imclasscode=%{
    class NativeExceptionHelper
    {
        // C# delegate for the C/C++ nativeExceptionCallback
        public delegate void NativeExceptionDelegate(global::System.IntPtr error);
        static NativeExceptionDelegate s_nativeDelegate =
            new NativeExceptionDelegate(SetPendingNativeException);

        [global::System.Runtime.InteropServices.DllImport("$dllimport", EntryPoint="NativeExceptionRegisterCallback")]
        public static extern void NativeExceptionRegisterCallback(NativeExceptionDelegate nativeCallback);

        static void SetPendingNativeException(global::System.IntPtr error)
        {
            SWIGPendingException.Set(new Error(error, true));
        }

        static NativeExceptionHelper()
        {
            NativeExceptionRegisterCallback(s_nativeDelegate);
        }
    }

    static NativeExceptionHelper exceptionHelper = new NativeExceptionHelper();
%}

%exception %{
    $action

    if (check_error())
    {
        SWIG_CSharpSetPendingExceptionNative((int*)get_last_error());
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

%noexception replica_info_get_server_id;
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
%noexception thing_type_get_label;
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

%define %promise(Prefix, Type, function_prefix)
%typemap(csbody) Prefix ## Promise %{
    private global::System.Runtime.InteropServices.HandleRef swigCPtr;
    protected bool swigCMemOwn;

    internal $csclassname(global::System.IntPtr cPtr, bool cMemoryOwn)
    {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = new global::System.Runtime.InteropServices.HandleRef(this, cPtr);
    }

    internal static global::System.Runtime.InteropServices.HandleRef getCPtr($csclassname obj)
    {
        return (obj == null)
            ? new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero)
            : obj.swigCPtr;
    }

    public Type Resolve()
    {
        swigCMemOwn = false;
        return typedb_driver.function_prefix ## _promise_resolve(this);
    }
%}

%typemap(csdisposing, methodname="Dispose", methodmodifiers="protected", parameters="bool disposing") Prefix ## Promise %{{
    lock(this)
    {
        if (swigCPtr.Handle != global::System.IntPtr.Zero)
        {
            if (swigCMemOwn)
            {
                swigCMemOwn = false;
                Resolve(); // An equivalent for the standard "delete_xxxPromise".
            }

            swigCPtr = new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero);
        }
    }
}%}
%enddef

%promise(Concept, Concept, concept)
%promise(Rule, Rule, rule)
%promise(String, string, string)
%promise(Bool, bool, bool)

/* void promises require special handling */
%typemap(csbody) VoidPromise %{
    private global::System.Runtime.InteropServices.HandleRef swigCPtr;
    protected bool swigCMemOwn;

    internal $csclassname(global::System.IntPtr cPtr, bool cMemoryOwn)
    {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = new global::System.Runtime.InteropServices.HandleRef(this, cPtr);
    }

    internal static global::System.Runtime.InteropServices.HandleRef getCPtr($csclassname obj)
    {
        return (obj == null)
            ? new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero)
            : obj.swigCPtr;
    }

    public void Resolve()
    {
        swigCMemOwn = false;
        typedb_driver.void_promise_resolve(this);
    }
%}

%typemap(csdisposing, methodname="Dispose", methodmodifiers="protected", parameters="bool disposing") VoidPromise %{{
    lock(this)
    {
        if (swigCPtr.Handle != global::System.IntPtr.Zero)
        {
            if (swigCMemOwn)
            {
                swigCMemOwn = false;
                Resolve(); // An equivalent for the standard "delete_VoidPromise".
            }

            swigCPtr = new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero);
        }
    }
}%}

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
                throw new System.IndexOutOfRangeException("Enumerator's Current element is out of range");
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
        throw new System.NotSupportedException("This Enumerator does not support Reset");
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


%define %array(Type)
%typemap(ctype) Type ** "Type**"
%typemap(cstype) Type ** "Type[]"
%typemap(imtype) Type ** "global::System.IntPtr"

%typemap(
    csin,
    pre="var unmanaged$csinput = Type.ArrayUnmanaged($csinput);",
    post="global::System.Runtime.InteropServices.Marshal.FreeHGlobal(unmanaged$csinput);"
) Type ** "unmanaged$csinput"

%typemap(csout) Type **
{
    return $imcall;
}

%typemap(in) char ** "$1 = $input;"
%typemap(freearg) char ** ""

%typemap(cscode) Type %{
    public static global::System.IntPtr ArrayUnmanaged($csclassname[] input)
    {
        int arraySize = input.Length;
        global::System.IntPtr unmanagedArray = System.Runtime.InteropServices.Marshal.AllocHGlobal(
            (arraySize + 1) * System.Runtime.InteropServices.Marshal.SizeOf<global::System.IntPtr>());

        unsafe
        {
            global::System.IntPtr* arrayPtr = (global::System.IntPtr*)unmanagedArray.ToPointer();

            for (int i = 0; i < arraySize; i++)
            {
                arrayPtr[i] = Type.getCPtr(input[i]).Handle;
            }

            arrayPtr[arraySize] = global::System.IntPtr.Zero;
        }

        return unmanagedArray;
    }
%}
%enddef

%array(Annotation)
%array(Concept)


%typemap(ctype) char ** "char**"
%typemap(cstype) char ** "string[]"
%typemap(imtype) char ** "global::System.IntPtr"

%typemap(
    csin,
    pre="
        int arraySize$csinput = $csinput.Length;
        global::System.IntPtr unmanaged$csinput = System.Runtime.InteropServices.Marshal.AllocHGlobal(
            (arraySize$csinput + 1) * System.Runtime.InteropServices.Marshal.SizeOf<global::System.IntPtr>());

        unsafe
        {
            global::System.IntPtr* arrayPtr = (global::System.IntPtr*)unmanaged$csinput.ToPointer();

            for (int i = 0; i < arraySize$csinput; i++)
            {
                arrayPtr[i] = global::System.Runtime.InteropServices.Marshal.StringToCoTaskMemAnsi($csinput[i]);
            }

            arrayPtr[arraySize$csinput] = global::System.IntPtr.Zero;
        }",
    post="
        unsafe
        {
            global::System.IntPtr* arrayPtr = (global::System.IntPtr*)unmanaged$csinput.ToPointer();

            for (int i = 0; i < arraySize$csinput; i++)
            {
                global::System.Runtime.InteropServices.Marshal.FreeHGlobal(arrayPtr[i]);
            }
        }

        global::System.Runtime.InteropServices.Marshal.FreeHGlobal(unmanaged$csinput);"
) char ** "unmanaged$csinput"

%typemap(csout) char **
{
    return $imcall;
}

%typemap(in) char ** "$1 = $input;"
%typemap(freearg) char ** ""


%typemap(in) enum ValueType * {
    if ($input >= 0)
    {
        $1 = (enum ValueType *)malloc(sizeof(enum ValueType));
        *$1 = (enum ValueType)$input;
    }
}

%typemap(freearg) enum ValueType * { free($1); }

%typemap(ctype) enum ValueType * "int"
%typemap(imtype) enum ValueType * "int"
%typemap(cstype) enum ValueType * "ValueType?"

%typemap(csin) enum ValueType * "$csinput == null ? -1 : (int)$csinput"
%typemap(csout) enum ValueType *
{
    return $imcall;
}
