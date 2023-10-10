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

data class Method(
    val name: String,
    val signature: String,
    val anchor: String? = null,
    val args: List<Variable> = listOf(),
    val description: List<String> = listOf(),
    val examples: List<String> = listOf(),
    val mode: String? = null,
    val returnDescription: String? = null,
    val returnType: String? = null,
) {
    fun toAsciiDoc(language: String): String {
        val builder = AsciiDocBuilder()
        var result = ""
        result += builder.anchor(this.anchor ?: replaceSymbolsForAnchor(this.name))
        result += builder.header(4, this.name)
        result += builder.codeBlock(this.signature, language)
        result += "${this.description.joinToString("\n\n")}\n\n"

        if (this.args.isNotEmpty()) {
            result += builder.caption("Input parameters")

            val headers = mutableListOf("Name", "Description", "Type")
            if (language == "python") {
                headers.add("Default Value")
            }
            val tableBuilder = AsciiDocTableBuilder(headers)
            result += tableBuilder.header()
            result += tableBuilder.body(this.args.map { it.toTableDataAsArgument(language) })
            result += "\n"
        }

        result += builder.caption("Returns")
        result += when (language) {
            "rust" -> builder.codeBlock(this.returnType, language)
            else -> "`${this.returnType}`\n\n"
        }

        if (this.examples.isNotEmpty()) {
            result += builder.caption("Code examples")
            this.examples.forEach {
                result += builder.codeBlock(it, language)
            }
        }

        return result
    }

    fun toAsciiDocFeaturesMerged(vararg other_methods: Method): String {
        val methods = other_methods.toList() + listOf(this)
        val builder = AsciiDocBuilder()
        val language = "rust"
        var result = ""
        result += builder.anchor(this.anchor ?: replaceSymbolsForAnchor(this.name))
        result += builder.header(4, this.name)

        result += builder.tabsIfNotEqual(methods.map {
            Pair(it.mode!!, builder.codeBlock(it.signature, language))
        })

        result += builder.tabsIfNotEqual(methods.map {
            Pair(it.mode!!, "${it.description.joinToString("\n\n")}\n\n")
        })

        if (this.args.isNotEmpty()) {
            result += builder.caption("Input parameters")
            val tableBuilder = AsciiDocTableBuilder(listOf("Name", "Description", "Type"))
            result += tableBuilder.header()
            result += builder.tabsIfNotEqual(methods.map {
                Pair(it.mode!!, tableBuilder.body(it.args.map { it.toTableDataAsArgument(language) }))
            })
            result += "\n"
        }

        result += builder.caption("Returns")
        result += builder.tabsIfNotEqual(methods.map {
            Pair(it.mode!!, builder.codeBlock(it.returnType, language))
        })

        if (this.examples.isNotEmpty()) {
            result += builder.caption("Code examples")
            result += builder.tabsIfNotEqual(methods.map {
                Pair(it.mode!!, it.examples.joinToString("") { builder.codeBlock(it, language) })
            })
        }

        return result
    }
}
