#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.postJsonBody
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.kotlin.usage
import com.baulsupp.okurl.kotlin.args

data class Commit(val oid: String)
data class CommitNode(val commit: Commit)
data class Commits(val nodes: List<CommitNode>)
data class PullRequest(val author: Author, val title: String, val permalink: String, val updatedAt: String, val commits: Commits) {
  val commit = commits.nodes.last().commit
}
data class PullRequests(val nodes: List<PullRequest>)
data class Author(val name: String?, val login: String)
data class Repository(val pullRequests: PullRequests)
data class Data(val repository: Repository)
data class PullRequestResults(val data: Data) {
  val pullRequests = this.data.repository.pullRequests.nodes
}

data class Query(val query: String)

if (args.size < 2) {
  throw usage("github-pull-requests.kts org repo")
}

val (owner, repo) = args

val query = """
query {
  repository(name: "$repo", owner: "$owner") {
    pullRequests(first: 10, states:OPEN, orderBy:{field: UPDATED_AT, direction:DESC}) {
      nodes {
        author {
          login
          ... on User {
            name
          }
        }
        title
        permalink
        updatedAt
        commits(last:1) {
          nodes {
            commit {
              oid
            }
          }
        }
      }
    }
  }
}
"""

val results = query<PullRequestResults>(request {
  url("https://api.github.com/graphql")
  header("Accept", "application/vnd.github.antiope-preview")
  postJsonBody(Query(query))
})

results.pullRequests.forEach {
  println("Title: ${it.title}")
  println("Author: ${it.author.login}")
  println("Commit: ${it.commit.oid}")
  println()
}
