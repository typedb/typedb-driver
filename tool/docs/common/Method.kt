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

data class Method(
    val name: String,
    val signature: String,
    val anchor: String? = null,
    val args: List<Variable> = listOf(),
    val description: List<String> = listOf(),
    val examples: List<String> = listOf(),
    val returnDescription: String? = null,
    val returnType: String? = null,
) {
    fun toAsciiDoc(language: String): String {
        var result = ""
        result += "[#_${this.anchor ?: replaceSymbolsForAnchor(this.name)}]\n"
        result += "=== ${this.name}\n\n"
        result += "==== Signature\n\n"
        result += "[source,$language]\n----\n${this.signature}\n----\n\n"
        result += "==== Description\n\n${this.description.joinToString("\n\n")}\n\n"

        if (this.args.isNotEmpty()) {
            result += "==== Input parameters\n\n"
            result += "[cols=\"~,~,~"
            if (language == "python") {
                result += ",~"
            }
            result += "\"]\n[options=\"header\"]\n|===\n"
            result += "|Name |Description |Type"
            if (language == "python") {
                result += " |Default Value"
            }
            result += "\n"
            this.args.forEach { result += it.toAsciiDocAsArgument(language) + "\n" }
            result += "|===\n\n"
        }

        result += "==== Returns\n\n"
        result += when (language) {
            "rust" -> "[source,rust]\n----\n${this.returnType}\n----\n\n"
            else -> "`${this.returnType}`\n\n"
        }

        if (this.examples.isNotEmpty()) {
            result += "==== Code examples\n\n"
            this.examples.forEach {
                result += "[source,$language]\n----\n$it\n----\n\n"
            }
        }

        return result
    }
}
