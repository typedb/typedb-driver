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

import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import com.vaticle.typedb.client.tool.doc.common.Argument
import com.vaticle.typedb.client.tool.doc.common.Class
import com.vaticle.typedb.client.tool.doc.common.Method

fun main(args: Array<String>) {
    val outputFilename = args[0]
    val inputJarName = args[1]
    println("Input: $inputJarName")

    val outputFile = File(outputFilename)
    outputFile.createNewFile()

//    File(inputDirectoryName).walkTopDown().forEach {
//        println(it)
////        if (it.toString().endsWith(".html")) {
////            val html = it.readText(Charsets.UTF_8)
////            val parsed = Jsoup.parse(html)
////
////            parsed.select("dl.class").forEach {
////                val parsedClass = parseClassPy(it)
////                println(parsedClass)
////                outputFile.appendText(parsedClass.toString() + "\n")
////            }
////        }
//        outputFile.appendText(it.readText())
//    }

    unzipFile(Path.of(inputJarName), Path.of(".")).forEach {
        println(it)
//        if (it.toString().contains("struct.")) {
//            val html = File(it.toUri()).readText(Charsets.UTF_8)
//            val parsed = Jsoup.parse(html)
//            if (!parsed.select(".main-heading h1 a.struct").isNullOrEmpty()) {
//                val parsedClass = parseClass(parsed)
//                print(parsedClass)
//                outputFile.appendText(parsedClass.toString() + "\n")
//            }
//        }
    }
}


//fun parseClassPy(element: Element): Class {
//    val classSigElement = element.selectFirst("dt.sig-object")
//    val className = classSigElement!!.selectFirst("dt.sig-object span.sig-name")!!.text()
//
//    val classDetails = classSigElement.nextElementSibling()
//    val classDetailsParagraphs = classDetails!!.children().map { it }.filter { it.tagName() == "p" }  // FIXME
//    val (descr, bases) = classDetailsParagraphs.partition { it.select("code.py-class").isNullOrEmpty() }
//    val classBases = bases[0]!!.select("span").map { it.html() }
//    val classDescr = descr.map { it.html() }
//
//    val methodsDetails = classDetails.children().map { it }.filter { it.classNames().contains("method") }
//    val methods = methodsDetails.map { parseMethodPy(it) }
//
//    val propertiesDetails = classDetails.children().map { it }.filter { it.classNames().contains("property") }
//    val properties = propertiesDetails.map { parsePropertyPy(it) }
//
//    return Class(
//        name = className,
//        description = classDescr,
//        methods = methods,
//        fields = properties,
//        bases = classBases,
//    )
//}
//
//fun parseMethodPy(element: Element): Method {
//    val methodSignature = element.selectFirst("dt.sig-object")!!.text()
//    val methodName = element.selectFirst("dt.sig-object span.sig-name")!!.text()
//    val allArgs = getArgsFromSignaturePy(element.selectFirst("dt.sig-object")!!)
//    val methodReturnType = element.select(".sig-return-typehint").text()
//    val methodDescr = element.select("dd > p").map { it.html() }
//    val methodArgs = mutableListOf<Argument>()
//    var methodReturnDescr: String? = null
//    if (!element.select(".field-list").isNullOrEmpty()) {
//        element.select(".field-list > dt").forEach {
//            if (it.textNodes().map { it.text() }.contains("Parameters")) {
//                it.nextElementSibling()!!.select("li p").forEach {
//                    val arg_name = it.selectFirst("strong")!!.text()
//                    assert(allArgs.contains(arg_name))
//                    val arg_descr = it.textNodes().joinToString()
//                    methodArgs.add(Argument(name = arg_name, type = allArgs[arg_name], description = arg_descr))
//                }
//            }
//            if (it.textNodes().map { it.text() }.contains("Returns")) {
//                methodReturnDescr = it.nextElementSibling()?.select("p")?.text()
//            }
//        }
//    }
//    val methodExample = element.selectFirst("#examples .highlight")?.text()
//
//    return Method(
//        name = methodName,
//        signature = methodSignature,
//        description = methodDescr,
//        args = methodArgs,
//        returnType = methodReturnType,
//        returnDescription = methodReturnDescr,
//        example = methodExample,
//    )
//
//}
//
//fun parsePropertyPy(element: Element): Argument {
//    val propertyName = element.selectFirst("dt.sig-object span.sig-name")!!.text()
//    val propertyType = element.selectFirst("dt.sig-object span.sig-name + .property ")?.text()?.dropWhile { !it.isLetter() }
//    val propertyDescr = element.select("dd > p").map { it.html() }.joinToString()  // TODO: Test it
//    return Argument(
//        name = propertyName,
//        type = propertyType,
//        description = propertyDescr,
//    )
//}
//
//fun getArgsFromSignaturePy(methodSignature: Element): Map<String, String?> {
//    return methodSignature.select(".sig-param").map {
//        it.selectFirst(".n")!!.text() to it.select(".p + .w + .n").text()
//    }.toMap()
//}

fun unzipFile(path: Path, into: Path, strip: Int = 0, delete: Boolean = true) = sequence {
    val stream = ZipInputStream(FileInputStream(path.toFile()))
    val buffer = ByteArray(1024)
    stream.use {
        it.asIterable().forEach { entry ->
            val destination = Path.of(entry.name)
            val sanitizedDestination = if (destination.nameCount > strip) {
                destination.subpath(strip, destination.nameCount)
            } else destination

            val finalDestination = into.resolve(sanitizedDestination)
            if (finalDestination.parent?.let { parent -> !Files.exists(parent) } == true) {
                Files.createDirectories(finalDestination.parent)
            }
            if (entry.isDirectory) {
                if (sanitizedDestination.nameCount > strip) {
                    if (!Files.exists(finalDestination)) {
                        Files.createDirectories(finalDestination)
                    }
                }
            } else {
                Files.createFile(finalDestination)
                FileChannel.open(finalDestination, StandardOpenOption.WRITE).use { writer ->
                    while (it.read(buffer) > 0) {
                        writer.write(ByteBuffer.wrap(buffer))
                    }
                }
                yield(finalDestination)
            }
        }
    }

    if (delete) {
        Files.deleteIfExists(path)
    }
}

private fun ZipInputStream.asIterable(): Iterable<ZipEntry> = object : Iterable<ZipEntry> {
    override fun iterator(): Iterator<ZipEntry> = ZipIterator(this@asIterable)
}

private class ZipIterator(private val stream: ZipInputStream) : Iterator<ZipEntry> {
    private var next: ZipEntry? = null
    override fun hasNext(): Boolean {
        next = stream.nextEntry
        return next != null
    }

    override fun next(): ZipEntry = next ?: throw NoSuchElementException()
}
