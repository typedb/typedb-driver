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

package com.typedb.driver.tool.docs.dataclasses

import com.typedb.driver.tool.docs.adoc.AsciiDocBuilder
import com.typedb.driver.tool.docs.adoc.AsciiDocTableBuilder
import com.typedb.driver.tool.docs.util.replaceSymbolsForAnchor

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
        val traitImplementors: List<String> = listOf(),
        val mode: String? = null,
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
            mode = this.mode ?: other.mode,
        )
    }

    fun isNotEmpty(): Boolean {
        return enumConstants.isNotEmpty()
                || description.isNotEmpty()
                || examples.isNotEmpty()
                || fields.isNotEmpty()
                || methods.isNotEmpty()
                || superClasses.isNotEmpty()
    }

    fun nameDescriptor(language: String): String {
        return when (language) {
            "rust" -> {
                if (this.enumConstants.isNotEmpty()) {
                    "enum"
                } else {
                    "struct"
                }
            }
            else -> "class"
        }
    }

    fun toAsciiDoc(language: String, mergeWith: Class? = null, headerLevel: Int = 3): String {
        val builder = AsciiDocBuilder()
        var result = ""
        result += builder.anchor(this.anchor ?: replaceSymbolsForAnchor(this.name))
        result += builder.header(headerLevel,  this.name)
        result += builder.caption(this.nameDescriptor(language))

        this.packagePath?.let { result += "*Package*: `$it`\n\n" }

        if (this.superClasses.isNotEmpty()) {
            result += builder.boldHeader(
                when (language) {
                    "java" -> "Superinterfaces:"
                    "rust" -> "Implements traits:"
                    else -> "Supertypes:"
                }
            )
            result += builder.unorderedList(this.superClasses)
            result += "\n\n"
        }

        if (this.traitImplementors.isNotEmpty()) {
            result += builder.boldHeader("Implementors:")
            result += builder.unorderedList(this.traitImplementors)
            result += "\n\n"
        }

        val thisDescriptionExample = (if (this.description.isNotEmpty()) "${this.description.joinToString("\n\n")}\n\n" else "") +
                        (if (this.examples.isNotEmpty()) "${builder.caption("Examples")}${this.examples.joinToString { builder.codeBlock(it, language) }}" else "")

        if (mergeWith != null) {
            val mergeDescriptionExample =  (if (mergeWith.description.isNotEmpty()) "${mergeWith.description.joinToString("\n\n")}\n\n" else "") +
                    (if (mergeWith.examples.isNotEmpty()) "${builder.caption("Examples")}${mergeWith.examples.joinToString { builder.codeBlock(it, language) }}" else "")
            result += builder.tabsIfNotEqual(listOf(
                    Pair(this.mode!!, thisDescriptionExample),
                    Pair(mergeWith.mode!!, mergeDescriptionExample),
            ))
        } else {
            result += thisDescriptionExample
        }

        if (this.enumConstants.isNotEmpty()) {
            result += builder.caption(
                when (language) {
                    "rust" -> "Enum variants"
                    "nodejs" -> "Namespace variables"
                    else -> "Enum constants"
                }
            )
            result += builder.tagBegin("enum_constants")

            val headers = when (language) {
                "rust" -> listOf("Variant")
                "python" -> listOf("Name", "Value")
                else -> listOf("Name")
            }

            val tableBuilder = AsciiDocTableBuilder(headers)
            this.enumConstants.sortedBy { it.name }.forEach { tableBuilder.addRow(it.toTableData(language)) }
            result += tableBuilder.build()

            result += builder.tagEnd("enum_constants")
        } else if (this.fields.isNotEmpty()) {
            result += builder.caption(
                when (language) {
                    "python" -> "Properties"
                    else -> "Fields"
                }
            )
            result += builder.tagBegin("properties")

            val headers = listOf("Name", "Type", "Description")
            val tableBuilder = AsciiDocTableBuilder(headers)
            this.fields.sortedBy { it.name }.forEach { tableBuilder.addRow(it.toTableDataAsField(language)) }
            result += tableBuilder.build()

            result += builder.tagEnd("properties")
        }

        if (this.methods.isNotEmpty()) {
            result += builder.tagBegin("methods")
            if (mergeWith == null) {
                this.methods.sortedBy { it.name }.forEach { result += it.toAsciiDoc(language, headerLevel + 1) }
            } else {
                val mergeMethods = mergeWith.methods.associateBy {
                    it.name
                }
                this.methods.sortedBy { it.name }.forEach { result += it.toAsciiDocFeaturesMerged(mergeMethods[it.name]!!) }
            }
            result += builder.tagEnd("methods")
        }

        return result
    }
}
