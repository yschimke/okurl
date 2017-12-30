package com.baulsupp.oksocial.completion

import com.google.common.io.Resources
import java.io.File
import java.nio.charset.StandardCharsets

data class UrlList(val match: Match, val urls: List<String>) {
  enum class Match {
    /**
     * An exact match that should be recalculated for any change
     */
    EXACT,
    /**
     * Site specific matches, all subpaths are provided within a site.
     */
    SITE,
    /**
     * Host selection only
     */
    HOSTS
  }

  fun getUrls(prefix: String): List<String> {
    return urls.filter { u -> u.startsWith(prefix) }
  }

  fun replace(variable: String, replacements: List<String>, keepTemplate: Boolean): UrlList {
    if (replacements.isEmpty()) {
      return this
    }

    val literalToken = "{$variable}"

    val replacementList: List<String>
    if (keepTemplate) {
      replacementList = replacements.toMutableList()
      if (keepTemplate) {
        replacementList.add(literalToken)
      }
    } else {
      replacementList = replacements
    }

    val newUrls = urls.flatMap { url ->
      if (url.contains(literalToken))
        replacementList.map { s -> url.replace(literalToken, s) }
      else
        listOf(url)
    }

    return UrlList(match, newUrls)
  }

  fun toFile(file: File, strip: Int, prefix: String) {
    val content = "${regex(prefix)}\n${urls.joinToString("\n") { u -> u.substring(strip) }}"

    file.writeText(content)
  }

  private fun regex(prefix: String): String {
    return when (match) {
      UrlList.Match.EXACT -> prefix
      UrlList.Match.HOSTS -> "[^/]*:?/?/?[^/]*"
      UrlList.Match.SITE -> prefix + ".*"
    }
  }

  fun combine(b: UrlList): UrlList {
    val newUrls = mutableListOf<String>()

    newUrls.addAll(urls)
    newUrls.addAll(b.urls)

    val newMatch: Match = if (match == b.match) {
      match
    } else {
      Match.EXACT
    }

    return UrlList(newMatch, newUrls)
  }

  override fun toString() = urls.joinToString("\n")

  companion object {
    fun fromResource(serviceName: String): UrlList? {
      return UrlList::class.java.getResource("/urls/$serviceName.txt")?.let {
        UrlList(Match.SITE, Resources.readLines(it, StandardCharsets.UTF_8))
      }
    }
  }
}
