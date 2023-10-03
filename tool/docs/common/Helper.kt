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

fun replaceCodeTags(html: String): String {
    return Regex("<code[^>]*>").replace(html, "`").replace("</code>", "`")
}

fun replaceEmTags(html: String): String {
    return Regex("<em[^>]*>").replace(html, "_").replace("</em>", "_")
}

fun removeAllTags(html: String): String {
    return Regex("(?<!<)<[^<>]*>(?!>)").replace(html, "")
}

fun replaceSpaces(html: String): String {
    return html.replace("&nbsp;", " ").replace("\u00a0", " ")
}

fun replaceSymbols(html: String): String {
    return html.replace("&lt;", "<").replace("&gt;", ">")
        .replace("&amp;", "&")
}

fun replaceSymbolsForAnchor(name: String): String {
    return name.replace("[\\.,\\(\\)\\s#]".toRegex(), "_").removeSuffix("_")
}

fun mergeClasses(first: Class, second: Class): Class {
    assert(first.name == second.name)
    return Class(
        name = first.name,
        anchor = first.anchor ?: second.anchor,
        enumConstants = first.enumConstants + second.enumConstants,
        description = first.description.ifEmpty { second.description },
        examples = first.examples.ifEmpty { second.examples },
        fields = first.fields + second.fields,
        methods = first.methods + second.methods,
        packagePath = first.packagePath ?: second.packagePath,
        superClasses = first.superClasses.ifEmpty { second.superClasses },
    )
}
