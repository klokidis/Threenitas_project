package com.example.threenitas_project.data

import com.example.threenitas_project.model.Book
import com.example.threenitas_project.model.LoginRequest
import com.example.threenitas_project.model.LoginResponse
import com.example.threenitas_project.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BooksRepository {
    suspend fun login(username: String, password: String): LoginResponse
    suspend fun getBooks(token: String): List<Book>
}

/**
 * Network implementation of repository that retrieves data from underlying data source.
 */
class DefaultBooksRepository(
    private val apiService: ApiService
) : BooksRepository {

    // Login method to authenticate user and get the token
    override suspend fun login(username: String, password: String): LoginResponse {
        return withContext(Dispatchers.IO) {
            apiService.login(LoginRequest(username, password))
        }
    }

    // Fetch books using the Bearer token after login
    override suspend fun getBooks(token: String): List<Book> {
        return withContext(Dispatchers.IO) {
            apiService.getBooks("Bearer $token")  // Pass Bearer token for authorization
        }
    }
}
