package com.baulsupp.oksocial.services.postman.model

data class User(val id: String)

data class UserResult(val user: User)

data class Collection(val id: String, val name: String, val owner: String, val uid: String)

data class CollectionsResult(val collections: List<Collection>)