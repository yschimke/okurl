package com.baulsupp.oksocial.services.microsoft.model


data class User(
  val businessPhones: List<String>?,
  val preferredLanguage: String?,
  val mail: String?,
  val mobilePhone: String?,
  val officeLocation: String?,
  val displayName: String,
  val surname: String,
  val givenName: String,
  val jobTitle: String?,
  val id: String,
  val userPrincipalName: String?)
