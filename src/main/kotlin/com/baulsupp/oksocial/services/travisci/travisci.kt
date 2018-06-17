package com.baulsupp.oksocial.services.travisci

import com.baulsupp.oksocial.credentials.DefaultToken
import com.baulsupp.oksocial.kotlin.Rest
import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.queryPages
import com.baulsupp.oksocial.services.travisci.model.Build
import com.baulsupp.oksocial.services.travisci.model.BuildList
import kotlin.math.min

suspend fun queryAllBuilds(slug: String? = null, id: Int? = null, branch: String? = null, limit: Int = 100): List<Build> {
  val repo = slug?.replace("/", "%2F") ?: id.toString()
  val url = "https://api.travis-ci.org/repo/$repo/builds?limit=${min(100, limit)}"

  return client.queryPages<BuildList>(url, {
    Rest(pagination.limit.rangeTo(pagination.count).step(pagination.limit).map { "$url&offset=$it" })
  }, DefaultToken, pageLimit = (limit / 100) + 1)
    .flatMap { it.builds }.filter { branch == null || it.branch?.name == branch }.take(limit)
}
