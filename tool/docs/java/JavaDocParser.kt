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
import com.vaticle.typedb.client.tool.doc.common.Enum
import com.vaticle.typedb.client.tool.doc.common.EnumConstant
import com.vaticle.typedb.client.tool.doc.common.Method
import com.vaticle.typedb.client.tool.doc.common.removeAllTags
import com.vaticle.typedb.client.tool.doc.common.replaceCodeTags
import com.vaticle.typedb.client.tool.doc.common.replaceEmTags
import com.vaticle.typedb.client.tool.doc.common.replaceSpaces
import com.vaticle.typedb.client.tool.doc.common.replaceSymbolsForAnchor
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
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
        var parsedClassName = ""
        var parsedClassAsciiDoc = ""
        if (!parsed.select("h2[title^=Interface]").isNullOrEmpty()
                || !parsed.select("h2[title^=Class]").isNullOrEmpty()) {
            val parsedClass = parseClass(parsed, it.parent)
            parsedClassName = parsedClass.name
            parsedClassAsciiDoc = parsedClass.toAsciiDoc("java")

        } else if (!parsed.select("h2[title^=Enum]").isNullOrEmpty()) {
            val parsedClass = parseEnum(parsed)
            parsedClassName = parsedClass.name
            parsedClassAsciiDoc = parsedClass.toAsciiDoc("java")
        }

        val outputFile = docsDir.resolve("$parsedClassName.adoc").toFile()
        outputFile.createNewFile()
        outputFile.writeText(parsedClassAsciiDoc)
    }
}

fun parseClass(document: Element, currentDirName: String): Class {
    val className = document.selectFirst(".contentContainer .description pre .typeNameLabel")!!.text()
    val classDescr: List<String> = document.selectFirst(".contentContainer .description pre + div")
        ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h")) } ?: listOf()
    val packagePath = document.selectFirst(".packageLabelInType + a")?.text()
    val classExamples = document.select(".contentContainer .description pre + div pre").map { replaceSpaces(it.text()) }
    val superClasses = document.select(".contentContainer .description dt:contains(Superinterfaces) + dd code").map {
        it.text()
    }

    val fields = document.select(".summary > ul > li > section > ul > li:has(a[id=field.summary]) > table tr:gt(0)").map {
        parseField(it)
    }
    val methods = document.select(".details > ul > li > section > ul > li:has(a[id=constructor.detail]) > ul > li").map {
        parseMethod(it)
    } + document.select(".details > ul > li > section > ul > li:has(a[id=method.detail]) > ul > li").map {
        parseMethod(it)
    } + document.select(".memberSummary + ul > li > h3:contains(Methods inherited from) + code > a").map {
        val parentPath = Path.of(currentDirName).resolve(it.attr("href").substringBefore("#"))
        val parentHtml = File(parentPath.toString()).readText(Charsets.UTF_8)
        val parentParsed = Jsoup.parse(parentHtml)
        val anchor = it.attr("href").substringAfter("#")
        parseMethod(parentParsed.selectFirst("a[id=$anchor] + ul > li")!!)
    }


    return Class(
        name = className,
        description = classDescr,
        methods = methods,
        fields = fields,
        superClasses = superClasses,
        examples = classExamples,
        packagePath = packagePath,
    )
}

fun parseEnum(document: Element): Enum {
    val className = document.selectFirst(".contentContainer .description pre .typeNameLabel")!!.text()
    val classDescr: List<String> = document.selectFirst(".contentContainer .description pre + div")
        ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h")) } ?: listOf()
    val packagePath = document.selectFirst(".packageLabelInType + a")?.text()
    val classExamples = document.select(".contentContainer .description pre + div pre").map { replaceSpaces(it.text()) }
    val classBases = document.select(".contentContainer .description dt:contains(Superinterfaces) + dd code").map {
        it.text()
    }

    val enumConstants = document.select(".summary > ul > li > section > ul > li:has(a[id=enum.constant.summary]) > table tr:gt(0)").map {
        parseEnumConstant(it)
    }
    val fields = document.select(".summary > ul > li > section > ul > li:has(a[id=field.summary]) > table tr:gt(0)").map {
        parseField(it)
    }
    val methods = document.select(".details > ul > li > section > ul > li:has(a[id=constructor.detail]) > ul > li").map {
        parseMethod(it)
    } + document.select(".details > ul > li > section > ul > li:has(a[id=method.detail]) > ul > li").map {
        parseMethod(it)
    }

    return Enum(
        name = className,
        description = classDescr,
        constants = enumConstants,
        fields = fields,
        methods = methods,
        bases = classBases,
        examples = classExamples,
        packagePath = packagePath,
    )
}

