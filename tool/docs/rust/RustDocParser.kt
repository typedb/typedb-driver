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

package com.vaticle.typedb.client.tool.doc.rust

import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import com.vaticle.typedb.client.tool.doc.common.Argument
import com.vaticle.typedb.client.tool.doc.common.Class
import com.vaticle.typedb.client.tool.doc.common.Enum
import com.vaticle.typedb.client.tool.doc.common.EnumConstant
import com.vaticle.typedb.client.tool.doc.common.Method
import com.vaticle.typedb.client.tool.doc.common.removeAllTags
import com.vaticle.typedb.client.tool.doc.common.replaceCodeTags
import com.vaticle.typedb.client.tool.doc.common.replaceEmTags
import com.vaticle.typedb.client.tool.doc.common.replaceSpaces
import com.vaticle.typedb.client.tool.doc.common.replaceSymbols
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val inputDirectoryName = args[0]
    val outputDirectoryName = args[1]

    val docsDir = Paths.get(outputDirectoryName)
    Files.createDirectory(docsDir)

    File(inputDirectoryName).walkTopDown().filter {
        it.toString().contains("struct.") || it.toString().contains("trait.") || it.toString().contains("enum.")
    }.forEach {
        val html = it.readText(Charsets.UTF_8)
        val parsed = Jsoup.parse(html)
        if (!parsed.select(".main-heading h1 a.struct").isNullOrEmpty()) {
            val parsedClass = parseClass(parsed)
//            print(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("rust"))
        } else if (!parsed.select(".main-heading h1 a.trait").isNullOrEmpty()) {
            val parsedClass = parseTrait(parsed)
//            print(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("rust"))
        } else if (!parsed.select(".main-heading h1 a.enum").isNullOrEmpty()) {
            val parsedClass = parseEnum(parsed)
            print(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("rust"))
        }
    }
}

fun parseClass(document: Element): Class {
    val className = document.selectFirst(".main-heading h1 a.struct")!!.text()
    val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

    val fields = document.select(".structfield").map {
        parseField(it)
    }

    val methods = document.select("#implementations-list details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it)
    }

    return Class(
        name = className,
        description = classDescr,
        fields = fields,
        methods = methods,
    )
}

fun parseTrait(document: Element): Class {
    val className = document.selectFirst(".main-heading h1 a.trait")!!.text()
    val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

    val methods = document.select("#provided-methods + .methods details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it)
    }

    return Class(
        name = "Trait $className",
        description = classDescr,
        methods = methods,
    )
}

fun parseEnum(document: Element): Enum {
    val className = document.selectFirst(".main-heading h1 a.enum")!!.text()
    val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

//    val fields = document.select(".structfield").map {
//        parseField(it)
//    }

    val variants = document.select("section.variant").map {
        parseEnumConstant(it)
    }

    val methods = document.select("#implementations-list details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it)
    }

    return Enum(
        name = className,
        description = classDescr,
        constants = variants,
        methods = methods,
    )
}

fun parseMethod(element: Element): Method {
    val methodSignature = enhanceSignature(element.selectFirst("summary section h4")!!.html())
    val methodName = element.selectFirst("summary section h4 a.fn")!!.text()
    val allArgs = getArgsFromSignature(methodSignature)
    val methodReturnType = if (methodSignature.contains(" -> ")) methodSignature.split(" -> ").last() else null
    val methodDescr = element.select("div.docblock p").map { reformatTextWithCode(it.html()) }
    val methodExamples = element.select("div.docblock div.example-wrap pre").map { it.text() }
    val methodArgs = element.select("div.docblock ul li code:eq(0)").map {
        val argName = it.text().trim()
        assert(allArgs.contains(argName))
        val argDescr = reformatTextWithCode(removeArgName(it.parent()!!.html())).removePrefix(" – ")
        Argument(
            name = argName,
            type = allArgs[argName]?.let { it.trim() },
            description = argDescr
        )
    }

    return Method(
        name = methodName,
        signature = methodSignature,
        description = methodDescr,
        args = methodArgs,
        returnType = methodReturnType,
        examples = methodExamples,
    )

}

fun parseField(element: Element): Argument {
    val nameAndType = element.selectFirst("code")!!.text().split(": ")
    val descr = element.nextElementSibling()?.html()?.let { reformatTextWithCode(it) }
    return Argument(
        name = nameAndType[0],
        type = nameAndType[1],
        description = descr,
    )
}

fun parseEnumConstant(element: Element): EnumConstant {
    val nameAndType = element.selectFirst("h3")!!.text()
    return EnumConstant(
        name = nameAndType.substringBefore("("),
        type = nameAndType.substringAfter("(").substringBefore(")"),
    )
}

fun getArgsFromSignature(methodSignature: String): Map<String, String?> {
//    Splitting by ", " is incorrect (could be used in the type)
    return methodSignature
        .substringAfter("(").substringBeforeLast(")")
        .split(",\\s".toRegex()).associate {
            if (it.contains(":\\s".toRegex())) it.split(":\\s".toRegex(), limit = 2)
                .let { it[0].trim() to it[1].trim() } else it.trim() to null
        }
}

fun reformatTextWithCode(html: String): String {
    return removeAllTags(replaceEmTags(replaceCodeTags(html)))
}

fun enhanceSignature(signature: String): String {
    return replaceSymbols(removeAllTags(replaceSpaces(dispatchNewlines(signature))))
}

fun dispatchNewlines(html: String): String {
    return Regex("<span[^>]*newline[^>]*>").replace(html, "\n")
}

fun removeArgName(html: String): String {
    return Regex("<code>[^<]*</code>").replaceFirst(html, "")
}

