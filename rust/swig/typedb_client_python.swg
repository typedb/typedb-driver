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

#include "string.h"

%typemap(in) char ** (int size) {
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
