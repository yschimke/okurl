package com.baulsupp.okurl.services.facebook.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Field(val name: String, val description: String?, val type: String?)

@JsonClass(generateAdapter = true)
data class Metadata(val type: String, val fields: List<Field>, val connections: Map<String, String>)

@JsonClass(generateAdapter = true)
open class Result(open val metadata: Metadata? = null)

@JsonClass(generateAdapter = true)
data class Paging(val cursors: Cursors)

@JsonClass(generateAdapter = true)
data class Cursors(val before: String, val after: String)

@JsonClass(generateAdapter = true)
open class PageableResult<out T>(open val data: List<T>, open val paging: Paging?) : Result()

@JsonClass(generateAdapter = true)
open class IdResult(open val id: String) : Result()

@JsonClass(generateAdapter = true)
data class MetadataResult(override val metadata: Metadata?) : Result()

@JsonClass(generateAdapter = true)
data class Account(override val id: String, val username: String?) : IdResult(id)

@JsonClass(generateAdapter = true)
data class App(override val id: String, val name: String) : IdResult(id)

@JsonClass(generateAdapter = true)
data class UserOrPage(override val id: String, val name: String) : IdResult(id)

@JsonClass(generateAdapter = true)
data class AccountList(override val data: List<Account>, override val paging: Paging?) :
  PageableResult<Account>(data, paging)
