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

package com.vaticle.typedb.client.tool.doc.nodejs

import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import com.vaticle.typedb.client.tool.doc.common.Argument
import com.vaticle.typedb.client.tool.doc.common.Class
import com.vaticle.typedb.client.tool.doc.common.EnumConstant
import com.vaticle.typedb.client.tool.doc.common.Method
import com.vaticle.typedb.client.tool.doc.common.mergeClasses
import com.vaticle.typedb.client.tool.doc.common.removeAllTags
import com.vaticle.typedb.client.tool.doc.common.replaceCodeTags
import com.vaticle.typedb.client.tool.doc.common.replaceEmTags
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val inputDirectoryName = args[0]
    val outputDirectoryName = args[1]

    val docsDir = Paths.get(outputDirectoryName)
    Files.createDirectory(docsDir)

    val parsedClasses: HashMap<String, Class> = hashMapOf()
    File(inputDirectoryName).walkTopDown().filter {
        it.toString().contains("/classes/") || it.toString().contains("/interfaces/")
                || it.toString().contains("/modules/")
    }.forEach {
        val html = it.readText(Charsets.UTF_8)
        val parsed = Jsoup.parse(html)
        val title = parsed.select(".tsd-page-title h1")
        val parsedClass = if (!title.isNullOrEmpty() && (title.text().contains("Class") || title.text().contains("Interface"))) {
            parseClass(parsed)
        } else {
            parseNamespace(parsed)
        }

        parsedClasses[parsedClass.name] = if (parsedClasses.contains(parsedClass.name)) {
            mergeClasses(parsedClasses[parsedClass.name]!!, parsedClass)
        } else {
            parsedClass
        }

        val parsedClassAsciiDoc = parsedClasses[parsedClass.name]!!.toAsciiDoc("nodejs")
        val outputFile = docsDir.resolve("${parsedClass.name}.adoc").toFile()
        outputFile.createNewFile()
        outputFile.writeText(parsedClassAsciiDoc)
    }
}

fun parseClass(document: Element): Class {
    val className = document.selectFirst(".tsd-page-title h1")!!.textNodes().first()!!.text().split(" ", limit = 2)[1]
    val classDescr = document.select(".tsd-page-title + section.tsd-comment div.tsd-comment p").map {
        reformatTextWithCode(it.html())
    }

    val superClasses = document.select("ul.tsd-hierarchy li:has(ul.tsd-hierarchy span.target)").map {
        it.child(0).text()
    }

    val propertiesElements = document.select("section.tsd-member-group:contains(Properties)")
    val properties = propertiesElements.select("section.tsd-member:not(.tsd-is-private)").map {
        parseProperty(it)
    }

    val methodsElements = document.select("section.tsd-member-group:contains(Constructors), " +
            "section.tsd-member-group:contains(Method)")
    val methods = methodsElements.select("section.tsd-member > .tsd-signatures > .tsd-signature").map {
        parseMethod(it)
    }.filter {
        it.name != "proto"
    } + document.select("section.tsd-member-group:contains(Accessors)")
        .select("section.tsd-member > .tsd-signatures > .tsd-signature").map {
            parseAccessor(it)
        }

    return Class(
        name = className,
        description = classDescr,
        fields = properties,
        methods = methods,
        superClasses = superClasses,
    )
}

fun parseNamespace(document: Element): Class {
    val className = document.selectFirst(".tsd-page-title h1")!!.text().split(" ")[1]
    val classDescr = document.select(".tsd-page-title + section.tsd-comment div.tsd-comment p").map {
        reformatTextWithCode(it.html())
    }

    val variables = document.select(".tsd-index-heading:contains(Variables) + .tsd-index-list a").map {
        EnumConstant(name = it.text())
    }

    return Class(
        name = className,
        description = classDescr,
        enumConstants = variables,
    )
}

fun parseMethod(element: Element): Method {
    val methodSignature = element.text()
    val methodName = element.selectFirst(".tsd-kind-call-signature, .tsd-kind-constructor-signature")!!.text()
    val descrElement = element.nextElementSibling()
    val methodReturnType = descrElement!!
        .selectFirst(".tsd-description > .tsd-returns-title:not(.tsd-parameter-signature .tsd-returns-title)")
        ?.text()?.substringAfter("Returns ")
    val methodDescr = descrElement.select(".tsd-description > .tsd-comment p").map { reformatTextWithCode(it.html()) }
    val methodExamples = descrElement.select(".tsd-description > .tsd-comment > :has(a[href*=examples]) + pre > :not(button)")
        .map { it.text() }

    val methodArgs = descrElement.select(".tsd-description > .tsd-parameters > .tsd-parameter-list > " +
            "li:not(.tsd-parameter-signature li)").map {
        Argument(
            name = it.selectFirst(".tsd-kind-parameter")!!.text(),
            type = it.selectFirst("h5")!!.text().substringAfter(": "),
            description = it.selectFirst(".tsd-comment")?.let { reformatTextWithCode(it.html()) },
        )
    }

    return Method(
        name = methodName,
        signature = methodSignature,
        args = methodArgs,
        description = methodDescr,
        examples = methodExamples,
        returnType = methodReturnType,
    )
}

fun parseAccessor(element: Element): Method {
    val methodSignature = element.text()
    val methodName = element.selectFirst(".tsd-signature")!!.textNodes().first()!!.text()
    val descrElement = element.nextElementSibling()
    val methodReturnType = descrElement!!.select(".tsd-returns-title > *")
        .joinToString("") { it.text() }
    val methodDescr = descrElement.select(".tsd-description > .tsd-comment p").map { reformatTextWithCode(it.html()) }
    val methodExamples = descrElement
        .select(".tsd-description > .tsd-comment > :has(a[href*=examples]) + pre > :not(button)")
        .map { it.text() }

    return Method(
        name = methodName,
        signature = methodSignature,
        description = methodDescr,
        examples = methodExamples,
        returnType = methodReturnType,
    )
}

fun parseProperty(element: Element): Argument {
    val name = element.selectFirst(".tsd-signature span.tsd-kind-property")!!.text()
    val type = element.selectFirst(".tsd-signature .tsd-signature-type")?.text()
    val descr = element.selectFirst(".tsd-signature + .tsd-comment")?.text()
    return Argument(
        name = name,
        description = descr,
        type = type,
    )
}

fun reformatTextWithCode(html: String): String {
    return removeAllTags(replaceEmTags(replaceCodeTags(html)))
}
