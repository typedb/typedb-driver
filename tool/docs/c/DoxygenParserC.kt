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

package com.vaticle.typedb.driver.tool.docs.c

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
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.exitProcess


fun main(args: Array<String>): Unit = exitProcess(CommandLine(DoxygenParserC()).execute(*args))

@CommandLine.Command(name = "DoxygenParserC", mixinStandardHelpOptions = true)
class DoxygenParserC : Callable<Unit> {

    @Parameters(paramLabel = "<input>", description = ["Input directory"])
    private lateinit var inputDirectoryNames: List<String>

    @CommandLine.Option(names = ["--output", "-o"], required = true)
    private lateinit var outputDirectoryName: String

    /**
     * --dir=file=directory: put a file into the specified directory
     * If no directory is specified for at least one file, an exception will be thrown.
     */
    @CommandLine.Option(names = ["--dir", "-d"], required = true)
    private lateinit var unnormalisedDirs: MutableMap<String, String>

    private lateinit var sortedDirsLongest: List<Pair<String, String>>

    @CommandLine.Option(names = ["--forcefile", "-f"], required = true)
    private lateinit var unnormalisedFilenameOverrides: MutableMap<String, String>

    @Override
    override fun call() {
        val inputDirectoryName = inputDirectoryNames[0]
        val dirs: MutableMap<String, String> = HashMap()
        unnormalisedDirs.entries.forEach { entry -> dirs.put(normaliseKey(entry.key), entry.value) }
        val filenameOverrides: MutableMap<String, String> = HashMap()
        unnormalisedFilenameOverrides.entries.forEach { entry ->
            filenameOverrides.put(
                normaliseKey(entry.key),
                normaliseKey(entry.value)
            )
        }

        sortedDirsLongest =
            dirs.entries.map { entry -> Pair(entry.key, entry.value) }.sortedByDescending { it.first.length }.toList()

        val docsDir = System.getenv("BUILD_WORKSPACE_DIRECTORY")?.let { Paths.get(it).resolve(outputDirectoryName) }
            ?: Paths.get(outputDirectoryName)
        if (!docsDir.toFile().exists()) {
            Files.createDirectory(docsDir)
        }

        run {
            val typedb_driver_file = File(inputDirectoryName).resolve("html/typedb__driver_8h.html")
            val html = File(typedb_driver_file.path).readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)

            // Typedef
            val typedefs = parsed.select("td.memname").filter { element -> element.text().startsWith("typedef") }
                .map { element -> element.parents().select(".memitem").first() }
                .map {
                    parseTypeDef(it!!)
                }.toList()

            // Enums
            val enums = parsed.select("td.memname").filter { element -> element.text().startsWith("enum") }
                .map { element -> element.parents().select(".memitem").first() }.map {
                    parseEnum(it!!)
                }.filter {
                    it.name.isNotEmpty()
                }.toList()
            val functions = parsed.select("h2").filter { element -> element.text().endsWith("()") }
                .map { element -> element.nextElementSibling()!! }
                .filter { element -> element.className().equals("memitem") }
                .map { memItemElement -> parseMethod(memItemElement) }

            // Write to lists
            val typeFileContents: MutableMap<String, MutableList<String>> = HashMap()
            dirs.values.forEach { typeFileContents.put(it, ArrayList()) }
            typedefs.map {
                typeFileContents.get(dirs.get(resolveKey(it.name)))!!.add(it.toAsciiDoc("cpp"))
            }
            enums.forEach {
                typeFileContents.get(dirs.get(resolveKey(it.name)))!!.add(it.toAsciiDoc("cpp"))
            }
            val fileContents: MutableMap<String, MutableList<String>> = HashMap()
            dirs.keys.forEach { fileContents.put(resolveKey(it), ArrayList()) }
            functions.forEach {
                fileContents.get(resolveKey(it.name))!!.add(it.toAsciiDoc("cpp"))
            }

            // Write to files
            typeFileContents.entries.filter { it.value.isNotEmpty() }.forEach { entry ->
                val outputFile = createFile(docsDir.resolve(entry.key), "types.adoc")
                entry.value.forEach { outputFile.appendText(it) }
            }
            fileContents.entries.filter { it.value.isNotEmpty() }.forEach { entry ->
                val resolvedKey = resolveKey(entry.key)
                val filename = filenameOverrides.getOrDefault(resolvedKey, resolvedKey)
                val outputFile = createFile(docsDir.resolve(dirs.get(filename)), filename + ".adoc")
                val fileContent = entry.value
                fileContent.forEach { outputFile.appendText(it) }
            }
        }
    }

    private fun createFile(fileDir: Path, filename: String): File {
        if (!fileDir.toFile().exists()) {
            Files.createDirectory(fileDir)
        }
        val outputFile = fileDir.resolve(filename).toFile()
        outputFile.createNewFile()
        return outputFile
    }

    private fun normaliseKey(key: String): String {
        return key.replace("_", "").lowercase(Locale.getDefault())
    }

    private fun resolveKey(key: String): String {
        return sortedDirsLongest.first { normaliseKey(key).startsWith(it.first) }.first
    }

    private fun parseTypeDef(element: Element): Class {
        val name = element.previousElementSibling()!!.text().substringAfter(" ")
        val desc: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h")) } ?: listOf()
        return Class(
            name = name,
            anchor = replaceSymbolsForAnchor(name),
            description = desc,
        )
    }

    private fun parseEnum(element: Element): Class {
        val id = element.previousElementSibling()?.previousElementSibling()?.id()!!
        val className = element.select("td.memname > a").text()
        val classAnchor = replaceSymbolsForAnchor(className)
        val classDescr: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h")) } ?: listOf()
        val enumConstants =
            element.parents().select("div.contents").first()!!
                .select("table.memberdecls > tbody > tr[class=memitem:$id] > td.memItemRight ").first()!!
                .text().substringAfter("{").substringBefore("}")
                .split(",")
                .map {
                    EnumConstant(it.trim())
                }
        return Class(
            name = className,
            anchor = classAnchor,
            description = classDescr,
            enumConstants = enumConstants,
        )
    }

    private fun parseMethod(element: Element): Method {
        val methodName = element.previousElementSibling()!!.text().substringBefore("()").substringAfter(" ")
        val methodAnchor = replaceSymbolsForAnchor(methodName)
        val methodSignature = enhanceSignature(element.selectFirst("table.memname")!!.text())
        val argsList = getArgsFromSignature(methodSignature)
        val argsMap = argsList.toMap()
        val methodReturnType = getReturnTypeFromSignature(methodSignature)
        val methodDescr: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }
            ?.map { replaceSpaces(reformatTextWithCode(it.substringBefore("<h"))) } ?: listOf()

        val methodArgs = element.select("table.params > tbody > tr")
            .map {
                val argName = it.child(0).text()
                assert(argsMap.contains(argName))
                Variable(
                    name = argName,
                    type = argsMap[argName],
                    description = reformatTextWithCode(it.child(1).html()),
                )
            }

        return Method(
            name = methodName,
            signature = methodSignature,
            anchor = methodAnchor,
            args = methodArgs,
            description = methodDescr,
            returnType = methodReturnType,
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

    private fun reformatTextWithCode(html: String): String {
        return removeAllTags(replaceEmTags(replacePreTags(replaceCodeTags(html))))
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
}
