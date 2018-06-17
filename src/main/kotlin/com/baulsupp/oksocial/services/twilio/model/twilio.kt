package com.baulsupp.oksocial.services.twilio.model

data class AccountsItem(val subresource_uris: Map<String, String>,
                        val friendly_name: String,
                        val date_updated: String,
                        val date_created: String,
                        val owner_account_sid: String,
                        val auth_token: String,
                        val type: String,
                        val uri: String,
                        val status: String,
                        val sid: String)

data class Accounts(val first_page_uri: String? = null,
                    val start: Int,
                    val end: Int,
                    val previous_page_uri: String? = null,
                    val next_page_uri: String? = null,
                    val accounts: List<AccountsItem>,
                    val page: Int,
                    val uri: String,
                    val page_size: Int)


