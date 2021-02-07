package com.baulsupp.okurl.okio

import okio.ExperimentalFileSystem
import okio.FileMetadata
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.source
import java.io.InputStream

@OptIn(ExperimentalFileSystem::class)
class ClasspathFilesystem: FileSystem() {
  override fun appendingSink(file: Path): Sink {
    throw IOException("read only")
  }

  override fun atomicMove(source: Path, target: Path) {
    throw IOException("read only")
  }

  override fun canonicalize(path: Path): Path {
    return "".toPath() / path
  }

  override fun createDirectory(dir: Path) {
    throw IOException("read only")
  }

  override fun delete(path: Path) {
    throw IOException("read only")
  }

  override fun list(dir: Path): List<Path> {
    throw IOException("not listable")
  }

  override fun metadataOrNull(path: Path): FileMetadata? {
    TODO()
  }

  override fun sink(file: Path): Sink {
    throw IOException("read only")
  }

  override fun source(file: Path): Source {
    val resourceName = file.toString().substring(1)

    val stream: InputStream = this.javaClass.classLoader.getResourceAsStream(resourceName)
      ?: throw FileNotFoundException("file not found: $file")

    return stream.source()
  }
}
