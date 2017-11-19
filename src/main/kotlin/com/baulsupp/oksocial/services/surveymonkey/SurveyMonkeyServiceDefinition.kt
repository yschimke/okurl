package com.baulsupp.oksocial.services.surveymonkey

import com.baulsupp.oksocial.credentials.ServiceDefinition

class SurveyMonkeyServiceDefinition : ServiceDefinition<SurveyMonkeyToken> {
  override fun apiHost(): String = "api.surveymonkey.net"

  override fun serviceName(): String = "Survey Monkey API"

  override fun shortName(): String = "surveymonkey"

  override fun parseCredentialsString(s: String): SurveyMonkeyToken {
    val parts = s.split(":".toRegex(), 2).toTypedArray()
    return SurveyMonkeyToken(parts[0], parts[1])
  }

  override fun formatCredentialsString(credentials: SurveyMonkeyToken) =
      credentials.apiKey + ":" + credentials.accessToken

  override fun apiDocs() = "https://developer.surveymonkey.com/api/v3/#scopes"

  override fun accountsLink() = "https://developer.surveymonkey.com/apps/"
}
