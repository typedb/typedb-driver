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
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess


fun main(args: Array<String>): Unit = exitProcess(CommandLine(TestExamplesParser()).execute(*args))

@CommandLine.Command(name = "TestExamplesParser", mixinStandardHelpOptions = true)
class TestExamplesParser : Callable<Unit> {
    @Parameters(paramLabel = "<input>", description = ["Input test file"])
    private lateinit var inputTestFileName: String

    @CommandLine.Option(names = ["--output", "-o"], required = true)
    private lateinit var outputFileName: String

    /**
     * --remove=string: remove lines where this string is encountered
     */
    @CommandLine.Option(names = ["--remove", "-r"], required = false)
    private lateinit var removedLines: HashSet<String>

    override fun call() {
        val outputFile = System.getenv("BUILD_WORKSPACE_DIRECTORY")?.let { Paths.get(it).resolve(outputFileName) }
            ?: Paths.get(outputFileName)

        if (!Files.exists(outputFile)) {
            Files.createFile(outputFile)
        }

        val inputFile = File(inputTestFileName)
        val outputLines = mutableListOf<String>()
        var insideExampleBlock = false
        val currentBlockLines = mutableListOf<String>()
        var minIndentation = Int.MAX_VALUE

        inputFile.forEachLine { line ->
            when {
                line.contains("EXAMPLE START MARKER") -> {
                    insideExampleBlock = true
                    currentBlockLines.clear()
                    minIndentation = Int.MAX_VALUE
                }
                line.contains("EXAMPLE END MARKER") -> {
                    insideExampleBlock = false
                    val adjustedBlock = adjustIndentation(currentBlockLines, minIndentation)
                    outputLines.addAll(adjustedBlock)
                }
                insideExampleBlock -> {
                    if (removedLines.none { line.contains(it) }) {
                        if (line.isNotBlank()) {
                            val indentation = line.takeWhile { it == ' ' }.length
                            if (indentation < minIndentation) {
                                minIndentation = indentation
                            }
                        }
                        currentBlockLines.add(line)
                    }
                }
            }
        }

        Files.write(outputFile, outputLines)
    }

    private fun adjustIndentation(blockLines: List<String>, minIndentation: Int): List<String> {
        return blockLines.map { line ->
            if (line.isNotBlank() && minIndentation != Int.MAX_VALUE) {
                line.drop(minIndentation)
            } else {
                line
            }
        }
    }
}
