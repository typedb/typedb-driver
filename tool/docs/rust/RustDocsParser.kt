/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.vaticle.typedb.driver.tool.doc.rust

import com.vaticle.typedb.driver.tool.doc.common.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import picocli.CommandLine
import picocli.CommandLine.Parameters
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess


fun main(args: Array<String>): Unit = exitProcess(CommandLine(RustDocParser()).execute(*args))

@CommandLine.Command(name = "RustDocsParser", mixinStandardHelpOptions = true)
class RustDocParser : Callable<Unit> {
    @Parameters(paramLabel = "<input>", description = ["Input directory (for rust specify async first)"])
    private lateinit var inputDirectoryNames: List<String>

    @CommandLine.Option(names = ["--output", "-o"], required = true)
    private lateinit var outputDirectoryName: String

    @CommandLine.Option(names = ["--feature", "-f"], required = false)
    private lateinit var feature: String

    @Override
    override fun call() {
        val baseDocsDir = System.getenv("BUILD_WORKSPACE_DIRECTORY")?.let { Paths.get(it).resolve(outputDirectoryName) }
            ?: Paths.get(outputDirectoryName)
        if (!baseDocsDir.toFile().exists()) {
            Files.createDirectory(baseDocsDir)
        }

        if (feature != "merge") {
            val docsDir = baseDocsDir.resolve(feature)
            if (!docsDir.toFile().exists()) {
                Files.createDirectory(docsDir)
            }
            parseDirectory(inputDirectoryNames[0]).forEach { (className, parsedClass) ->
                val outputFile = docsDir.resolve(className.replace(" ", "_") + ".adoc").toFile()
                outputFile.createNewFile()
                outputFile.writeText(parsedClass.toAsciiDoc("rust"))
            }
        } else {
            assert(inputDirectoryNames.size == 2)
            val parsedAsync = parseDirectory(inputDirectoryNames[0])
            val parsedSync = parseDirectory(inputDirectoryNames[1])
            parsedAsync.forEach { (className, classAsync) ->
                val outputFile = baseDocsDir.resolve(className.replace(" ", "_") + ".adoc").toFile()
                outputFile.createNewFile()
                outputFile.writeText(classAsync.toAsciiDoc("rust", parsedSync[className]!!))
            }

        }
    }

    fun parseDirectory(inputDirectoryName: String): HashMap<String, Class> {
        val parsedClasses: HashMap<String, Class> = hashMapOf()
        File(inputDirectoryName).walkTopDown().filter {
            it.toString().contains("struct.") || it.toString().contains("trait.") || it.toString().contains("enum.")
        }.forEach {
            val html = it.readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)
            val anchor = getAnchorFromUrl(it.toString())
            val parsedClass = if (!parsed.select(".main-heading h1 a.struct").isNullOrEmpty()) {
                parseClass(parsed, anchor)
            } else if (!parsed.select(".main-heading h1 a.trait").isNullOrEmpty()) {
                parseTrait(parsed, anchor)
            } else if (!parsed.select(".main-heading h1 a.enum").isNullOrEmpty()) {
                parseEnum(parsed, anchor)
            } else {
                null
            }
            parsedClass?.let {
                if (parsedClass.isNotEmpty()) {
                    parsedClasses[parsedClass.name] = parsedClass
                }
            }
        }
        return parsedClasses
    }

    fun parseClass(document: Element, classAnchor: String): Class {
        val className = document.selectFirst(".main-heading h1 a.struct")!!.text()
        val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

        val fields = document.select(".structfield").map {
            parseField(it, classAnchor)
        }

        val methods =
            document.select("#implementations-list details[class*=method-toggle]:has(summary section.method)").map {
                parseMethod(it, classAnchor)
            } + document.select(
                "#trait-implementations-list summary:has(section:not(section:has(h3 a.trait[href^=http]))) " +
                        "+ .impl-items details[class*=method-toggle]:has(summary section.method)"
            ).map {
                parseMethod(it, classAnchor)
            }

        val traits = document.select(".sidebar-elems h3:has(a[href=#trait-implementations]) + ul li").map { it.text() }

        return Class(
            name = className,
            anchor = classAnchor,
            description = classDescr,
            fields = fields,
            methods = methods,
            superClasses = traits,
        )
    }

    fun parseTrait(document: Element, classAnchor: String): Class {
        val className = document.selectFirst(".main-heading h1 a.trait")!!.text()
        val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

        val methods =
            document.select("#required-methods + .methods details[class*=method-toggle]:has(summary section.method)")
                .map {
                    parseMethod(it, classAnchor)
                } + document.select("#provided-methods + .methods details[class*=method-toggle]:has(summary section.method)")
                .map {
                    parseMethod(it, classAnchor)
                }

        val implementors = document.select("#implementors-list > section > .code-header > .struct")
            .map {
                it.text()
            }

        return Class(
            name = "Trait $className",
            anchor = classAnchor,
            description = classDescr,
            methods = methods,
            traitImplementors = implementors,
        )
    }

    fun parseEnum(document: Element, classAnchor: String): Class {
        val className = document.selectFirst(".main-heading h1 a.enum")!!.text()
        val classDescr = document.select(".item-decl + details.top-doc .docblock p").map { it.html() }

        val variants = document.select("section.variant").map { parseEnumConstant(it) }

        val methods = document.select("#implementations-list details[class*=method-toggle]:has(summary section.method)")
            .map { parseMethod(it, classAnchor) }

        return Class(
            name = className,
            anchor = classAnchor,
            description = classDescr,
            enumConstants = variants,
            methods = methods,
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
            Variable(
                name = argName,
                description = argDescr,
                type = allArgs[argName]?.trim(),
            )
        }

        return Method(
            name = methodName,
            signature = methodSignature,
            anchor = "${classAnchor}_$methodAnchor",
            args = methodArgs,
            description = methodDescr,
            examples = methodExamples,
            returnType = methodReturnType,
        )

    }

    fun parseField(element: Element, classAnchor: String): Variable {
        val nameAndType = element.selectFirst("code")!!.text().split(": ")
        val descr = element.nextElementSibling()?.selectFirst(".docblock")?.let { reformatTextWithCode(it.html()) }
        return Variable(
            name = nameAndType[0],
            anchor = "${classAnchor}_${nameAndType[0]}",
            description = descr,
            type = nameAndType[1],
        )
    }

    fun parseEnumConstant(element: Element): EnumConstant {
        return EnumConstant(
            name = element.selectFirst("h3")!!.text(),
        )
    }

    fun getArgsFromSignature(methodSignature: String): Map<String, String?> {
        //    Splitting by ", " is incorrect (could be used in the type), but we don't have such cases now
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
        return replaceHtmlSymbols(removeAllTags(replaceSpaces(dispatchNewlines(signature))))
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
            .replace(html, "<<#_~$1_$2~,`$3`>>").split("~").toMutableList()
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
}
