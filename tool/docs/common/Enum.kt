/*
 *  Copyright (C) 2022 Vaticle
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.vaticle.typedb.client.tool.doc.common

data class Enum(
    val name: String,
    val constants: List<EnumConstant> = listOf(),
    val fields: List<Argument> = listOf(),
    val methods: List<Method> = listOf(),
    val description: List<String> = listOf(),
    val examples: List<String> = listOf(),
    val bases: List<String> = listOf(),
    val packagePath: String? = null,
    val anchor: String? = null,
) {
    fun toAsciiDoc(language: String): String {
        var result = ""
        result += "[#_${this.anchor ?: this.name}]\n"
        result += "= ${this.name}\n\n"

        this.packagePath?.let { result += "*Package*: `$it`\n\n" }

        result += "== Description\n\n${this.description.joinToString("\n\n")}\n\n"

        if (this.examples.isNotEmpty()) {
            result += "== Code examples\n\n"
            this.examples.forEach {
                result += "[source,$language]\n----\n$it\n----\n\n"
            }
        }

        if (this.constants.isNotEmpty()) {
            result += "== "
            result += when (language) {
                "rust" -> "Enum variants"
                else -> "Enum constants"
            }
            result += "\n\n[cols=\"~,~,~\"]\n[options=\"header\"]\n|===\n"
            result += "|Name |"
            result += when (language) {
                "rust" -> "Type \n"
                else -> "Value \n"
            }
            this.constants.forEach { result += it.toAsciiDocTableRow(language) + "\n" }
            result += "|===\n\n"
        }

        if (this.methods.isNotEmpty()) {
            result += "\n== Methods\n\n"
            this.methods.forEach { result += it.toAsciiDoc(language) }
        }

        return result
    }

    fun toJavaComment(): String {
        var result = ""
        result += "${this.name}\n\n"
        result += "/**\n     * ${this.description.map { backquotesToCode(it) }.joinToString("\n     * ")}\n"
        result += " * \n"

        if (this.examples.isNotEmpty()) {
            result += " * <h3>Examples</h3>\n"
            result += " * <pre>\n"
            this.examples.forEach {
                result += " * ${snakeToCamel(it)}\n"
            }
            result += " * </pre>\n"
        }

        return result + " */\n\n"
    }

    fun toRustComment(): String {
        var result = ""
        result += "${this.name}\n\n"

        if (this.description.isNotEmpty()) {
            result += "/// ${this.description.joinToString("\n/// ")}\n"
        }

        if (this.examples.isNotEmpty()) {
            result += "/// \n"
            result += "/// # Examples\n"
            result += "/// \n"
            result += "/// ```rust\n"
            this.examples.forEach {
                result += "/// $it\n"
            }
            result += "/// ```\n"
        }

        result += "\n"

        if (this.fields.isNotEmpty()) {
            this.fields.forEach { result += it.toRustCommentField() }
        }

        if (this.methods.isNotEmpty()) {
            this.methods.forEach { result += it.toRustComment() }
        }

        return result
    }

    fun toNodejsComment(): String {
        var result = ""
        result += "${this.name}\n\n"
        result += "/**\n * ${this.description.map { backquotesToCode(it) }.joinToString("\n * ")}\n"
        result += " * \n"

        if (this.examples.isNotEmpty()) {
            result += " * ### Examples\n"
            result += " * \n"
            result += " * ```ts\n"
            this.examples.forEach {
                result += " * ${snakeToCamel(it)}\n"
            }
            result += " * ```\n"
        }

        return result + " */\n\n"
    }
}
