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

package com.vaticle.typedb.driver.tool.doc.common

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
    fun merge(other: Class): Class {
        assert(this.name == other.name)
        return Class(
            name = this.name,
            anchor = this.anchor ?: other.anchor,
            enumConstants = this.enumConstants + other.enumConstants,
            description = this.description.ifEmpty { other.description },
            examples = this.examples.ifEmpty { other.examples },
            fields = this.fields + other.fields,
            methods = this.methods + other.methods,
            packagePath = this.packagePath ?: other.packagePath,
            superClasses = this.superClasses.ifEmpty { other.superClasses },
        )
    }

    fun toAsciiDoc(language: String): String {
        val builder = AsciiDocBuilder()
        var result = ""
        result += builder.anchor(this.anchor ?: replaceSymbolsForAnchor(this.name))
        result += builder.header(3, this.name)

        this.packagePath?.let { result += "*Package*: `$it`\n\n" }

        if (this.superClasses.isNotEmpty()) {
            result += builder.boldHeader(when (language) {
                "java" -> "Superinterfaces:"
                "rust" -> "Implements traits:"
                else -> "Supertypes:"
            })
            result += this.superClasses.joinToString("\n") { "* `$it`" }
            result += "\n\n"
        }

        if (this.description.isNotEmpty()) {
            result += "${this.description.joinToString("\n\n")}\n\n"
        }

        if (this.examples.isNotEmpty()) {
            result += builder.captionedBlock("${this.name} examples",
                this.examples.joinToString {
                    builder.codeBlock(it, language)
                }
            )
        }

        if (this.enumConstants.isNotEmpty()) {
            result += builder.caption(when (language) {
                "rust" -> "Enum variants"
                "nodejs" -> "Namespace variables"
                else -> "Enum constants"
            })
            result += builder.tagBegin("enum_constants")

            val headers = when (language) {
                "rust" -> listOf("Name", "Type")
                "python" -> listOf("Name", "Value")
                else -> listOf("Name")
            }
            val tableBuilder = AsciiDocTableBuilder(headers)
            result += tableBuilder.header()
            result += tableBuilder.body(this.enumConstants.map { it.toTableData(language) })
            result += builder.tagEnd("enum_constants")
        } else if (this.fields.isNotEmpty()) {
            result += builder.caption(when (language) {
                "python" -> "Properties"
                else -> "Fields"
            })
            result += builder.tagBegin("properties")

            val headers = listOf("Name", "Type", "Description")
            val tableBuilder = AsciiDocTableBuilder(headers)
            result += tableBuilder.header()
            result += tableBuilder.body(this.fields.map { it.toTableDataAsField(language) })

            result += builder.tagEnd("properties")
        }

        if (this.methods.isNotEmpty()) {
            result += builder.tagBegin("methods")
            this.methods.forEach { result += it.toAsciiDoc(language) }
            result += builder.tagEnd("methods")
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
