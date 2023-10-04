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

data class Argument(
    val name: String,
    val anchor: String? = null,
    val defaultValue: String? = null,
    val description: String? = null,
    val type: String? = null,
) {
    fun toAsciiDocPage(language: String): String {
        var result = ""
        result += "a| `${this.name}` "
        result += "a| `${this.type}` "
        result += "a| ${this.description ?: ""}\n"
        return result
    }

    fun toAsciiDocTableRow(language: String): String {
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

//    fun toJavaCommentArg(): String {
//        var result = ""
//        result += "     * @param ${snakeToCamel(this.name)} ${backquotesToCode(this.description)}\n"
//        return result
//    }
//
//    fun toJavaCommentField(): String {
//        var result = ""
//        result += "${snakeToCamel(this.name)}\n\n"
//        result += "    /**\n     * ${backquotesToCode(this.description)}\n"
//        return "$result     */\n\n"
//    }
//
//    fun toRustCommentArg(): String {
//        var result = ""
//        result += "    /// * `${this.name}` -- ${this.description}\n"
//        return result
//    }
//
//    fun toRustCommentField(): String {
//        var result = ""
//        result += "${this.name}\n\n"
//        result += "    /// ${this.description}\n"
//        return "$result\n"
//    }
//
//    fun toNodejsCommentArg(): String {
//        var result = ""
//        result += "     * @param ${snakeToCamel(this.name)} - ${backquotesToCode(this.description)}\n"
//        return result
//    }
//
//    fun toNodejsCommentField(): String {
//        var result = ""
//        result += "${snakeToCamel(this.name)}\n\n"
//        result += "    /**\n     * ${backquotesToCode(this.description)}\n"
//        return "$result     */\n\n"
//    }
}

fun backquotesToCode(text: String?): String? {
    return text?.let { Regex("`([^`]*)`").replace(text, "<code>$1</code>") }
}

fun snakeToCamel(text: String): String {
    val words = text.split("_")
    return words.first() + words.drop(1).joinToString("") { it.capitalize() }
}
