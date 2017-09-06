package com.baulsupp.oksocial.completion

import com.google.common.collect.Lists
import com.google.common.io.Files
import com.google.common.io.Resources
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Optional
import java.util.function.Function
import java.util.stream.Stream

import java.util.stream.Collectors.joining
import java.util.stream.Collectors.toList

class UrlList(private val match: Match, private val urls: List<String>) {
    enum class Match {
        EXACT, SITE, HOSTS
    }

    fun getUrls(prefix: String): List<String> {
        return urls.stream().filter { u -> u.startsWith(prefix) }.collect<List<String>, Any>(toList())
    }

    fun replace(variable: String, replacements: MutableList<String>, keepTemplate: Boolean): UrlList {
        if (replacements.isEmpty()) {
            return this
        }

        val regexToken = "\\{$variable\\}"
        val literalToken = "{$variable}"

        val replacementList: MutableList<String>
        if (keepTemplate) {
            replacementList = Lists.newArrayList(replacements)
            if (keepTemplate) {
                replacementList.add(literalToken)
            }
        } else {
            replacementList = replacements
        }

        val replacementFunction = { url ->
            if (url.contains("{"))
                replacementList.stream().map { s -> url.replace(regexToken.toRegex(), s) }
            else
                Stream.of<String>(url)
        }

        val newUrls = urls.stream().flatMap(replacementFunction).collect<List<String>, Any>(toList())

        return UrlList(match, newUrls)
    }

    @Throws(IOException::class)
    fun toFile(file: File, strip: Int, prefix: String) {
        val content = regex(prefix) + "\n" + urls.stream()
                .map { u -> u.substring(strip) }
                .collect<String, *>(joining("\n"))

        Files.write(content, file, StandardCharsets.UTF_8)
    }

    private fun regex(prefix: String): String {
        when (match) {
            UrlList.Match.EXACT -> return prefix
            UrlList.Match.HOSTS -> return "[^/]*:?/?/?[^/]*"
            UrlList.Match.SITE -> return prefix + ".*"
            else -> throw IllegalArgumentException()
        }
    }

    fun combine(b: UrlList): UrlList {
        val newUrls = Lists.newArrayList<String>()

        newUrls.addAll(urls)
        newUrls.addAll(b.urls)

        val newMatch: Match
        if (match == b.match) {
            newMatch = match
        } else {
            newMatch = Match.EXACT
        }

        return UrlList(newMatch, newUrls)
    }

    override fun toString(): String {
        return urls.stream().collect<String, *>(joining("\n"))
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is UrlList) {
            return false
        }

        val other = obj as UrlList?

        return other.match == this.match && other.urls == this.urls
    }

    override fun hashCode(): Int {
        return match.hashCode() xor urls.hashCode()
    }

    companion object {

        @Throws(IOException::class)
        fun fromResource(serviceName: String): Optional<UrlList> {
            val url = UrlList::class.java.getResource("/urls/$serviceName.txt")
            return if (url != null) {
                Optional.of(
                        UrlList(Match.SITE, Resources.readLines(url, StandardCharsets.UTF_8)))
            } else {
                Optional.empty()
            }
        }
    }
}
