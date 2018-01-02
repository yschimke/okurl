package com.baulsupp.oksocial.services.twitter.joauth

/**
 * the standard implementation of the Normalizer trait. Though stateless and threadsafe,
 * this is a class rather than an object to allow easy access from Java. Scala codebases
 * should use the corresponding STANDARD_NORMALIZER object instead.
 */
object StandardNormalizer {
  fun normalize(
    scheme: String?,
    host: String?,
    port: Int,
    verb: String?,
    path: String?,
    params: List<Pair<String, String>>,
    oAuth1Params: OAuthParams.OAuth1Params
  ): String {

    // We only need the stringbuilder for the duration of this method
    val paramsBuilder = StringBuilder(512)

    // first, concatenate the params and the oAuth1Params together.
    // the parameters are already URLEncoded, so we leave them alone
    val sigParams = mutableListOf<Pair<String, String>>()
    sigParams.addAll(params)
    sigParams.addAll(oAuth1Params.toList(false))

    // TODO proper sort
    sigParams.sortBy { it.first + ":" + it.second }

    if (!sigParams.isEmpty()) {
      val head = sigParams[0]
      paramsBuilder.append(head.first).append('=').append(head.second)
      (1 until sigParams.size)
        .map { sigParams[it] }
        .forEach { paramsBuilder.append('&').append(it.first).append('=').append(it.second) }
    }

    val requestUrlBuilder = StringBuilder(512)
    requestUrlBuilder.append(scheme!!.toLowerCase())
    requestUrlBuilder.append("://")
    requestUrlBuilder.append(host!!.toLowerCase())
    if (includePortString(port, scheme)) {
      requestUrlBuilder.append(":").append(port)
    }
    requestUrlBuilder.append(path)

    val normalizedBuilder = StringBuilder(512)

    normalizedBuilder.append(verb!!.toUpperCase())
    normalizedBuilder.append('&').append(UrlCodec.encode(requestUrlBuilder.toString()))
    normalizedBuilder.append('&').append(UrlCodec.encode(paramsBuilder.toString()))

    return normalizedBuilder.toString()
  }

  /**
   * The OAuth 1.0a spec says that the port should not be included in the normalized string
   * when (1) it is port 80 and the scheme is HTTP or (2) it is port 443 and the scheme is HTTPS
   */
  private fun includePortString(port: Int, scheme: String): Boolean {
    return !(port == 80 && "HTTP".equals(scheme, ignoreCase = true) || port == 443 && "HTTPS".equals(scheme, ignoreCase = true))
  }
}