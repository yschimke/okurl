package com.baulsupp.oksocial.services.giphy.model

data class Pagination(val total_count: Int, val count: Int, val offset: Int)

data class Meta(val status: Int, val msg: String, val response_id: String)

data class Image(val url: String?, val width: String?, val height: String?, val size: String?, val mp4: String?, val mp4_size: String?, val webp: String?, val webp_size: String?)

data class ImageResult(val type: String, val id: String, val url: String, val images: Map<String, Image>)

data class SearchResults(val data: List<ImageResult>, val pagination: Pagination, val meta: Meta)
