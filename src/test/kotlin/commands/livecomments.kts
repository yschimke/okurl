#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.sse.messageHandler
import com.baulsupp.oksocial.sse.newSse
import okhttp3.HttpUrl
import org.fusesource.jansi.Ansi

val videoId = args.getOrNull(0) ?: throw UsageException("supply videoid")

// https://developers.facebook.com/docs/graph-api/server-sent-events/endpoints/live-comments
val uri = HttpUrl.parse(
  "https://streaming-graph.facebook.com/$videoId/live_comments?comment_rate=ten_per_second&fields=message")!!

data class From(val id: String, val name: String)
data class Comment(val view_id: Long, val from: From?, val message: String, val id: String)

// https://developers.facebook.com/docs/graph-api/server-sent-events/endpoints/live-reactions
val uri2 = HttpUrl.parse(
  "https://streaming-graph.facebook.com/$videoId/live_reactions?fields=reaction_stream")!!

data class ReactionCounts(
  val LIKE: Int,
  val LOVE: Int,
  val WOW: Int,
  val HAHA: Int,
  val SAD: Int,
  val ANGRY: Int
) {
  fun String.rep(count: Int): String {
    return this.repeat(Math.max(0, count))
  }

  operator fun minus(element: ReactionCounts): ReactionCounts {
    return ReactionCounts(this.LIKE - element.LIKE, this.LOVE - element.LOVE,
      this.WOW - element.WOW, this.HAHA - element.HAHA, this.SAD - element.SAD,
      this.ANGRY - element.ANGRY)
  }

  fun isNonZero() =
    LIKE != 0 || LOVE != 0 || WOW != 0 || HAHA != 0 || SAD != 0 || ANGRY != 0

  override fun toString(): String {
    return "👍".rep(LIKE) + "❤️".rep(LOVE) + "😲".rep(WOW) + "😂".rep(HAHA) + "😢".rep(SAD) + "😡".rep(ANGRY)
  }
}
data class ReactionCount(val key: String, val value: Int)
data class Reactions(val view_id: Long, val reaction_stream: List<ReactionCount>) {
  fun counts(): ReactionCounts {
    return ReactionCounts(
      count("LIKE"),
      count("LOVE"),
      count("WOW"),
      count("HAHA"),
      count("SAD"),
      count("ANGRY")
    )
  }

  private fun count(s: String) = reaction_stream.find { it.key == s }?.value ?: 0
}

val commentSource = client.newSse(messageHandler<Comment> {
  println("${it.message} (${it.from?.name?.color(Ansi.Color.CYAN)})")
}, uri)

var counts: ReactionCounts? = null

val reactionSource = client.newSse(messageHandler<Reactions> {
  val new_counts = it.counts()
  counts?.let { old ->
    val delta = new_counts - old
    if (delta.isNonZero()) {
      println(delta)
    }
  }
  counts = new_counts
}, uri2)

Thread.sleep(600000)

commentSource.cancel()
reactionSource.cancel()
