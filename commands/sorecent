#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.services.stackexchange.model.*
import kotlinx.coroutines.experimental.runBlocking

var questions = runBlocking {
  client.query<Questions>("https://api.stackexchange.com/2.2/questions/unanswered/my-tags?order=desc&sort=creation&site=stackoverflow");
}

val titleWidth = Math.max(50, terminalWidth - 80);
for (q in questions.items) {
  val time = epochSecondsToDate(q.creation_date)
  val url = q.link.replace("(.*)/.*".toRegex(), "$1");
  println("%${titleWidth}.${titleWidth}s %-18.18s %4.4s %-10s %s".format(q.title, q.tags.joinToString(), q.answer_count, time, url));
}
