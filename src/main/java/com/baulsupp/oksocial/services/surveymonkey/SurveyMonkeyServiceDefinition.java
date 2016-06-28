package com.baulsupp.oksocial.services.surveymonkey;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class SurveyMonkeyServiceDefinition implements ServiceDefinition<SurveyMonkeyToken> {
  @Override public String apiHost() {
    return "api.surveymonkey.net";
  }

  @Override public String serviceName() {
    return "Survey Monkey API";
  }

  @Override public String shortName() {
    return "surveymonkey";
  }

  public SurveyMonkeyToken parseCredentialsString(String s) {
    String[] parts = s.split(":", 2);
    return new SurveyMonkeyToken(parts[0], parts[1]);
  }

  public String formatCredentialsString(SurveyMonkeyToken credentials) {
    return credentials.apiKey + ":" + credentials.accessToken;
  }
}
