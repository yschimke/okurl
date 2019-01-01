package com.baulsupp.okurl.services.travisci

import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.kotlin.Rest
import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.queryPages
import com.baulsupp.okurl.services.travisci.model.Build
import com.baulsupp.okurl.services.travisci.model.BuildList
import kotlin.math.min

suspend fun queryAllBuilds(
  slug: String? = null,
  id: Int? = null,
  branch: String? = null,
  limit: Int = 100
): List<Build> {
  val repo = slug?.replace("/", "%2F") ?: id.toString()
  val url = "https://api.travis-ci.org/repo/$repo/builds?limit=${min(100, limit)}"

  return client.queryPages<BuildList>(url, {
    Rest(pagination.limit.rangeTo(pagination.count).step(pagination.limit).map { "$url&offset=$it" })
  }, DefaultToken, pageLimit = (limit / 100) + 1)
    .flatMap { it.builds }.filter { branch == null || it.branch?.name == branch }.take(limit)
}
