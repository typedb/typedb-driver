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

data class Class(
    val name: String,
    val fields: List<Argument> = listOf(),
    val methods: List<Method> = listOf(),
    val description: List<String> = listOf(),
    val example: String? = null,
    val bases: List<String> = listOf(),
    val packagePath: String? = null,
)

data class Method(
    val name: String,
    val signature: String,
    val description: List<String> = listOf(),
    val args: List<Argument> = listOf(),
    val returnType: String? = null,
    val returnDescription: String? = null,
    val example: String? = null,
    val defaultValue: String? = null,
)

data class Argument(
    val name: String,
    val type: String? = null,
    val description: String? = null,
)
