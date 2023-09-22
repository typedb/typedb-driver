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

package com.vaticle.typedb.client.tool.doc.common

import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

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
