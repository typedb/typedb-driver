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

package com.typedb.driver.tool.docs.examples

import picocli.CommandLine
import picocli.CommandLine.Parameters
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Callable
import kotlin.system.exitProcess


fun main(args: Array<String>): Unit = exitProcess(CommandLine(MarkdownCodeUpdater()).execute(*args))

@CommandLine.Command(name = "MarkdownCodeUpdater", mixinStandardHelpOptions = true)
class MarkdownCodeUpdater : Callable<Unit> {

    @Parameters(paramLabel = "<inputMarkdown>", description = ["Input file containing the code block to insert"])
    private lateinit var inputFileName: String

    @CommandLine.Option(names = ["--output", "-o"], required = true, description = ["Output markdown file"])
    private lateinit var outputMarkdownFileName: String

    @CommandLine.Option(names = ["--start-marker", "-sm"], required = true, description = ["Marker indicating where to start replacing the code block in the output markdown file. Expected file's format: <!-- start-marker -->"])
    private lateinit var startMarker: String

    @CommandLine.Option(names = ["--end-marker", "-em"], required = true, description = ["Marker indicating where to end replacing the code block in the output markdown file. Expected file's format: <!-- end-marker -->"])
    private lateinit var endMarker: String

    @CommandLine.Option(names = ["--language", "-l"], required = false, defaultValue = "", description = ["Markdown language tag"])
    private lateinit var language: String

    companion object {
        const val MARKDOWN_COMMENT_START = "<!-- "
        const val MARKDOWN_COMMENT_END = " -->"
    }

    override fun call() {
        val inputFile = File(inputFileName)
        if (!inputFile.exists()) {
            throw IllegalArgumentException("Input file does not exist.")
        }
        val inputLines = inputFile.readLines().toList().dropLastWhile { it.isBlank() }

        val outputFile = File(outputMarkdownFileName)
        val outputLines = outputFile.readLines()

        val markdownStart = MARKDOWN_COMMENT_START + startMarker + MARKDOWN_COMMENT_END
        val startIndex = outputLines.indexOfFirst { it.contains(markdownStart) }
        val markdownEnd = MARKDOWN_COMMENT_START + endMarker + MARKDOWN_COMMENT_END
        val endIndex = outputLines.indexOfFirst { it.contains(markdownEnd) }

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw IllegalArgumentException("Markers not found or have invalid positions: $markdownStart($startIndex) - $markdownEnd($endIndex)")
        }

        val beforeCodeBlock = outputLines.subList(0, startIndex + 1)
        val afterCodeBlock = outputLines.subList(endIndex, outputLines.size)
        val newCodeBlock = listOf("\n```$language") + inputLines + "```\n"

        val updatedContent = beforeCodeBlock + newCodeBlock + afterCodeBlock
        Files.write(outputFile.toPath(), updatedContent)

        println("Code block updated successfully in $outputMarkdownFileName.")
    }
}
