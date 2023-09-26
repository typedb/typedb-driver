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
    val members: List<EnumMember> = listOf(),
    val methods: List<Method> = listOf(),
    val description: List<String> = listOf(),
    val examples: List<String> = listOf(),
    val bases: List<String> = listOf(),
    val packagePath: String? = null,
) {
    fun toAsciiDoc(language: String): String {
        var result = ""
        result += "[#_${this.name}]\n"
        result += "= ${this.name}\n\n"
        result += "== Description\n\n${this.description.joinToString("\n\n")}\n\n"

        if (this.examples.isNotEmpty()) {
            result += "== Code examples\n\n"
            this.examples.forEach {
                result += "[source,$language]\n----\n$it\n----\n\n"
            }
        }

        if (this.members.isNotEmpty()) {
            result += "== Enum members\n\n[options=\"header\"]\n|===\n"
            result += "|Name |Value \n"
            this.members.forEach { result += it.toAsciiDocTableRow(language) + "\n" }
            result += "|===\n\n"
        }

        if (this.methods.isNotEmpty()) {
            result += "\n== Methods\n\n"
            this.methods.forEach { result += it.toAsciiDoc(language) }
        }

        return result
    }
}
