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
import com.vaticle.typedb.client.tool.doc.common.Method
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val inputDirectoryName = args[0]
    val outputDirectoryName = args[1]

    val docsDir = Paths.get(outputDirectoryName)
    Files.createDirectory(docsDir)

    File(inputDirectoryName).walkTopDown().filter { it.toString().contains("struct.") }.forEach {
        val html = it.readText(Charsets.UTF_8)
        val parsed = Jsoup.parse(html)
        if (!parsed.select(".main-heading h1 a.struct").isNullOrEmpty()) {
            val parsedClass = parseClass(parsed)
            print(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("rust"))
        }
    }
}

fun parseClass(document: Element): Class {
    val class_name = document.selectFirst(".main-heading h1 a.struct")!!.text()
    val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

    val fields = document.select(".structfield code").map {
        val row = it.text().split(": ")
        Argument(name = row[0], type = row[1])
    }

    val methods = document.select("details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it)
    }

    return Class(
        name = class_name,
        description = classDescr,
        fields = fields,
        methods = methods,
    )
}

fun parseMethod(element: Element): Method {
    val methodName = element.select("summary section.method").first()!!.attr("id").removePrefix("method.")
    val methodSignature = element.select("summary section h4").first()!!.text()
//    Splitting by ", " is incorrect (could be used in the type)
    val allArgs = getArgsFromSignature(methodSignature)
    val methodReturnType = if (methodSignature.contains(" -> ")) methodSignature.split(" -> ").last() else null
    val methodDescr = element.select("div.docblock p").map { it.html() }
    val methodExamples = element.select("div.docblock div.example-wrap pre").map { it.text() }
    val methodArgs = element.select("div.docblock ul li code:eq(0)").map {
        val argName = it.text()
        assert(allArgs.contains(argName))
        val argDescr = it.parent()!!.text().removePrefix(it.text()).removePrefix(" – ")
        Argument(
            name = argName,
            type = allArgs[argName],
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

fun getArgsFromSignature(methodSignature: String): Map<String, String?> {
    return methodSignature
        .substringAfter("(").substringBeforeLast(")")
        .split(", ").associate {
            if (it.contains(": ")) it.split(": ", limit = 2).let { it[0] to it[1] } else it to null
        }
}
