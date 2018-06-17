#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.services.travisci.queryAllBuilds
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit

// requires http://www.brewformulas.org/Travis

runBlocking {
  val oneweekago = Instant.now().minus(7, ChronoUnit.DAYS)
  val builds = queryAllBuilds("square/okhttp", limit = 200)
  val errorBuilds = builds.filter { it.isErrored && it.started_at?.isAfter(oneweekago) ?: false }

  val failingLogs = errorBuilds.map {
    async(CommonPool) {
      it.jobs.map {
        client.queryForString(it.logOutputTxt)
      }
    }
  }

  failingLogs.forEach {
    println(it.await())
  }
}
