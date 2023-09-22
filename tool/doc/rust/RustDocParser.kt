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

fun main(args: Array<String>) {
    val outputFilename = args[0]
    val inputDirectoryName = args[1]

    val outputFile = File(outputFilename)
    outputFile.createNewFile()

    File(inputDirectoryName).walkTopDown().forEach {
        println(it)
        if (it.toString().contains("struct.")) {
            val html = it.readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)
            if (!parsed.select(".main-heading h1 a.struct").isNullOrEmpty()) {
                val parsedClass = parseClass(parsed)
                print(parsedClass)
                outputFile.appendText(parsedClass.toString() + "\n")
            }
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

    val methodsDetails = document.select("details[class*=method-toggle]")
    val methods = mutableListOf<Method>()
    methodsDetails.forEach {
        if (!it.select("summary section.method").isNullOrEmpty()) {
            methods.add(parseMethod(it))
        }
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
    val docblocks = element.select("div.docblock")
    var methodDescr = listOf<String>()
    val methodArgs = mutableListOf<Argument>()
    var methodExamples: List<String> = listOf()
    if (!docblocks.isNullOrEmpty()) {   // We store arguments info only if their comments are specified
        val methodComment = docblocks.first()!!
        methodDescr = methodComment.select("p").map { it.html() }
        methodComment.select("ul li code:eq(0)").forEach {
            val arg_name = it.text()
            assert(allArgs.contains(arg_name))
            val arg_descr = it.parent()!!.text().removePrefix(it.text()).removePrefix(" – ")
            methodArgs.add(Argument(name = arg_name, type = allArgs[arg_name], description = arg_descr))
        }
        methodExamples = methodComment.select("div.example-wrap pre").map { it.text() }
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
        .dropWhile { it != '(' }.drop(1)
        .dropLastWhile { it != ')' }.dropLast(1)
        .split(", ").map {
            if (it.contains(": ")) it.split(": ", limit = 2).let { it[0] to it[1] } else it to null
        }.toMap()
}
