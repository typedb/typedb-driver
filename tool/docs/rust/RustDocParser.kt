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
import com.vaticle.typedb.client.tool.doc.common.replaceSymbolsForAnchor
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
        val anchor = getAnchorFromUrl(it.toString())
        val overallOutputFile = docsDir.resolve("_rust_driver.adoc").toFile()
        if (!parsed.select(".main-heading h1 a.struct").isNullOrEmpty()) {
            val parsedClass = parseClass(parsed, anchor)
//            println(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("rust"))
            overallOutputFile.appendText(parsedClass.toAsciiDoc("rust"))
        } else if (!parsed.select(".main-heading h1 a.trait").isNullOrEmpty()) {
            val parsedClass = parseTrait(parsed, anchor)
//            println(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("rust"))
            overallOutputFile.appendText(parsedClass.toAsciiDoc("rust"))
        } else if (!parsed.select(".main-heading h1 a.enum").isNullOrEmpty()) {
            val parsedClass = parseEnum(parsed, anchor)
            //print(parsedClass)
            val outputFile = docsDir.resolve(parsedClass.name + ".adoc").toFile()
            outputFile.createNewFile()
            outputFile.writeText(parsedClass.toAsciiDoc("rust"))
            overallOutputFile.appendText(parsedClass.toAsciiDoc("rust"))
        }
    }
}

fun parseClass(document: Element, classAnchor: String): Class {
    val className = document.selectFirst(".main-heading h1 a.struct")!!.text()
    val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

    val fields = document.select(".structfield").map {
        parseField(it, classAnchor)
    }

    val methods = document.select("#implementations-list details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it, classAnchor)
    } + document.select("#trait-implementations-list summary:has(section:not(section:has(h3 a.trait[href^=http]))) " +
            "+ .impl-items details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it, classAnchor)
    }

    val traits = document.select(".sidebar-elems h3:has(a[href=#trait-implementations]) + ul li").map { it.text() }

    return Class(
        name = className,
        description = classDescr,
        fields = fields,
        methods = methods,
        anchor = classAnchor,
        superClasses = traits,
    )
}

fun parseTrait(document: Element, classAnchor: String): Class {
    val className = document.selectFirst(".main-heading h1 a.trait")!!.text()
    val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

    val methods = document.select("#required-methods + .methods details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it, classAnchor)
    } + document.select("#provided-methods + .methods details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it, classAnchor)
    }

    return Class(
        name = "Trait $className",
        description = classDescr,
        methods = methods,
        anchor = classAnchor,
    )
}

fun parseEnum(document: Element, classAnchor: String): Enum {
    val className = document.selectFirst(".main-heading h1 a.enum")!!.text()
    val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

//    val fields = document.select(".structfield").map {
//        parseField(it)
//    }

    val variants = document.select("section.variant").map {
        parseEnumConstant(it)
    }

    val methods = document.select("#implementations-list details[class*=method-toggle]:has(summary section.method)").map {
        parseMethod(it, classAnchor)
    }

    return Enum(
        name = className,
        description = classDescr,
        constants = variants,
        methods = methods,
        anchor = classAnchor,
    )
}

fun parseMethod(element: Element, classAnchor: String): Method {
    val methodSignature = enhanceSignature(element.selectFirst("summary section h4")!!.html())
    val methodName = element.selectFirst("summary section h4 a.fn")!!.text()
    val methodAnchor = replaceSymbolsForAnchor(
        element.selectFirst("summary section h4 a.fn")!!.attr("href").substringAfter("#")
    )
    val allArgs = getArgsFromSignature(methodSignature)
    val methodReturnType = if (methodSignature.contains(" -> ")) methodSignature.split(" -> ").last() else null
    val methodDescr = if (element.select("div.docblock p").isNotEmpty()) {
        element.select("div.docblock p").map { reformatTextWithCode(it.html()) }
    } else {
        element.select("div.docblock a:contains(Read more)").map {
            "<<#_" + getAnchorFromUrl(it.attr("href")) + ",Read more>>"
        }
    }
    val methodExamples = element.select("div.docblock div.example-wrap pre").map { it.text() }
    val methodArgs = element.select("div.docblock ul li code:eq(0)").map {
        val argName = it.text().trim()
        assert(allArgs.contains(argName))
        val argDescr = reformatTextWithCode(removeArgName(it.parent()!!.html())).removePrefix(" – ")
        Argument(
            name = argName,
            type = allArgs[argName]?.trim(),
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
        anchor = "${classAnchor}_$methodAnchor",
    )

}

fun parseField(element: Element, classAnchor: String): Argument {
    val nameAndType = element.selectFirst("code")!!.text().split(": ")
    val descr = element.nextElementSibling()?.selectFirst(".docblock")?.let { reformatTextWithCode(it.html()) }
    return Argument(
        name = nameAndType[0],
        type = nameAndType[1],
        description = descr,
        anchor = "${classAnchor}_${nameAndType[0]}",
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
    return removeAllTags(replaceEmTags(replaceCodeTags(replaceLinks(html))))
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

fun getAnchorFromUrl(url: String): String {
   return replaceSymbolsForAnchor(url.substringAfterLast("/").replace(".html", ""))
}

fun replaceLinks(html: String): String {
    val fragments: MutableList<String> = Regex("<a\\shref=\"([^:]*)#([^\"]*)\"[^>]*><code>([^<]*)</code>")
        .replace(html, "<<#_~$1_$2~,$3>>").split("~").toMutableList()
    if (fragments.size > 1) {
        val iterator = fragments.listIterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (!value.contains("<<") && !value.contains(">>")) {
                iterator.set(getAnchorFromUrl(value))
            }
        }
    }
    return fragments.joinToString("")
}
