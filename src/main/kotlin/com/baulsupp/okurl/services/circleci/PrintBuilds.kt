package com.baulsupp.okurl.services.circleci

import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.queryList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun main() {
  val org = "square"
  val project = "okhttp"

  coroutineScope {
    val builds = builds(org, project)
    val buildNums = builds.filter { it.branch == "master" }.map { it.build_num }

    val failedTests = buildNums.map {  buildNum ->
      async {
        testMetaData(org, project, buildNum)
      }
    }.flatMap {
      it.await().failedTests
    }

    for ((testName, count) in failedTests.groupingBy { it.classname + "." + it.name }.eachCount().toSortedMap()) {
      println("$count\t$testName")
    }
  }
}

private suspend fun builds(org: String, project: String) =
  client.queryList<Build>(
    "https://circleci.com/api/v1.1/project/github/$org/$project?limit=100&filter=failed&shallow=true"
  )

private suspend fun testMetaData(org: String, project: String, buildNum: Int) =
  client.query<TestMetaData>(
    "https://circleci.com/api/v1.1/project/github/$org/$project/$buildNum/tests"
  )
