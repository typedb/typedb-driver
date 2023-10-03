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

data class Method(
    val name: String,
    val signature: String,
    val description: List<String> = listOf(),
    val args: List<Argument> = listOf(),
    val returnType: String? = null,
    val returnDescription: String? = null,
    val examples: List<String> = listOf(),
    val anchor: String? = null,
) {
    fun toAsciiDoc(language: String): String {
        var result = ""
        result += "[#_${this.anchor ?: this.name}]\n"
        result += "=== ${this.name}\n\n"
        result += "==== Signature\n\n"
        result += "[source,$language]\n----\n${this.signature}\n----\n\n"
        result += "==== Description\n\n${this.description.joinToString("\n\n")}\n\n"

        if (this.args.isNotEmpty()) {
            result += "==== Input parameters\n\n[cols=\"~,~,~\"]\n[options=\"header\"]\n|===\n"
            result += "|Name |Description |Type"
            if (language == "python") {
                result += " |Default Value"
            }
            result += "\n"
            this.args.forEach { result += it.toAsciiDocTableRow(language) + "\n" }
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

    fun toJavaComment(): String {
        var result = ""
        result += "${this.name}\n\n"
        result += "    /**\n     * ${this.description.map { backquotesToCode(it) }.joinToString("\n     * ")}\n"
        result += "     * \n"

        if (this.examples.isNotEmpty()) {
            result += "     * <h3>Examples</h3>\n"
            result += "     * <pre>\n"
            this.examples.forEach {
                result += "     * ${snakeToCamel(it)}\n"
            }
            result += "     * </pre>\n"
        }

        if (this.args.isNotEmpty()) {
            result += "     * \n"
            this.args.forEach { result += it.toJavaCommentArg() }
        }

        return result + "     */\n\n"
    }

    fun toRustComment(): String {
        var result = ""
        result += "${this.name}\n\n"
        result += "    /// ${this.description.joinToString("\n    /// ")}\n"

        if (this.args.isNotEmpty()) {
            result += "    /// \n"
            result += "    /// # Arguments\n"
            result += "    /// \n"
            this.args.forEach { result += it.toRustCommentArg() }
        }

        if (this.examples.isNotEmpty()) {
            result += "    /// \n"
            result += "    /// # Examples\n"
            result += "    /// \n"
            result += "    /// ```rust\n"
            this.examples.forEach {
                result += "    #[cfg_attr(feature = \"sync\", doc = \"$it\")]\n"
            }
            this.examples.forEach {
                result += "    #[cfg_attr(not(feature = \"sync\"), doc = \"${it}.await\")]\n"
            }
            this.examples.forEach {
                result += "    /// $it\n"
            }
            result += "    /// ```\n"
        }

        return result + "\n"
    }

    fun toNodejsComment(): String {
        var result = ""
        result += "${this.name}\n\n"
        result += "    /**\n     * ${this.description.map { backquotesToCode(it) }.joinToString("\n     * ")}\n"
        result += "     * \n"

        if (this.examples.isNotEmpty()) {
            result += "     * ### Examples\n"
            result += "     * \n"
            result += "     * ```ts\n"
            this.examples.forEach {
                result += "     * ${snakeToCamel(it)}\n"
            }
            result += "     * ```\n"
        }

        if (this.args.isNotEmpty()) {
            result += "     * \n"
            this.args.forEach { result += it.toNodejsCommentArg() }
        }

        return result + "     */\n\n"
    }
}
