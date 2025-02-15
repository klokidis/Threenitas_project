package com.example.threenitas_project.data

import com.example.threenitas_project.network.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val booksRepository: BooksRepository
}

class DefaultAppContainer : AppContainer {

    private val BASE_URL = "https://3nt-demo-backend.azurewebsites.net/Access/"

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Retrofit builder to build a retrofit object using a kotlinx.serialization converter
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    /**
     * Retrofit service object for creating api calls
     */
    private val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * DI implementation for repository
     */
    override val booksRepository: BooksRepository by lazy {
        DefaultBooksRepository(retrofitService)
    }
}
