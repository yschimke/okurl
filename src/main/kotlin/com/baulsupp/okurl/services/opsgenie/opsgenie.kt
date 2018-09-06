package com.baulsupp.okurl.services.opsgenie

data class Team(val name: String, val description: String, val id: String)

data class TeamsResponse(val took: Double, val data: List<Team>, val requestId: String)


