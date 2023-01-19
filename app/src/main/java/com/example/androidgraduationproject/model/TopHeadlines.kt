package com.example.androidgraduationproject.model

data class TopHeadlines(
    val articles: List<Article>? = listOf(),
    val status: String? = "",
    val totalResults: Int? = 0
)