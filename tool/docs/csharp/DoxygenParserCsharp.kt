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

package com.vaticle.typedb.driver.tool.docs.csharp

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


fun main(args: Array<String>): Unit = exitProcess(CommandLine(DoxygenParserCsharp()).execute(*args))

@CommandLine.Command(name = "DoxygenParserCsharp", mixinStandardHelpOptions = true)
class DoxygenParserCsharp : Callable<Unit> {
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
        val classes: MutableList<Class> = ArrayList()

        // TODO: Parse enums?

        // class files
        File(inputDirectoryName).walkTopDown().filter {
            (it.toString().startsWith("csharp/doxygen_docs/html/class_type_")
                    || it.toString().startsWith("csharp/doxygen_docs/html/interface_type_")
                    || it.toString().startsWith("csharp/doxygen_docs/html/struct_type_"))
                && it.toString().endsWith(".html")
                && !it.toString().contains("-members")
        }.forEach {
            val html = File(it.path).readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)
            val parsedClass = parseClass(parsed)
            if (parsedClass.isNotEmpty()) classes.add(parsedClass)
        }

        classes.forEach { parsedClass ->
            val parsedClassAsciiDoc = parsedClass.toAsciiDoc("csharp")
            val outputFile = getFile(docsDir, "${generateFilename(parsedClass.name)}.adoc")
            outputFile.writeText(parsedClassAsciiDoc)
        }
    }

    private fun getFile(docsDir: Path, fileName: String): File {
        val fileDir = docsDir.resolve(
            dirs[fileName]
                ?: throw IllegalArgumentException("Output directory for '$fileName' was not provided")
        )
        if (!fileDir.toFile().exists()) {
            Files.createDirectory(fileDir)
        }
        val outputFile = fileDir.resolve(fileName).toFile()
        outputFile.createNewFile()
        return outputFile
    }

    private fun parseMemberDecls(document: Element): Pair<Map<String, List<Element>>, Map<String, String>> {
        val missingDeclarations: MutableList<String> = ArrayList()
        val map: MutableMap<String, List<Element>> = HashMap()
        val idToAnchor: MutableMap<String, String> = HashMap()
        document.select("table.memberdecls").forEach { table ->
            val heading: String = table.selectFirst("tr.heading > td > h2 > a")!!.attr("name")
            println(heading)
            val members: MutableList<Element> = ArrayList()
            table.select("tr").filter { element ->
                element.className().matches(Regex("memitem:[a-f0-9]+"))
            }.forEach { element ->
                val id = element.className().substringAfter("memitem:")
                val type = element.selectFirst("td.memItemLeft")?.text()
                if (type == "enum") {
                    println("ENUM! $element")
                    val memberDetails =
                        document.selectFirst("div.contents > a#$id")?.nextElementSibling()?.nextElementSibling()

                    val typeAndName = memberDetails?.selectFirst("div.memitem > div.memproto > table.memname > tbody > tr > td.memname")?.text()?.split(" ")
                    if (typeAndName != null && typeAndName.size > 1 && typeAndName[0] == "enum") { // ENUM!
                        println("Details: $memberDetails, typeAndName: ${typeAndName}")
                    }
                }

                val memberDetails =
                    document.selectFirst("div.contents > a#$id")?.nextElementSibling()?.nextElementSibling()

                if (memberDetails == null) {
                    missingDeclarations.add(element.selectFirst("td.memItemRight")!!.text())
                } else {
                    members.add(memberDetails)
                    idToAnchor[id] = replaceSymbolsForAnchor(memberDetails.select("table.memname").text())
                }
            }
            map[heading] = members
//            print("Result for now: $map")
        }

        if (missingDeclarations.isNotEmpty()) {
            println("Missing some member declarations:\n\t-" + missingDeclarations.joinToString("\n\t-"))
        }

        return Pair(map, idToAnchor)
    }

    private fun parseClass(document: Element): Class {
        // If we want inherited members, consider doxygen's INLINE_INHERITED_MEMB instead of the javadoc approach
        val fullyQualifiedName = document.selectFirst("div .title")!!.text()
            .replace(Regex("Class(?: Template)? Reference.*"), "").trim()
        val packagePath = fullyQualifiedName.substringBeforeLast("::")
        val className = fullyQualifiedName.substringAfterLast("::")
//        println(packagePath, className)
        val classAnchor = replaceSymbolsForAnchor(className)
        val classExamples = document.select("div.textblock > pre").map { replaceSpaces(it.text()) }
        val superClasses = document.select("tr.inherit_header")
            .map { it.text().substringAfter("inherited from ") }
            .toSet().toList()

        val (memberDecls, idToAnchor) = parseMemberDecls(document)
        val classDescr: List<String> = document.selectFirst("div.textblock")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h"), idToAnchor) }
            ?: listOf()

        val fields = memberDecls.getOrDefault("pub-attribs", listOf()).map { parseField(it, idToAnchor) }
        val methods: List<Method> = (
                memberDecls.getOrDefault("pub-methods", listOf()) +
                        memberDecls.getOrDefault("pub-static-methods", listOf())
                ).map {
                parseMethod(it, idToAnchor)
            }

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
        val fullyQualifiedName = element.select("td.memname > a").text()
        val className = fullyQualifiedName.substringAfterLast("::")
        val classAnchor = replaceSymbolsForAnchor(className)
        val classDescr: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h"), HashMap()) }
            ?: listOf()
        val classExamples = element.select("div.memdoc > pre").map { replaceSpaces(it.text()) }
        val enumConstants =
            element.parents().select("div.contents").first()!!
                .select("table.memberdecls > tbody > tr[class=memitem:$id] > td.memItemRight ").first()!!
                .text().substringAfter("{").substringBefore("}")
                .split(",")
                .map {
                    EnumConstant(it.trim())
                }
        val packagePath = fullyQualifiedName.substringBeforeLast("::")
        return Class(
            name = className,
            anchor = classAnchor,
            description = classDescr,
            enumConstants = enumConstants,
            examples = classExamples,
            packagePath = packagePath,
        )
    }

    private fun parseMethod(element: Element, idToAnchor: Map<String, String>): Method {
        val id = element.previousElementSibling()?.previousElementSibling()?.id()!!
        val methodAnchor = idToAnchor[id]
        val methodName = element.previousElementSibling()!!.text().substringBefore("()").substringAfter(" ")
        val methodSignature = enhanceSignature(element.selectFirst("table.memname")!!.text())
        val argsList = getArgsFromSignature(methodSignature)
        val argsMap = argsList.toMap()
        val methodReturnType = getReturnTypeFromSignature(methodSignature)
        val methodDescr: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }
            ?.map { replaceSpaces(reformatTextWithCode(it.substringBefore("<h"), idToAnchor)) } ?: listOf()
        val methodExamples = element.select("td.memdoc > pre + div pre").map { replaceSpaces(it.text()) }

        val methodArgs = element.select("table.params > tbody > tr")
            .map {
                val argName = it.child(0).text()
                assert(argsMap.contains(argName))
                Variable(
                    name = argName,
                    type = argsMap[argName],
                    description = reformatTextWithCode(it.child(1).html(), idToAnchor),
                )
            }

        return Method(
            name = methodName,
            signature = methodSignature,
            anchor = methodAnchor,
            args = methodArgs,
            description = methodDescr,
            examples = methodExamples,
            returnType = methodReturnType,
        )

    }

    private fun parseField(element: Element, idToAnchor: Map<String, String>): Variable {
        val type = element.selectFirst("td.memname")!!.text().substringBeforeLast("::")
        val name = element.selectFirst("td.memname")!!.text().substringAfterLast("::")
        val descr = reformatTextWithCode(element.selectFirst("div.memdoc")!!.html(), idToAnchor)
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
            .split(",\\s".toRegex()).map { arg ->
                arg.split("\u00a0").let { it.last() to it.dropLast(1).joinToString(" ") }
            }.filter { it.first.isNotEmpty() || it.second.isNotEmpty() }
            .toList()
    }

    private fun reformatTextWithCode(html: String, idToAnchor: Map<String, String>): String {
        return removeAllTags(replaceLocalLinks(idToAnchor, replaceEmTags(replacePreTags(replaceCodeTags(html)))))
    }

    private fun replacePreTags(html: String): String {
        return html.replace("<pre>", "[source,java]\n----\n").replace("</pre>", "\n----\n")
    }

    private fun enhanceSignature(signature: String): String {
        var enhanced = replaceSpaces(signature)
        enhanced = enhanced.replace("( ", "(")
        enhanced = Regex("\\s([()*&])").replace(enhanced, "$1")
        return enhanced
    }

    private fun getReturnTypeFromSignature(signature: String): String {
        return signature.replace(", ", ",").replace("< ", "<").replace(" >", ">")
            .substringBefore("(").substringBeforeLast(" ")
            .replace(",", ", ").replace("<", "< ").replace(">", " >")
    }

    private fun splitToParagraphs(html: String): List<String> {
        return html.replace("</p>", "").split("\\s*<p>\\s*".toRegex()).map { it.trim() }
    }


    private fun replaceLocalLinks(idToAnchor: Map<String, String>, html: String): String {
        // The Intellij preview messes up nested templates & The '>>' used for cross links.
        return Regex("<a class=\"el\" href=\"[^\"^#]*#([^\"]*)\">([^<]*)</a>")
            .replace(html) {
                if (idToAnchor.containsKey(it.groupValues[1]))
                    "<<#_%s,%s>>".format(idToAnchor[it.groupValues[1]], it.groupValues[2])
                else "%s".format(it.groupValues[2])
            }
    }

    private fun generateFilename(className: String): String {
        return className.replace("[<> ,]".toRegex(), "_").replace("_Interface_Reference", "")
    }
}
