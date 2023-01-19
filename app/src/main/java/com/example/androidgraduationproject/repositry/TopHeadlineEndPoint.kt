package com.example.androidgraduationproject.repositry

import com.example.androidgraduationproject.model.TopHeadlines
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface TopHeadlineEndpoint {

    @GET ("top-headlines")
    fun getTopHeadlines (
        @Query("country") country : String,
        @Query("apiKey") apiKey : String
    ) : Observable<TopHeadlines>

    @GET("top-headlines")
    fun getUserSearchInput (
        @Query("apiKey") apiKey: String,
        @Query("q") q: String
    ) : Observable<TopHeadlines>

}