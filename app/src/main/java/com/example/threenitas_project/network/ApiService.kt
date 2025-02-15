package com.example.threenitas_project.network

import com.example.threenitas_project.model.Book
import com.example.threenitas_project.model.LoginRequest
import com.example.threenitas_project.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("Login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("Books")
    suspend fun getBooks(@Header("Authorization") token: String): List<Book>
}