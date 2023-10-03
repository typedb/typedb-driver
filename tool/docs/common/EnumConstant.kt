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

data class EnumConstant(
    val name: String,
    val type: String? = null,
    val value: String? = null,
) {
    fun toAsciiDocTableRow(language: String): String {
        var result = ""
        result += "a| `${this.name}` "
        if (language == "python" || language == "rust") {
            result += "a| "
            assert(this.type == null || this.value == null)
            this.value?.let { result += "`$it`" }
            this.type?.let { result += "`$it`" }
        }
        return result + "\n"
    }
}
