package com.example.androidgraduationproject.model

import androidx.core.app.RemoteInput


data class Article(
    val author: String? = "",
    val content: String? = "",
    val description: String? = "",
    val publishedAt: String? = "",
    val source: RemoteInput.Source? = RemoteInput.Source(),
    val title: String? = "",
    val url: String? = "",
    val urlToImage: String? = ""
)
