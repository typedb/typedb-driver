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

package com.vaticle.typedb.client.common.exception;

import javax.annotation.Nullable;

public class TypeDBClientException extends RuntimeException {

    @Nullable
    private final ErrorMessage errorMessage;

    public TypeDBClientException(ErrorMessage error, Object... parameters) {
        super(error.message(parameters));
        assert !getMessage().contains("%s");
        this.errorMessage = error;
    }

    public TypeDBClientException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = null;
    }

    public String getName() {
        return this.getClass().getName();
    }

    @Nullable
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
