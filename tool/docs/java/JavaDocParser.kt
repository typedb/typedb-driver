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

package com.vaticle.typedb.client.tool.doc.java

import com.vaticle.typedb.client.tool.doc.common.Argument
import com.vaticle.typedb.client.tool.doc.common.Class
import com.vaticle.typedb.client.tool.doc.common.Method
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


fun main(args: Array<String>) {
    val inputDirectoryName = args[0]
    val outputDirectoryName = args[1]

    val docsDir = Paths.get(outputDirectoryName)
    Files.createDirectory(docsDir)

    File(inputDirectoryName).walkTopDown().filter {
        it.toString().contains("/api/") && !it.toString().contains("-use")
                && !it.toString().contains("-summary") && !it.toString().contains("-tree")
                && it.toString().endsWith(".html")
    }.forEach {
        val html = File(it.path).readText(Charsets.UTF_8)
        val parsed = Jsoup.parse(html)
        if (!parsed.select("h2[title^=Interface]").isNullOrEmpty()) {
            val parsedClass = parseClass(parsed)
            println(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("java"))
        }
    }
}


fun parseClass(document: Element): Class {
    val className = document.selectFirst(".contentContainer .description pre .typeNameLabel")!!.text()
    val classDescr = document.select(".contentContainer .description pre + div").map { it.text() }
    val classBases = document.select(".contentContainer .description dt:contains(Superinterfaces) + dd code").map {
        it.text()
    }

    val fields = document.select(".summary > ul > li > section > ul > li:has(a[id=field.summary]) > table tr:gt(0)").map {
        parseField(it)
    }
    val methods = document.select(".details > ul > li > section > ul > li:has(a[id=method.detail]) > ul > li").map {
        parseMethod(it)
    }

    return Class(
        name = className,
        description = classDescr,
        methods = methods,
        fields = fields,
        bases = classBases,
    )
}

fun parseMethod(element: Element): Method {
    val methodName = element.selectFirst("h4")!!.text()
    val methodSignature = element.selectFirst(".methodSignature")!!.text()
    val allArgs = getArgsFromSignature(methodSignature)
    val methodReturnType = methodSignature.substringBefore("(").substringBeforeLast("\u00a0")
    val methodDescr: List<String> = element.selectFirst(".methodSignature + div")?.textNodes()
        ?.map { it.text() } ?: listOf()
    val methodExamples = element.select(".methodSignature + div pre").map { it.text() }
    val methodArgs = element.select("dl:has(.paramLabel) dd").map {
        val arg_name = it.select("code").text()
        assert(allArgs.contains(arg_name))
        Argument(
            name = arg_name,
            type = allArgs[arg_name],
            description = it.textNodes().joinToString().removePrefix(" - "),
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
    val propertyName = element.selectFirst(".colSecond")!!.text()
    val propertyType = element.selectFirst(".colFirst")!!.text()
    val propertyDescr = element.selectFirst(".colLast")?.text()
    return Argument(
        name = propertyName,
        type = propertyType,
        description = propertyDescr,
    )
}

fun getArgsFromSignature(methodSignature: String): Map<String, String?> {
    return methodSignature
        .substringAfter("(").substringBefore(")")
        .split(",").map {
            it.split("\u00a0").let { it.last() to it.dropLast(1).joinToString(" ") }
        }.toMap()
}
