package com.baulsupp.oksocial.services.datasettes.model

data class DatasetteIndex2(val file: String, val hash: String, val tables: Map<String, DatasetteTable2>, val views: List<String>)

data class DatasetteTable2(val name: String, val columns: List<String>, val count: Int, val foreign_keys: DatasetteForeignKeys, val fts_table: Any?, val hidden: Boolean, val label_column: String?, val primary_keys: List<String>)

data class DatasetteForeignKeys(val incoming: List<Any>, val outgoing: List<Any>)

data class DatasetteResultSet(val database: String, val rows: List<List<Any>>, val truncated: Boolean, val columns: List<String>)
