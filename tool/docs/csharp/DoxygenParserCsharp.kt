/*
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

package com.typedb.driver.tool.docs.csharp

import com.typedb.driver.tool.docs.dataclasses.Class
import com.typedb.driver.tool.docs.dataclasses.EnumConstant
import com.typedb.driver.tool.docs.dataclasses.Method
import com.typedb.driver.tool.docs.dataclasses.Variable
import com.typedb.driver.tool.docs.util.*
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
        val htmlDocsDirectoryName = inputDirectoryName + "/html/"

        val docsDir = System.getenv("BUILD_WORKSPACE_DIRECTORY")?.let { Paths.get(it).resolve(outputDirectoryName) }
            ?: Paths.get(outputDirectoryName)
        if (!docsDir.toFile().exists()) {
            Files.createDirectory(docsDir)
        }
        val classes: MutableList<Class> = ArrayList()

        // Enums that are not members of a class
        Files.walk(Paths.get(htmlDocsDirectoryName))
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().startsWith(htmlDocsDirectoryName + "namespace_type_d_b_1_1_driver_") }
            .forEach {
                val html = File(it.toAbsolutePath().toString()).readText(Charsets.UTF_8)
                val parsed = Jsoup.parse(html)
                val (_, _, parsedEnums) = parseMemberDecls(parsed)
                classes += parsedEnums
            }

        // Class (Interface, Struct) files
        File(inputDirectoryName).walkTopDown().filter {
            (it.toString().startsWith(htmlDocsDirectoryName + "class_type_")
                    || it.toString().startsWith(htmlDocsDirectoryName + "interface_type_")
                    || it.toString().startsWith(htmlDocsDirectoryName + "struct_type_"))
                    && it.toString().endsWith(".html")
                    && !it.toString().contains("-members")
        }.forEach {
            val html = File(it.path).readText(Charsets.UTF_8)
            val parsed = Jsoup.parse(html)

            val (parsedClass, nestedParsedClasses) = parseClass(parsed)
            if (parsedClass.isNotEmpty()) {
                classes.add(parsedClass)
            }
            nestedParsedClasses.forEach { element -> classes.add(element) }
        }

        classes.forEach { parsedClass ->
            val parsedClassAsciiDoc = parsedClass.toAsciiDoc("cs")
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

    private fun parseMemberDecls(document: Element):
            Triple<Map<String, List<Element>>, Map<String, String>, List<Class>> {
        val missingDeclarations: MutableList<String> = ArrayList()
        val map: MutableMap<String, List<Element>> = HashMap()
        val idToAnchor: MutableMap<String, String> = HashMap()
        val nestedClasses: MutableList<Class> = ArrayList()

        document.select("table.memberdecls").forEach { table ->
            val heading: String = table.selectFirst("tr.heading > td > h2 > a")!!.attr("name")
            val members: MutableList<Element> = ArrayList()

            table.select("tr")
                .filter { element ->
                    element.className().matches(Regex("memitem:[a-f0-9]+"))
                }.forEach { element ->
                    val id = element.className().substringAfter("memitem:")
                    val type = element.selectFirst("td.memItemLeft")?.text()

                    val memberDetails =
                        document.selectFirst("div.contents > a#$id")?.nextElementSibling()?.nextElementSibling()

                    if (memberDetails == null) {
                        missingDeclarations.add(element.selectFirst("td.memItemRight")!!.text())
                    } else {
                        if (type == "enum") { // Enums that are members of a class
                            val parsedEnum = parseEnum(element, memberDetails)
                            if (parsedEnum.isNotEmpty()) {
                                nestedClasses.add(parsedEnum)
                            }
                        } else {
                            members.add(memberDetails)
                            idToAnchor[id] = replaceSymbolsForAnchor(memberDetails.select("table.memname").text())
                        }
                    }
                }

            map[heading] = members
        }

        if (missingDeclarations.isNotEmpty()) { // Ignore the methods that are @private.
            println("Missing some member declarations:\n\t-" + missingDeclarations.joinToString("\n\t-"))
        }

        return Triple(map, idToAnchor, nestedClasses)
    }

    private fun parseClass(document: Element): Pair<Class, List<Class>> {
        // If we want inherited members, consider doxygen's INLINE_INHERITED_MEMB instead of the javadoc approach
        val fullyQualifiedName = formatEntityName(document.selectFirst("div .title")!!.text())
        val packagePath = fullyQualifiedName.substringBeforeLast(".")
        val className = fullyQualifiedName.substringAfterLast(".")
        val classAnchor = replaceSymbolsForAnchor(className)
        val classExamples = document.select("div.textblock > pre").map { replaceSpaces(it.text()) }
        val superClasses = document.select("tr.inherit_header")
            .map { it.text().substringAfter("inherited from ") }
            .toSet().toList()

        val (memberDecls, idToAnchor, nestedClasses) = parseMemberDecls(document)
        val classDescr: List<String> = document.selectFirst("div.textblock")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h"), idToAnchor) }
            ?: listOf()

        val fields = memberDecls.getOrDefault("pub-attribs", listOf()).map { parseField(it, idToAnchor) }
        val methods: List<Method> = (
                memberDecls.getOrDefault("pub-methods", listOf()) +
                        memberDecls.getOrDefault("pub-static-methods", listOf()) +
                        memberDecls.getOrDefault("properties", listOf())
                ).map {
                parseMethod(it, idToAnchor)
            }

        return Pair(
            Class(
                name = className,
                anchor = classAnchor,
                description = classDescr,
                examples = classExamples,
                fields = fields,
                methods = methods,
                packagePath = packagePath,
                superClasses = superClasses
            ),
            nestedClasses
        )
    }

    private fun parseEnum(constantsElement: Element, detailsElement: Element): Class {
        val typeAndName = detailsElement
            .selectFirst("div.memitem > div.memproto > table.memname > tbody > tr > td.memname")
            ?.text()?.split(" ")!!
        assert(typeAndName.size > 1)

        val fullyQualifiedName = typeAndName[1]
        val className = fullyQualifiedName.substringAfterLast(".")
        val classAnchor = replaceSymbolsForAnchor(className)
        val packagePath = fullyQualifiedName.substringBeforeLast(".")

        val classDescr: List<String> = detailsElement.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }?.map { reformatTextWithCode(it.substringBefore("<h"), HashMap()) }
            ?: listOf()
        val classExamples = detailsElement.select("div.memdoc > pre").map { replaceSpaces(it.text()) }

        val enumConstantsText = constantsElement
            .selectFirst("td.memItemRight")
            ?.text()!!
        val enumConstants = Regex("[A-Za-z]+ = [A-Za-z.]+")
            .findAll(enumConstantsText).map { EnumConstant(it.value.substringBefore(" ")) }.toList()

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
        val methodSignature = updateSignature(element.selectFirst("table.memname")!!.text())
        val argsList = getArgsFromSignature(methodSignature)
        val argsMap = argsList.associate { (first, second) ->
            Pair(
                addZeroWidthWhitespaces(first),
                addZeroWidthWhitespaces(second)
            )
        }
        val methodReturnType = getReturnTypeFromSignature(methodSignature)
        val methodDescr: List<String> = element.selectFirst("div.memdoc")
            ?.let { splitToParagraphs(it.html()) }
            ?.map { replaceSpaces(reformatTextWithCode(it.substringBefore("<h").substringBefore("<dl class=\"params\">"), idToAnchor)) } ?: listOf()
        val methodExamples = element.select("div.memdoc > pre").map { replaceSpaces(it.text()) }

        val methodArgs = element.select("table.params > tbody > tr")
            .map {
                val argName = it.child(0).text()
                assert(argsMap.contains(argName))
                Variable(
                    name = addZeroWidthWhitespaces(argName),
                    description = reformatTextWithCode(it.child(1).html(), idToAnchor),
                    type = argsMap[argName],
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
            name = addZeroWidthWhitespaces(name),
            description = descr,
            type = addZeroWidthWhitespaces(type),
        )
    }

    private fun getArgsFromSignature(methodSignature: String): List<Pair<String, String>> {
        val splittedArgsAndTypes = methodSignature
            .replace("\\s+".toRegex(), " ")
            .substringAfter("(").substringBefore(")")
            .split(",\\s".toRegex()).toMutableList()

        var i = 0
        while (true) {
            if (i >= splittedArgsAndTypes.lastIndex) {
                break
            }

            if (splittedArgsAndTypes[i].contains("<") && !splittedArgsAndTypes[i].contains(">")) {
                splittedArgsAndTypes[i] = splittedArgsAndTypes[i] + ", " + splittedArgsAndTypes[i + 1]
                splittedArgsAndTypes.removeAt(i + 1)
            } else {
                ++i
            }
        }

        return splittedArgsAndTypes.map { arg ->
            arg.split("\\s".toRegex()).let { it.last() to it.dropLast(1).joinToString(" ") }
        }.filter { it.first.isNotEmpty() || it.second.isNotEmpty() }
            .toList()
    }

    private fun reformatTextWithCode(html: String, idToAnchor: Map<String, String>): String {
        return formatSeeAlso(
            replaceHtmlSymbols(
                removeHyperlinkFormatting(
                    removeAllTags(
                        replaceLocalLinks(
                            idToAnchor,
                            replaceEmTags(
                                replacePreTags(
                                    replaceCodeTags(html)
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    private fun formatSeeAlso(html: String): String {
        val oldSeeAlso = "  See also"

        var updatedHtml = html
        var seeAlsoIndex = -1

        while (true) {
            seeAlsoIndex = updatedHtml.indexOf(oldSeeAlso, seeAlsoIndex + 1)
            if (seeAlsoIndex == -1) {
                break
            }

            val seeAlsoEndIndex = seeAlsoIndex + oldSeeAlso.length
            val seeTargetEnd = Regex("([^\\s])+\n").find(updatedHtml, seeAlsoEndIndex)
            val seeTargetEndIndex = seeTargetEnd?.range?.endInclusive
            if (seeTargetEndIndex != null) {
                updatedHtml = StringBuilder(updatedHtml).apply { insert(seeTargetEndIndex + 1, "----") }.toString()
                seeAlsoIndex = seeTargetEndIndex + "----".length

                val seeTargetStart = Regex("\n  [^\\s\\n]+").find(updatedHtml, seeAlsoEndIndex)
                if (seeTargetStart != null) {
                    updatedHtml = updatedHtml.replace(seeTargetStart.value, seeTargetStart.value.replace("  ", ""))
                }
            }
        }

        return updatedHtml.replace(oldSeeAlso, "\nSee also\n[source,cs]\n----")
    }

    private fun removeHyperlinkFormatting(html: String): String {
        var updatedHtml = html

        val regex = Regex("<<#_[a-zA-Z_]+,.*>>")
        regex.findAll(html).map { it.value }.forEach {
            val newValue = it
                .substringAfter("<<#_")
                .substringBeforeLast(">>")
                .split(",").drop(1).joinToString(",")
            updatedHtml = updatedHtml.replace(it, newValue)
        }

        return updatedHtml
    }

    private fun replacePreTags(html: String): String {
        return html.replace("<pre>", "[source,cs]\n----\n").replace("</pre>", "\n----\n")
    }

    private fun updateSignature(signature: String): String {
        var enhanced = replaceSpaces(signature)
        enhanced = enhanced
            .replace("( ", "(")
            .replace("< ", "<")
            .replace(" >", ">")
        enhanced = Regex("\\s([()*&])").replace(enhanced, "$1")
        val splitted = enhanced.split(" ").toMutableList()
        val methodNameIndex = splitted.indexOfFirst { it.contains('(') }
        if (methodNameIndex != -1) {
            splitted[methodNameIndex] = splitted[methodNameIndex].substringAfterLast(".")
        }
        return splitted.joinToString(separator = " ")
            .replace("<", "< ")
            .replace(">", " >")
    }

    private fun getReturnTypeFromSignature(signature: String): String {
        return signature.replace("static ", "")
            .replace(", ", ",").replace("< ", "<").replace(" >", ">")
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
        return className.replace("[<> ,]".toRegex(), "_")
    }

    private fun formatEntityName(entityName: String): String {
        return entityName.replace(Regex("(Class|Interface|Struct)(?: Template)? Reference.*"), "").trim()

    }
}
