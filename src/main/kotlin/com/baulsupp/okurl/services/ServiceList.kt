package com.baulsupp.okurl.services

import com.baulsupp.okurl.authenticator.AuthInterceptor

object ServiceList {
  @Suppress("UNCHECKED_CAST")
  fun defaultServices(): List<AuthInterceptor<*>> =
    listOf(
      com.baulsupp.okurl.services.atlassian.AtlassianAuthInterceptor(),
      com.baulsupp.okurl.services.basicauth.BasicAuthInterceptor(),
      com.baulsupp.okurl.services.box.BoxAuthInterceptor(),
      com.baulsupp.okurl.services.circleci.CircleCIAuthInterceptor(),
      com.baulsupp.okurl.services.cirrusci.CirrusCiAuthInterceptor(),
      com.baulsupp.okurl.services.citymapper.CitymapperAuthInterceptor(),
      com.baulsupp.okurl.services.coinbase.CoinbaseAuthInterceptor(),
      com.baulsupp.okurl.services.coinbin.CoinBinAuthInterceptor(),
      com.baulsupp.okurl.services.coingecko.CoinGeckoAuthInterceptor(),
      com.baulsupp.okurl.services.companieshouse.CompaniesHouseAuthInterceptor(),
      com.baulsupp.okurl.services.cooee.CooeeAuthInterceptor(),
      com.baulsupp.okurl.services.cronhub.CronhubAuthInterceptor(),
      com.baulsupp.okurl.services.datasettes.DatasettesAuthInterceptor(),
      com.baulsupp.okurl.services.deezer.DeezerAuthInterceptor(),
      com.baulsupp.okurl.services.dropbox.DropboxAuthInterceptor(),
      com.baulsupp.okurl.services.facebook.FacebookAuthInterceptor(),
      com.baulsupp.okurl.services.fitbit.FitbitAuthInterceptor(),
      com.baulsupp.okurl.services.foursquare.FourSquareAuthInterceptor(),
      com.baulsupp.okurl.services.gdax.GdaxAuthInterceptor(),
      com.baulsupp.okurl.services.giphy.GiphyAuthInterceptor(),
      com.baulsupp.okurl.services.github.GithubAuthInterceptor(),
      com.baulsupp.okurl.services.google.GoogleAuthInterceptor(),
      com.baulsupp.okurl.services.hitbtc.HitBTCAuthInterceptor(),
      com.baulsupp.okurl.services.howsmyssl.HowsMySslAuthInterceptor(),
      com.baulsupp.okurl.services.httpbin.HttpBinAuthInterceptor(),
      com.baulsupp.okurl.services.imgur.ImgurAuthInterceptor(),
      com.baulsupp.okurl.services.instagram.InstagramAuthInterceptor(),
      com.baulsupp.okurl.services.life360.Life360AuthInterceptor(),
      com.baulsupp.okurl.services.linkedin.LinkedinAuthInterceptor(),
      com.baulsupp.okurl.services.lyft.LyftAuthInterceptor(),
      com.baulsupp.okurl.services.mapbox.MapboxAuthInterceptor(),
      com.baulsupp.okurl.services.microsoft.MicrosoftAuthInterceptor(),
      com.baulsupp.okurl.services.monzo.MonzoAuthInterceptor(),
      com.baulsupp.okurl.services.opsgenie.OpsGenieAuthInterceptor(),
      com.baulsupp.okurl.services.oxforddictionaries.OxfordDictionariesInterceptor(),
      com.baulsupp.okurl.services.paypal.PaypalAuthInterceptor(),
      com.baulsupp.okurl.services.paypal.PaypalSandboxAuthInterceptor(),
      com.baulsupp.okurl.services.postman.PostmanAuthInterceptor(),
      com.baulsupp.okurl.services.quip.QuipAuthInterceptor(),
      com.baulsupp.okurl.services.soundcloud.SoundcloudAuthInterceptor(),
      com.baulsupp.okurl.services.sheetsu.SheetsuAuthInterceptor(),
      com.baulsupp.okurl.services.slack.SlackAuthInterceptor(),
      com.baulsupp.okurl.services.smartystreets.SmartyStreetsAuthInterceptor(),
      com.baulsupp.okurl.services.spotify.SpotifyAuthInterceptor(),
      com.baulsupp.okurl.services.squareup.SquareUpAuthInterceptor(),
      com.baulsupp.okurl.services.stackexchange.StackExchangeAuthInterceptor(),
      com.baulsupp.okurl.services.strava.StravaAuthInterceptor(),
      com.baulsupp.okurl.services.stripe.StripeAuthInterceptor(),
      com.baulsupp.okurl.services.streamdata.StreamdataAuthInterceptor(),
      com.baulsupp.okurl.services.surveymonkey.SurveyMonkeyAuthInterceptor(),
      com.baulsupp.okurl.services.tfl.TflAuthInterceptor(),
      com.baulsupp.okurl.services.transferwise.TransferwiseAuthInterceptor(),
      com.baulsupp.okurl.services.transferwise.TransferwiseTestAuthInterceptor(),
      com.baulsupp.okurl.services.travisci.TravisCIAuthInterceptor(),
      com.baulsupp.okurl.services.trello.TrelloAuthInterceptor(),
      com.baulsupp.okurl.services.twilio.TwilioAuthInterceptor(),
      com.baulsupp.okurl.services.twitter.TwitterAuthInterceptor(),
      com.baulsupp.okurl.services.uber.UberAuthInterceptor(),
      com.baulsupp.okurl.services.weekdone.WeekdoneAuthInterceptor()
    )
}