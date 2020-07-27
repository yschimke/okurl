package com.baulsupp.okurl.services.datasettes.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatasetteIndex2(
  val file: String,
  val hash: String,
  val tables: Map<String, DatasetteTable2>,
  val views: List<String>
)

@JsonClass(generateAdapter = true)
data class DatasetteTable2(
  val name: String,
  val columns: List<String>,
  val count: Int,
  val foreign_keys: DatasetteForeignKeys,
  val fts_table: Any?,
  val hidden: Boolean,
  val label_column: String?,
  val primary_keys: List<String>
)

@JsonClass(generateAdapter = true)
data class DatasetteForeignKeys(val incoming: List<Any>, val outgoing: List<Any>)

@JsonClass(generateAdapter = true)
data class DatasetteResultSet(
  val database: String,
  val rows: List<List<Any>>,
  val truncated: Boolean,
  val columns: List<String>
)
