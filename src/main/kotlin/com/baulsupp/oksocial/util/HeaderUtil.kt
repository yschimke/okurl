package com.baulsupp.oksocial.util

import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.util.FileUtil.expectedFile
import com.google.common.base.Charsets
import com.google.common.io.Files
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

// TODO handle duplicate header keys
object HeaderUtil {
    fun headerMap(headers: List<String>?): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()

        headers?.forEach {
            if (it.startsWith("@")) {
                headerMap.putAll(headerFileMap(it))
            } else {
                val parts = it.split(":".toRegex(), 2).toTypedArray()
                // TODO: consider better strategy than simple trim
                val name = parts[0].trim { it <= ' ' }
                val value = stringValue(parts[1].trim { it <= ' ' })
                headerMap.put(name, value)
            }
        }

        return headerMap.toMap()
    }

    private fun headerFileMap(input: String): Map<out String, String> {
        return try {
            headerMap(Files.readLines(inputFile(input), Charsets.UTF_8))
        } catch (ioe: IOException) {
            throw UsageException("failed to read header file", ioe)
        }

    }

    fun stringValue(source: String): String {
        return if (source.startsWith("@")) {
            try {
                Files.toString(inputFile(source), StandardCharsets.UTF_8)
            } catch (e: IOException) {
                throw UsageException(e.toString())
            }

        } else {
            source
        }
    }

    fun inputFile(path: String): File {
        return expectedFile(path.substring(1))
    }
}
