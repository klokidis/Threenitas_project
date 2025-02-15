package com.example.threenitas_project.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.threenitas_project.BookApplication
import com.example.threenitas_project.data.BooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApiState(
    val token: String = "",
)

class ApiViewModel(private val booksRepository: BooksRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiState())
    private val uiState: StateFlow<ApiState> = _uiState.asStateFlow()

    fun login(userId: String, password: String, navigateToBottomBar: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = booksRepository.login(userId, password)
                _uiState.update { currentState ->
                    currentState.copy(
                        token = response.access_token
                    )
                }
                navigateToBottomBar()
                println("Login successful, token: ${uiState.value.token}")
            } catch (e: Exception) {
                println("Login error: ${e.message}")
            }
        }
    }

    fun getBooks() {
        viewModelScope.launch {
            try {
                val books = booksRepository.getBooks("Bearer ${uiState.value.token}")
                println("Books: $books")
            } catch (e: Exception) {
                println("Error fetching books: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory { //allows you to specify how the ViewModel should be initialized.
                initializer {
                    val application =
                        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                                as BookApplication)
                    val booksRepository = application.container.booksRepository
                    ApiViewModel(booksRepository = booksRepository)
                }
            }
    }
}