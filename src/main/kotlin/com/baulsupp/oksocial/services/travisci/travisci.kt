package com.baulsupp.oksocial.services.travisci

import com.baulsupp.oksocial.credentials.DefaultToken
import com.baulsupp.oksocial.kotlin.Rest
import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.queryPages
import com.baulsupp.oksocial.services.travisci.model.Build
import com.baulsupp.oksocial.services.travisci.model.BuildList

suspend fun queryAllBuilds(slug: String? = null, id: Int? = null, branch: String? = null): List<Build> {
  val repo = slug?.replace("/", "%2F") ?: id.toString()
  val url = "https://api.travis-ci.org/repo/$repo/builds?limit=100"

  return client.queryPages<BuildList>(url, {
    Rest(pagination.limit.rangeTo(pagination.count).step(pagination.limit).map { "$url&offset=$it" })
  }, DefaultToken).flatMap { it.builds }.filter { branch == null || it.branch?.name == branch }
}
