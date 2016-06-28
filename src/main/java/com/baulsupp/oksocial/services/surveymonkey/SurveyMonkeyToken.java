package com.baulsupp.oksocial.services.surveymonkey;

public class SurveyMonkeyToken {
  public String apiKey;
  public String accessToken;

  public SurveyMonkeyToken(String apiKey, String accessToken) {
    this.apiKey = apiKey;
    this.accessToken = accessToken;
  }
}
