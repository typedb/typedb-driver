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

package com.vaticle.typedb.client.tool.doc.nodejs

import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import com.vaticle.typedb.client.tool.doc.common.Argument
import com.vaticle.typedb.client.tool.doc.common.Class
import com.vaticle.typedb.client.tool.doc.common.Method

fun main(args: Array<String>) {
    val outputFilename = args[0]
    val inputDirectoryName = args[1]
//    println("Input: $inputDirectoryName")

    val outputFile = File(outputFilename)
    outputFile.createNewFile()

    File(inputDirectoryName).walkTopDown().forEach {
        if (it.toString().contains("/classes/") || it.toString().contains("/interfaces/")) {
//            println(it)
            val html = it.readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)

            val title = parsed.select(".tsd-page-title h1")
            if (!title.isNullOrEmpty() && (title.text().contains("Class") || title.text().contains("Interface"))) {
                val parsedClass = parseClass(parsed)
                if (parsedClass.name == "User") {
                    println(parsedClass)
                }
                outputFile.appendText(parsedClass.toString() + "\n")
            }
        }
    }
}

fun parseClass(document: Element): Class {
    val className = document.selectFirst(".tsd-page-title h1")!!.text().split(" ")[1]
    val classDescr = document.select(".tsd-page-title + section.tsd-comment div.tsd-comment p").map { it.html() }

    val classBases = document.select("ul.tsd-hierarchy li:has(ul.tsd-hierarchy span.target)").map {
        it.child(0).text()
    }

    val propertiesElements = document.select("section.tsd-member-group:contains(Properties)")
    val properties = propertiesElements.select("section.tsd-member").map {
        parseProperty(it)
    }

    val methodsElements = document.select("section.tsd-member-group:contains(Method)")
    val methods = methodsElements.select("section.tsd-member").map {
        parseMethod(it)
    }

    return Class(
        name = className,
        description = classDescr,
        methods = methods,
        fields = properties,
        bases = classBases,
    )
}

fun parseMethod(element: Element): Method {
    val methodSignature = element.selectFirst(".tsd-signatures .tsd-signature")!!.text()
    val methodName = element.selectFirst(".tsd-signatures .tsd-signature .tsd-kind-call-signature")!!.text()
    val methodReturnType = element.select(".tsd-signatures .tsd-description .tsd-returns-title > *").map {
        it.text()
    }.joinToString("")
    val methodDescr = element.select(".tsd-description > .tsd-comment p").map { it.html() }
    val methodExamples = element.select(".tsd-description > .tsd-comment > :has(a[href*=examples]) + pre > :not(button)")
        .map { it.text() }

    val methodArgs = element.select(".tsd-description .tsd-parameters .tsd-parameter-list > li").map {
        Argument(
            name = it.selectFirst(".tsd-kind-parameter")!!.text(),
            type = it.selectFirst(".tsd-signature-type")?.text(),
            description = it.selectFirst(".tsd-comment")?.text(),
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

fun parseProperty(element: Element): Argument {
    val propertyName = element.selectFirst(".tsd-signature span.tsd-kind-property")!!.text()
    val propertyType = element.selectFirst(".tsd-signature span.tsd-signature-type")?.text()
    return Argument(
        name = propertyName,
        type = propertyType,
    )
}
