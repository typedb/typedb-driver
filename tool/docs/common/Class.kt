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

data class Class(
    val name: String,
    val anchor: String? = null,
    val enumConstants: List<EnumConstant> = listOf(),
    val description: List<String> = listOf(),
    val examples: List<String> = listOf(),
    val fields: List<Variable> = listOf(),
    val methods: List<Method> = listOf(),
    val packagePath: String? = null,
    val superClasses: List<String> = listOf(),
) {
    fun toAsciiDoc(language: String): String {
        var result = ""
        result += "[#_${this.anchor ?: replaceSymbolsForAnchor(this.name)}]\n"
        result += "= ${this.name}\n\n"

        this.packagePath?.let { result += "*Package*: `$it`\n\n" }

        if (this.superClasses.isNotEmpty()) {
            result += when (language) {
                "java" -> "*Superinterfaces:*\n\n"
                "rust" -> "*Implements traits:*\n\n"
                else -> "*Supertypes:*\n\n"
            }
            result += this.superClasses.joinToString("\n") { "* `$it`" }
            result += "\n\n"
        }

        if (this.description.isNotEmpty()) {
            result += "== Description\n\n${this.description.joinToString("\n\n")}\n\n"
        }

        if (this.examples.isNotEmpty()) {
            result += "== Code examples\n\n"
            this.examples.forEach {
                result += "[source,$language]\n----\n$it\n----\n\n"
            }
        }

        if (this.enumConstants.isNotEmpty()) {
            result += "// tag::enum_constants[]\n"
            result += "== "
            result += when (language) {
                "rust" -> "Enum variants"
                "nodejs" -> "Namespace variables"
                else -> "Enum constants"
            }
            result += "\n\n[options=\"header\"]\n|===\n"
            result += "|Name "
            result += when (language) {
                "rust" -> "|Type "
                "python" -> "|Value "
                else -> ""
            } + "\n"
            this.enumConstants.forEach { result += it.toAsciiDocTableRow(language) }
            result += "|===\n"
            result += "// end::enum_constants[]\n\n"
        } else if (this.fields.isNotEmpty()) {
            result += "== " + when (language) {
                "python" -> "Properties"
                else -> "Fields"
            } + "\n\n"
            result += "// tag::properties[]\n"
            result += "[cols=\"~,~,~\"]\n[options=\"header\"]\n|===\n"
            result += "|Name |Type |Description\n"
            this.fields.forEach { result += it.toAsciiDocAsField(language) }
            result += "|===\n"
            result += "// end::properties[]\n\n"
        }

        if (this.methods.isNotEmpty()) {
            result += "== Methods\n\n"
            result += "// tag::methods[]\n"
            this.methods.forEach { result += it.toAsciiDoc(language) }
            result += "// end::methods[]\n"
        }

        return result
    }

    fun isNotEmpty(): Boolean {
        return enumConstants.isNotEmpty()
                || description.isNotEmpty()
                || examples.isNotEmpty()
                || fields.isNotEmpty()
                || methods.isNotEmpty()
                || superClasses.isNotEmpty()
    }
}
