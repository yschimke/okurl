package com.baulsupp.oksocial.services.surveymonkey

import com.baulsupp.oksocial.credentials.ServiceDefinition
import java.util.Optional

import java.util.Optional.of

class SurveyMonkeyServiceDefinition : ServiceDefinition<SurveyMonkeyToken> {
    override fun apiHost(): String {
        return "api.surveymonkey.net"
    }

    override fun serviceName(): String {
        return "Survey Monkey API"
    }

    override fun shortName(): String {
        return "surveymonkey"
    }

    override fun parseCredentialsString(s: String): SurveyMonkeyToken {
        val parts = s.split(":".toRegex(), 2).toTypedArray()
        return SurveyMonkeyToken(parts[0], parts[1])
    }

    override fun formatCredentialsString(credentials: SurveyMonkeyToken): String {
        return credentials.apiKey + ":" + credentials.accessToken
    }

    override fun apiDocs(): Optional<String> {
        return of("https://developer.surveymonkey.com/api/v3/#scopes")
    }

    override fun accountsLink(): Optional<String> {
        return of("https://developer.surveymonkey.com/apps/")
    }
}
