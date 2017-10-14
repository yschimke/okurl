package com.baulsupp.oksocial.services.spotify

import okhttp3.Request
import java.util.Arrays

object SpotifyUtil {
    val SCOPES: Collection<String> = Arrays.asList("playlist-read-private",
            "playlist-read-collaborative",
            "playlist-modify-public",
            "playlist-modify-private",
            "streaming",
            "ugc-image-upload",
            "user-follow-modify",
            "user-follow-read",
            "user-library-read",
            "user-library-modify",
            "user-read-private",
            "user-read-birthdate",
            "user-read-email",
            "user-top-read")

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.spotify.com" + s).build()
    }


}
