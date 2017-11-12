package com.baulsupp.oksocial.okhttp

import okhttp3.Response
import java.io.IOException

sealed class PotentialResponse

data class SuccessfulResponse(val response: Response) : PotentialResponse()

data class FailedResponse(val exception: IOException) : PotentialResponse()
