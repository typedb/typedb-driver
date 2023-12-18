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

package com.vaticle.typedb.driver.tool.docs.cpp

import com.vaticle.typedb.driver.tool.docs.dataclasses.Class
import com.vaticle.typedb.driver.tool.docs.dataclasses.EnumConstant
import com.vaticle.typedb.driver.tool.docs.dataclasses.Method
import com.vaticle.typedb.driver.tool.docs.dataclasses.Variable
import com.vaticle.typedb.driver.tool.docs.util.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import picocli.CommandLine
import picocli.CommandLine.Parameters
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess


fun main(args: Array<String>): Unit = exitProcess(CommandLine(DoxygenParser()).execute(*args))

@CommandLine.Command(name = "DoxygenParser", mixinStandardHelpOptions = true)
class DoxygenParser : Callable<Unit> {
    @Parameters(paramLabel = "<input>", description = ["Input directory"])
    private lateinit var inputDirectoryNames: List<String>

    @CommandLine.Option(names = ["--output", "-o"], required = true)
    private lateinit var outputDirectoryName: String

    /**
     * --dir=file=directory: put a file into the specified directory
     * If no directory is specified for at least one file, an exception will be thrown.
     */
    @CommandLine.Option(names = ["--dir", "-d"], required = true)
    private lateinit var dirs: HashMap<String, String>

    @Override
    override fun call() {
        val inputDirectoryName = inputDirectoryNames[0]

        val docsDir = System.getenv("BUILD_WORKSPACE_DIRECTORY")?.let { Paths.get(it).resolve(outputDirectoryName) }
            ?: Paths.get(outputDirectoryName)
        if (!docsDir.toFile().exists()) {
            Files.createDirectory(docsDir)
        }

        // Namespace file for the enums
        run {
            val namespacefile = File(inputDirectoryName).resolve("html/namespace_type_d_b.html");
            assert(namespacefile.exists())
            val html = File(namespacefile.path).readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)
            parsed.select("td.memname").filter { element ->
                element.text().startsWith("enum class")
            }.map { element -> element.parents().select(".memitem").first() }.forEach {
                val parsedEnum = parseEnum(it!!)
                System.out.println("TODO: Enum:" + parsedEnum.name)
            }
        }

        System.out.println(dirs)
        // class files
        File(inputDirectoryName).walkTopDown().filter {
            it.toString().startsWith("cpp/doxygen_docs/html/class_type_") && it.toString().endsWith(".html")
                    && !it.toString().contains("-members")
        }.forEach {
            val html = File(it.path).readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)
            val parsedClass = parseClass(parsed, it.parent)
            System.out.println("TODO: Class:" + parsedClass.name)
            if (parsedClass.isNotEmpty()) {
                val parsedClassAsciiDoc = parsedClass.toAsciiDoc("cpp")
                val fileName = "${generateFilename(parsedClass.name)}.adoc"
                val fileDir = docsDir.resolve(dirs[fileName]
                    ?: throw IllegalArgumentException("Output directory for '$fileName' was not provided"))
                if (!fileDir.toFile().exists()) {
                    Files.createDirectory(fileDir)
                }
                val outputFile = fileDir.resolve(fileName).toFile()
                outputFile.createNewFile()
                outputFile.writeText(parsedClassAsciiDoc)
            }
        }
    }

    private fun parseMemberDecls(document: Element): Map<String, List<Element>> {
        var map: MutableMap<String, List<Element>> = HashMap();
        document.select("table.memberdecls").forEach { table ->
            val heading: String = table.selectFirst("tr.heading > td > h2 > a")!!.id()
            val members: MutableList<Element> = ArrayList()
            table.select("tr").filter { element ->
                element.className().matches(Regex("memitem:[a-f0-9]+"))
            }.map { element ->
                element.className().substringAfter("memitem:")
            }.forEach { id ->
                val methodDetails =
                    document.selectFirst("div.contents > a#" + id)?.nextElementSibling()?.nextElementSibling()
                if (methodDetails == null) {
                    System.err.println("Member details not found: " + document.selectFirst("#r_" + id)!!.text())
                } else {
                    members.add(methodDetails!!)
                }
            }
            map[heading] = members
        }
        return map;
    }

    private fun parseClass(document: Element, currentDirName: String): Class {
        val fullyQualifiedName = document.selectFirst("div .title")!!.text()
            .replace(Regex("Class(?: Template)? Reference.*"), "").trim()
        val packagePath = fullyQualifiedName
        val className = fullyQualifiedName.substringAfterLast("::")
        val classAnchor = replaceSymbolsForAnchor(className)
        val classDescr: List<String> = document.selectFirst("div.textblock")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h")) } ?: listOf()
        val classExamples = document.select("div.textblock > pre").map { replaceSpaces(it.text()) }

        val memberDecls = parseMemberDecls(document)
        System.out.println("--- " + className + " ---")
        val superClasses = document.select("tr.inherit_header")
            .map { it.text().substringAfter("inherited from ") }
            .toSet().toList()

        val fields = memberDecls.getOrDefault("pub-attribs", listOf()).map { parseField(it) }
        val methods: List<Method> = (
                memberDecls.getOrDefault("pub-methods", listOf()) +
                memberDecls.getOrDefault("pub-static-methods", listOf())
            ).map {
                parseMethod(it!!, classAnchor)
            }

        System.out.println(superClasses)
        return Class(
            name = className,
            anchor = classAnchor,
            description = classDescr,
            examples = classExamples,
            fields = fields,
            methods = methods,
            packagePath = packagePath,
            superClasses = superClasses,
        )
    }

    private fun parseEnum(element: Element): Class {
        val id = element.previousElementSibling()?.previousElementSibling()?.id()!!
        val className = element.select("td.memname > a").text()
        val classAnchor = replaceSymbolsForAnchor(className)
        val classDescr: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h")) } ?: listOf()
        val classExamples = element.select("div.memdoc > pre").map { replaceSpaces(it.text()) }
        val enumConstants =
            element.parents().select("div.contents").first()!!
                .select("table.memberdecls > tbody > tr#r_" + id + " > td.memItemRight ").first()!!
                .text().substringAfter("{").substringBefore("}")
                .split(",")
                .map {
                    EnumConstant(it)
                }
        val packagePath = "TypeDB::" + className
        return Class(
            name = className,
            anchor = classAnchor,
            description = classDescr,
            enumConstants = enumConstants,
            examples = classExamples,
            packagePath = packagePath,
        )
    }

    private fun parseMethod(element: Element, classAnchor: String): Method {
        val methodName = element.previousElementSibling()!!.text().substringBefore("()").substringAfter(" ")
        val methodSignature = element.selectFirst("div.memproto")!!.text()
        val argsList = getArgsFromSignature(methodSignature)
        val argsMap = argsList.toMap()
        val methodReturnType = getReturnTypeFromSignature(methodSignature)
        var methodDescr: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }
            ?.map { replaceSpaces(reformatTextWithCode(it.substringBefore("<h"))) } ?: listOf()
        val methodExamples = element.select("td.memdoc > pre + div pre").map { replaceSpaces(it.text()) }
