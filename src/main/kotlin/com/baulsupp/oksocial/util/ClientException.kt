package com.baulsupp.oksocial.util

import java.io.IOException

class ClientException(responseMessage: String, val code: Int) : IOException("" + code + ": " + responseMessage)
