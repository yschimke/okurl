package com.baulsupp.okurl

object OkUrl {
  /**
   * This is a string like "4.5.0-RC1", "4.5.0", or "4.6.0-SNAPSHOT" indicating the version of
   * OkUrl in the current runtime. Use this to include the OkUrl version in custom `User-Agent`
   * headers.
   *
   * [semver]: https://semver.org
   */
  const val VERSION = "$projectVersion"
}
