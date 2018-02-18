package com.baulsupp.oksocial.services.travisci.model

data class User(val id: String, val login: String, val name: String, val github_id: String?, val avatar_url: String, val is_syncing: Boolean, val synced_at: String)

data class Repository(val id: String, val name: String, val slug: String)

// TODO add pagination
data class RepositoryList(val repositories: List<Repository>)