package com.baulsupp.oksocial.services.facebook.model

data class Field(val name: String, val description: String, val type: String)

data class Metadata(val type: String, val fields: List<Field>, val connections: Map<String, String>)

open class Result (open val metadata: Metadata? = null)

data class Paging(val cursors: Cursors)

data class Cursors(val before: String, val after: String)

open class PageableResult<T> (open val data: List<T>, open val paging: Paging): Result()

open class IdResult (open val id: String): Result()

data class MetadataResult(override val metadata: Metadata?) : Result()

data class Account(override val id: String, val username: String) : IdResult(id)

data class App(override val id: String, val name: String) : IdResult(id)

data class UserOrPage(override val id: String, val name: String) : IdResult(id)

data class AccountList(override val data: List<Account>, override val paging: Paging) :
        PageableResult<Account>(data, paging)
