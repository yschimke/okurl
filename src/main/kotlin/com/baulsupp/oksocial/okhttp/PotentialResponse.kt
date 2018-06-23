package com.baulsupp.oksocial.okhttp

import okhttp3.Call
import okhttp3.Response
import java.io.IOException

sealed class PotentialResponse

data class SuccessfulResponse(val call: Call, val response: Response) : PotentialResponse()

data class FailedResponse(val call: Call, val exception: IOException) : PotentialResponse()