fun parseMethod(element: Element): Method {
    val methodAnchor = replaceSymbolsForAnchor(element.parent()!!.previousElementSibling()!!.id())
    val methodName = element.selectFirst("h4")!!.text()
    val methodSignature = element.selectFirst("li.blockList > pre")!!.text()
    val allArgs = getArgsFromSignature(methodSignature)
    val methodReturnType = getReturnTypeFromSignature(methodSignature)
    var methodDescr: List<String> = element.selectFirst("li.blockList > pre ~ div:not(div:has(.descfrmTypeLabel))")
        ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h")) } ?: listOf()
    val methodExamples = element.select("li.blockList > pre + div pre").map { replaceSpaces(it.text()) }

    val methodArgs = element
        .select("dt:has(.paramLabel) " +
                "~ dd:not(dt:has(.returnLabel) ~ dd, dt:has(.throwsLabel) ~ dd, dt:has(.seeLabel) ~ dd)")
        .map {
            val arg_name = it.selectFirst("code")!!.text()
            assert(allArgs.contains(arg_name))
            Argument(
                name = arg_name,
                type = allArgs[arg_name]?.replace("...", "[]"),
                description = reformatTextWithCode(it.html().substringAfter(" - ")),
            )
        }

    val seeAlso = element.selectFirst("dt:has(.seeLabel) + dd")?.let { reformatTextWithCode(it.html()) }
    seeAlso?.let { methodDescr += "\nSee also: $seeAlso\n" }

    return Method(
        name = methodName,
        signature = enhanceSignature(methodSignature),
        description = methodDescr,
        args = methodArgs,
        returnType = methodReturnType,
        examples = methodExamples,
        anchor = methodAnchor,
    )

}

fun parseField(element: Element): Argument {
    val name = element.selectFirst(".colSecond")!!.text()
    val type = element.selectFirst(".colFirst")!!.text()
    val descr = element.selectFirst(".colLast")?.text()
    return Argument(
        name = name,
        type = type,
        description = descr,
    )
}

fun parseEnumConstant(element: Element): EnumConstant {
    val name = element.selectFirst(".colFirst")!!.text()
    return EnumConstant(
        name = name,
    )
}

fun getArgsFromSignature(methodSignature: String): Map<String, String?> {
    return methodSignature
        .replace("\\s+".toRegex(), " ")
        .substringAfter("(").substringBefore(")")
        .split(",\\s".toRegex()).map {
            it.split("\u00a0").let { it.last() to it.dropLast(1).joinToString(" ") }
        }.toMap()
}

fun reformatTextWithCode(html: String): String {
    return removeAllTags(replaceLocalLinks(replaceEmTags(replacePreTags(replaceCodeTags(html)))))
}

fun replacePreTags(html: String): String {
    return html.replace("<pre>", "[source,java]\n----\n").replace("</pre>", "\n----\n")
}

fun enhanceSignature(signature: String): String {
    return replaceSpaces(signature)
}

fun getReturnTypeFromSignature(signature: String): String {
    return Regex("@[^\\s]*\\s|default ").replace(signature.substringBefore("(")
        .substringBeforeLast("\u00a0"), "")
}

fun splitToParagraphs(html: String): List<String> {
    return html.replace("</p>", "").split("\\s*<p>\\s*".toRegex()).map { it.trim() }
}

fun replaceLocalLinks(html: String): String {
    val fragments: MutableList<String> = Regex("<a\\shref=\"#([^\"]*)\">([^<]*)</a>")
        .replace(html, "<<~#_$1~,$2>>").split("~").toMutableList()
    if (fragments.size > 1) {
        val iterator = fragments.listIterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (!value.contains("<<") && !value.contains(">>")) {
                iterator.set(replaceSymbolsForAnchor(value))
            }
        }
    }
    return fragments.joinToString("")
}
