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

package com.vaticle.typedb.client.tool.doc.python

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import com.vaticle.typedb.client.tool.doc.common.Argument
import com.vaticle.typedb.client.tool.doc.common.Class
import com.vaticle.typedb.client.tool.doc.common.Enum
import com.vaticle.typedb.client.tool.doc.common.EnumMember
import com.vaticle.typedb.client.tool.doc.common.Method

fun main(args: Array<String>) {
    val inputDirectoryName = args[0]
    val outputDirectoryName = args[1]

    val docsDir = Paths.get(outputDirectoryName)
    Files.createDirectory(docsDir)

    File(inputDirectoryName).walkTopDown().filter {
        it.toString().endsWith(".html") &&
                (it.toString().contains(".api.") || it.toString().contains(".common."))
    }.forEach {
        println(it)
        val html = it.readText(Charsets.UTF_8)
        val parsed = Jsoup.parse(html)

        parsed.select("dl.class").forEach {
            if (it.selectFirst("dt.sig-object + dd > p")!!.text().contains("Enum")) {
                val parsedClass = parseEnum(it)
                println(parsedClass)
                val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
                outputFile.createNewFile()
                outputFile.writeText(parsedClass.toAsciiDoc("python"))

            } else {
                val parsedClass = parseClass(it)
                println(parsedClass)
                val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
                outputFile.createNewFile()
                outputFile.writeText(parsedClass.toAsciiDoc("python"))
            }
        }
    }
}

fun parseClass(element: Element): Class {
    val classSigElement = element.selectFirst("dt.sig-object")
    val className = classSigElement!!.selectFirst("dt.sig-object span.sig-name")!!.text()

    val classDetails = classSigElement.nextElementSibling()
    val classDetailsParagraphs = classDetails!!.children().map { it }.filter { it.tagName() == "p" }  // FIXME
    val (descr, bases) = classDetailsParagraphs.partition { it.select("code.py-class").isNullOrEmpty() }
    val classBases = bases[0]!!.select("span").map { it.html() }
    val classDescr = descr.map { textWithCode(it.html()) }

    val classExamples = element.select("dl.class > dt.sig-object + dd > section:contains(Example) > div > .highlight").map { it.text() }

    val methods = classDetails.select("dl.method")
        .map { parseMethod(it) }

    val properties = classDetails.select("dl.property")
        .map { parseProperty(it) }

    return Class(
        name = className,
        description = classDescr,
        methods = methods,
        fields = properties,
        bases = classBases,
        examples = classExamples,
    )
}

fun parseEnum(element: Element): Enum {
    val classSigElement = element.selectFirst("dt.sig-object")
    val className = classSigElement!!.selectFirst("dt.sig-object span.sig-name")!!.text()

    val classDetails = classSigElement.nextElementSibling()
    val classDetailsParagraphs = classDetails!!.children().map { it }.filter { it.tagName() == "p" }
    val (descr, bases) = classDetailsParagraphs.partition { it.select("code.py-class").isNullOrEmpty() }
    val classBases = bases[0]!!.select("span").map { it.html() }
    val classDescr = descr.map { textWithCode(it.html()) }

    val classExamples = element.select("section:contains(Examples) .highlight").map { it.text() }

    val methods = classDetails.select("dl.method")
        .map { parseMethod(it) }

    val members = classDetails.select("dl.attribute")
        .map { parseEnumMember(it) }

    return Enum(
        name = className,
        description = classDescr,
        methods = methods,
        members = members,
        bases = classBases,
        examples = classExamples,
    )
}

fun parseMethod(element: Element): Method {
    val methodSignature = element.selectFirst("dt.sig-object")!!.text()
    val methodName = element.selectFirst("dt.sig-object span.sig-name")!!.text()
    val allArgs = getArgsFromSignature(element.selectFirst("dt.sig-object")!!)
    val methodReturnType = element.select(".sig-return-typehint").text()
    val methodDescr = element.select("dl.method > dd > p").map { textWithCode(it.html()) }
    val methodArgs = element.select(".field-list > dt:contains(Parameters) + dd p").map {
        val arg_name = it.selectFirst("strong")!!.text()
        assert(allArgs.contains(arg_name))
        val arg_descr = textWithCode(removeArgName(it.html())).removePrefix(" – ")
        Argument(
            name = arg_name,
            type = allArgs[arg_name]?.first,
            defaultValue = allArgs[arg_name]?.second,
            description = arg_descr
        )
    }
    val methodReturnDescr = element.select(".field-list > dt:contains(Returns) + dd p").text()
    val methodExamples = element.select("section:contains(Examples) .highlight").map { it.text() }

    return Method(
        name = methodName,
        signature = methodSignature,
        description = methodDescr,
        args = methodArgs,
        returnType = methodReturnType,
        returnDescription = methodReturnDescr,
        examples = methodExamples,
    )

}

fun parseProperty(element: Element): Argument {
    val propertyName = element.selectFirst("dt.sig-object span.sig-name")!!.text()
    val propertyType = element.selectFirst("dt.sig-object span.sig-name + .property ")?.text()?.dropWhile { !it.isLetter() }
    val propertyDescr = element.select("dd > p").map { textWithCode(it.html()) }.joinToString("\n\n")  // TODO: Test it
    return Argument(
        name = propertyName,
        type = propertyType,
        description = propertyDescr,
    )
}

fun parseEnumMember(element: Element): EnumMember {
    val memberName = element.selectFirst("dt.sig-object span.sig-name")!!.text()
    val memberValue = element.selectFirst("dt.sig-object span.sig-name + .property")!!.text().removePrefix("= ")
    return EnumMember(
        name = memberName,
        value = memberValue,
    )
}

fun getArgsFromSignature(methodSignature: Element): Map<String, Pair<String?, String?>> {
    return methodSignature.select(".sig-param").map {
        it.selectFirst(".n")!!.text() to
                Pair(it.select(".p + .w + .n").text(), it.selectFirst("span.default_value")?.text())
    }.toMap()
}

fun textWithCode(text: String): String {
    return removeAllTags(replaceEmTags(replaceCodeTags(text)))
}

fun replaceCodeTags(text: String): String {
    return Regex("<code[^>]*>").replace(text, "`").replace("</code>", "` ")
}

fun replaceEmTags(text: String): String {
    return Regex("<em[^>]*>").replace(text, "_").replace("</em>", "_")
}

fun removeAllTags(text: String): String {
    return Regex("<[^>]*>").replace(text, "")
}

fun removeArgName(text: String): String {
    return Regex("<strong>[^<]*</strong>").replace(text, "")
}
