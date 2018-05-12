package com.baulsupp.oksocial.services.twilio.model

data class AccountsItem(val subresourceUris: Map<String, String>?,
                        val friendlyName: String,
                        val dateUpdated: String,
                        val dateCreated: String,
                        val ownerAccountSid: String,
                        val authToken: String,
                        val type: String,
                        val uri: String,
                        val status: String,
                        val sid: String)

data class Accounts(val firstPageUri: String? = null,
                    val start: Int,
                    val end: Int,
                    val previousPageUri: String? = null,
                    val nextPageUri: String? = null,
                    val accounts: List<AccountsItem>,
                    val page: Int,
                    val uri: String,
                    val pageSize: Int)


