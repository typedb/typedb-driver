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
) {
    fun toAsciiDoc(language: String): String {
        var result = ""
        result += "[#_${this.name}]\n"
        result += "== ${this.name}\n\n"
        result += "=== Signature\n\n"
        result += "[source,$language]\n----\n${this.signature}\n----\n\n"
        result += "=== Description\n\n${this.description.joinToString("\n\n")}\n\n"

        if (this.args.isNotEmpty()) {
            result += "=== Input parameters\n\n[options=\"header\"]\n|===\n"
            result += "|Name |Description |Type |Default Value\n"
            this.args.forEach { result += it.toAsciiDocTableRow(language) + "\n" }
            result += "|===\n\n"
        }

        result += "=== Returns\n\n${this.returnType}\n\n"

        if (this.examples.isNotEmpty()) {
            result += "=== Code examples\n\n"
            this.examples.forEach {
                result += "[source,$language]\n----\n$it\n----\n\n"
            }
        }

        return result
    }
}
