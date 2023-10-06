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

package com.vaticle.typedb.client.tool.doc.common

data class Variable(
    val name: String,
    val anchor: String? = null,
    val defaultValue: String? = null,
    val description: String? = null,
    val type: String? = null,
) {
    fun toAsciiDocAsField(language: String): String {
        var result = ""
        result += "a| `${this.name}` "
        result += "a| `${this.type?.replace("|", "\\|")}` "
        result += "a| ${this.description ?: ""}\n"
        return result
    }

    fun toAsciiDocAsArgument(language: String): String {
        var result = ""
        result += "a| `${this.name}` "
        result += "a| ${this.description} "
        result += "a| "
        this.type?.let { result += "`${it.replace("|", "\\|")}` " }
        if (language == "python") {
            result += "a| "
            this.defaultValue?.let { result += "`$it`" }
        }
        return result
    }
}
