package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.services.google.model.Parameter

class DiscoveryParameter(private val name: String, private val parameter: Parameter) {
  fun name(): String = name

  fun type(): String? = parameter.type

  fun description(): String? = parameter.description

  fun location(): String? = parameter.location

  fun pattern(): String? = parameter.pattern
}
