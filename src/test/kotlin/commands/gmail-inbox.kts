#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.*

data class Thread(val id: String, val snippet: String, val historyId: String)
data class ThreadList(val threads: List<Thread>, val nextPageToken: String?, val resultSizeEstimate: Int)

val query = args.getOrElse(0) { "label:inbox" }

val threads = query<ThreadList>("https://www.googleapis.com/gmail/v1/users/me/threads?q=$query")

threads.threads.forEach {
  println(it.snippet)
}
