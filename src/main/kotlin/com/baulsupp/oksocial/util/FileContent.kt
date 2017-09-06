package com.baulsupp.oksocial.util

import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

object FileContent {

    @Throws(IOException::class)
    fun readParamBytes(param: String): ByteArray {
        if (param == "@-") {
            return IOUtils.toByteArray(System.`in`)
        }
        return if (param.startsWith("@")) {
            FileUtils.readFileToByteArray(File(param.substring(1)))
        } else {
            param.toByteArray(StandardCharsets.UTF_8)
        }
    }

    @Throws(IOException::class)
    fun readParamString(param: String): String {
        if (param == "@-") {
            return IOUtils.toString(System.`in`)
        }
        return if (param.startsWith("@")) {
            FileUtils.readFileToString(File(param.substring(1)))
        } else {
            param
        }
    }
}