//
//        val methodArgs = element
//            .select(
//                "dt:has(.paramLabel) " +
//                        "~ dd:not(dt:has(.returnLabel) ~ dd, dt:has(.throwsLabel) ~ dd, dt:has(.seeLabel) ~ dd)"
//            )
//            .map {
//                val arg_name = it.selectFirst("code")!!.text()
//                assert(argsMap.contains(arg_name))
//                Variable(
//                    name = arg_name,
//                    type = argsMap[arg_name]?.replace("...", "[]"),
//                    description = reformatTextWithCode(it.html().substringAfter(" - ")),
//                )
//            }
        val methodAnchor = replaceSymbolsForAnchor("${classAnchor}_${methodName}_${argsList.map { it.second }}")
//        val seeAlso = element.selectFirst("dt:has(.seeLabel) + dd")?.let { reformatTextWithCode(it.html()) }
//        seeAlso?.let { methodDescr += "\nSee also: $seeAlso\n" }

        return Method(
            name = methodName,
            signature = enhanceSignature(methodSignature),
            anchor = methodAnchor,
//            args = methodArgs,
            description = methodDescr,
            examples = methodExamples,
            returnType = methodReturnType,
        )

    }

    private fun parseField(element: Element): Variable {
        val type = element.selectFirst("td.memname")!!.text().substringBeforeLast("::")
        val name = element.selectFirst("td.memname")!!.text().substringAfterLast("::")
        val descr = element.selectFirst("div.memdoc")!!.text()
        return Variable(
            name = name,
            description = descr,
            type = type,
        )
    }

    private fun getArgsFromSignature(methodSignature: String): List<Pair<String, String>> {
        return methodSignature
            .replace("\\s+".toRegex(), " ")
            .substringAfter("(").substringBefore(")")
            .split(",\\s".toRegex()).map {
                it.split("\u00a0").let { it.last() to it.dropLast(1).joinToString(" ") }
            }.filter { !it.first.isEmpty() || !it.second.isEmpty() }
            .toList()
    }

    private fun reformatTextWithCode(html: String): String {
        return removeAllTags(replaceLocalLinks(replaceEmTags(replacePreTags(replaceCodeTags(html)))))
    }

    private fun replacePreTags(html: String): String {
        return html.replace("<pre>", "[source,java]\n----\n").replace("</pre>", "\n----\n")
    }

    private fun enhanceSignature(signature: String): String {
        return replaceSpaces(signature)
    }

    private fun getReturnTypeFromSignature(signature: String): String {
        return Regex("@[^\\s]*\\s|default ").replace(
            signature.substringBefore("(")
                .substringBeforeLast("\u00a0"), ""
        )
    }

    private fun splitToParagraphs(html: String): List<String> {
        return html.replace("</p>", "").split("\\s*<p>\\s*".toRegex()).map { it.trim() }
    }

    private fun replaceLocalLinks(html: String): String {
        val fragments: MutableList<String> = Regex("<a\\shref=\"#([^\"]*)\">([^<]*)</a>")
            .replace(html, "<<#_~$1~,$2>>").split("~").toMutableList()
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

    private fun generateFilename(className: String): String {
        return className.replace("[<> ,]".toRegex(), "_")
    }
}
