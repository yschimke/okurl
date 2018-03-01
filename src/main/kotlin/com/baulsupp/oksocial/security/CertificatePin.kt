package com.baulsupp.oksocial.security

import com.baulsupp.oksocial.output.util.UsageException
import okhttp3.CertificatePinner

class CertificatePin(patternAndPin: String) {
  val pattern: String
  private val pin: String?

  init {
    val parts = patternAndPin.split(":".toRegex(), 2).toTypedArray()

    pattern = parts[0]
    pin = if (parts.size == 2) parts[1] else null
  }

  fun getPin(): String {
    if (pin == null) {
      throw UsageException(
        "--certificatePin expects host:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
    }

    return pin
  }

  companion object {

    fun buildFromCommandLine(pins: List<CertificatePin>): CertificatePinner {
      val builder = CertificatePinner.Builder()

      pins.groupBy { it.pattern }.forEach { (pattern, pins) ->
        builder.add(pattern, *(pins.map { it.pin }.toTypedArray()))
      }

      return builder.build()
    }
  }
}
