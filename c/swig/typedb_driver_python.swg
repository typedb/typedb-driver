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

/* Arrays handling */
%define %array(Type)
%typemap(in) Type ** (int size = 0) {
    size = PyList_Size($input);
    $1 = (Type **) malloc((size + 1) * sizeof(Type *));
    int i = 0;
    for (i = 0; i < size; ++i) {
        void *argp = 0;
        int res = SWIG_ConvertPtr(PyList_GetItem($input, i), &argp, SWIGTYPE_p_##Type, 0);
        if (!SWIG_IsOK(res)) {
            SWIG_exception_fail(SWIG_ArgError(res), "array item is not of type 'Type const *'");
          }
        $1[i] = reinterpret_cast<Type *>(argp);
    }
    $1[i] = 0;
}

%typemap(freearg) Type ** { free($1); }
%enddef

%array(Concept)


/* Disown resource when sending it to the native side */
%define %disown(Function, Resource)
%feature("pythonprepend") Function {
    Resource.thisown = 0
}
%enddef

%disown(query_answer_into_rows, query_answer)
%disown(query_answer_into_documents, query_answer)


/* char** needs special handling */
%typemap(in) char ** (int size = 0) {
    if (PyList_Check($input)) {
        size = PyList_Size($input);
        int i = 0;
        $1 = (char **) malloc((size + 1) * sizeof(char *));
        for (i = 0; i < size; ++i) {
            PyObject *obj = PyList_GetItem($input, i);
            if (PyUnicode_Check(obj)) {
                const char *str = PyUnicode_AsUTF8(PyList_GetItem($input, i));
                $1[i] = (char *)malloc((strlen(str) + 1) * sizeof(char));
                strcpy($1[i], str);
            }
            else {
                PyErr_SetString(PyExc_TypeError, "list must contain strings");
                for (int j = 0; j < i; ++j)
                        free($1[j]);
                free($1);
                return NULL;
            }
        }
        $1[i] = 0;
    } else {
        PyErr_SetString(PyExc_TypeError, "not a list");
        return NULL;
    }
}

%typemap(freearg) char ** {
    for (int i = 0; i < size$argnum - 1; ++i)
        free($1[i]);
    free((char *) $1);
}


/* Introduce TypeDBDriverExceptionNative and use it to wrap all native exceptions */
%{
static PyObject* PyExc_TypeDBDriverError;
%}

%init %{
    PyExc_TypeDBDriverError = PyErr_NewException("native_driver_python.TypeDBDriverExceptionNative", NULL, NULL);
    Py_INCREF(PyExc_TypeDBDriverError);
    PyModule_AddObject(m, "TypeDBDriverExceptionNative", PyExc_TypeDBDriverError);
%}

%exception {
    $action
    if (check_error()) {
        PyErr_SetString(PyExc_TypeDBDriverError, error_message(get_last_error()));
        SWIG_fail;
    }
}

%pythoncode %{
    TypeDBDriverExceptionNative = native_driver_python.TypeDBDriverExceptionNative

    native_driver_python.init_logging()
%}
