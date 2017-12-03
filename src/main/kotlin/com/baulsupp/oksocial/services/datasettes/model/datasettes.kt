package com.baulsupp.oksocial.services.datasettes.model

import com.squareup.moshi.Json

data class DatasetteIndex(val name: String, val hash: String, val path: String, @Json(
        name = "tables_truncated") val tables: List<Any>)

data class DatasetteTable(val name: String, val columns: List<String>, @Json(
        name = "table_rows") val tableRows: Int)

data class DatasetteTables(val database: String, val tables: List<DatasetteTable>,
                           val views: List<String>, val source: String, @Json(
                name = "source_url") val sourceUrl: String)