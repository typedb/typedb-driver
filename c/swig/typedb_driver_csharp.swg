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

/* Error functions do not throw */
%noexception error_code;
%noexception error_message;

/* Driver state check does not throw */
%noexception driver_is_open;

/* Transaction state check does not throw */
%noexception transaction_is_open;

/* User property getters do not throw */
%noexception user_get_name;
%noexception user_get_password_expiry_seconds;

/* Replica info getters do not throw */
%noexception replica_info_get_server;
%noexception replica_info_is_primary;
%noexception replica_info_is_preferred;
%noexception replica_info_get_term;

/* Database property getters do not throw */
%noexception database_get_name;
%noexception database_get_replicas_info;
%noexception database_get_primary_replica_info;
%noexception database_get_preferred_replica_info;

/* Query options do not throw */
%noexception query_options_new;
%noexception query_options_get_include_instance_types;
%noexception query_options_get_include_query_structure;
%noexception query_options_get_prefetch_size;
%noexception query_options_has_include_instance_types;
%noexception query_options_has_include_query_structure;
%noexception query_options_has_prefetch_size;
%noexception query_options_set_include_instance_types;
%noexception query_options_set_include_query_structure;
%noexception query_options_set_prefetch_size;

/* Transaction options do not throw */
%noexception transaction_options_new;
%noexception transaction_options_get_transaction_timeout_millis;
%noexception transaction_options_get_schema_lock_acquire_timeout_millis;
%noexception transaction_options_has_transaction_timeout_millis;
%noexception transaction_options_has_schema_lock_acquire_timeout_millis;
%noexception transaction_options_set_transaction_timeout_millis;
%noexception transaction_options_set_schema_lock_acquire_timeout_millis;

/* Query answer getters do not throw */
%noexception query_answer_get_query_type;
%noexception query_answer_is_ok;
%noexception query_answer_is_concept_row_stream;
%noexception query_answer_is_concept_document_stream;

/* Concept row getters do not throw */
%noexception concept_row_get_query_type;
%noexception concept_row_equals;
%noexception concept_row_to_string;

/* Director constructor does not throw */
%noexception TransactionCallbackDirector;

/* Destructors do not throw */
%noexception ~Connection;
%noexception ~Credentials;
%noexception ~Database;
%noexception ~DatabaseIterator;
%noexception ~Error;
%noexception ~QueryAnswer;
%noexception ~QueryOptions;
%noexception ~TransactionOptions;
%noexception ~ConceptRowIterator;
%noexception ~ConceptRow;
%noexception ~Concept;
%noexception ~ConceptIterator;
%noexception ~StringIterator;
%noexception ~StringAndOptValueIterator;
%noexception ~Transaction;
%noexception ~TransactionCallbackDirector;
%noexception ~User;
%noexception ~UserIterator;
%noexception ~ReplicaInfo;
%noexception ~ReplicaInfoIterator;

/* Commit and rollback are non-throwing operations on construction */
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

/* 3.0 API promises - only String and Bool are used */
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

/* 3.0 API iterators */
%iterator(Concept, Concept, concept)
%iterator(ConceptRow, ConceptRow, concept_row)
%iterator(string, String, string)
%iterator(StringAndOptValue, StringAndOptValue, string_and_opt_value)
%iterator(User, User, user)
%iterator(Database, Database, database)


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

/* 3.0 API arrays */
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


/* QueryAnswer needs custom handling because into_rows/into_documents consume ownership */
%typemap(csbody) QueryAnswer %{
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

    public ConceptRowIterator IntoRows()
    {
        swigCMemOwn = false;
        return typedb_driver.query_answer_into_rows(this);
    }

    public StringIterator IntoDocuments()
    {
        swigCMemOwn = false;
        return typedb_driver.query_answer_into_documents(this);
    }
%}

%typemap(csdisposing, methodname="Dispose", methodmodifiers="protected", parameters="bool disposing") QueryAnswer %{{
    lock(this)
    {
        if (swigCPtr.Handle != global::System.IntPtr.Zero)
        {
            if (swigCMemOwn)
            {
                swigCMemOwn = false;
                typedb_driverPINVOKE.delete_QueryAnswer(swigCPtr);
            }

            swigCPtr = new global::System.Runtime.InteropServices.HandleRef(null, global::System.IntPtr.Zero);
        }
    }
}%}
