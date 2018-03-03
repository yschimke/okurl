package com.baulsupp.oksocial.util

import java.io.IOException

class ClientException(val responseMessage: String, val code: Int) : IOException("$code: $responseMessage")
