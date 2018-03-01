package com.baulsupp.oksocial.services.spotify.model

data class Error(val status: Int, val message: String)

data class ErrorResponse(val error: Error)
