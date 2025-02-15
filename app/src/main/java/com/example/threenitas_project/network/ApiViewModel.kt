package com.example.threenitas_project.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.threenitas_project.BookApplication
import com.example.threenitas_project.data.BooksRepository
import com.example.threenitas_project.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApiState(
    val token: String = "", //token stays on a private state
)

data class ApiResults(
    val books: List<Book> = listOf(),
    val loginError: Boolean = false,
    val booksError: Boolean = false,
    val booksLoading: Boolean = false
)

class ApiViewModel(private val booksRepository: BooksRepository) : ViewModel() {

    private val _apiState = MutableStateFlow(ApiState())
    private val apiState: StateFlow<ApiState> =
        _apiState.asStateFlow() //token stays on a private state

    private val _valueState = MutableStateFlow(ApiResults())
    val valueState: StateFlow<ApiResults> = _valueState.asStateFlow()

    init {
        if (apiState.value.token != "") {
            getBooks()
        }
    }

    fun login(
        userId: String,
        password: String,
        navigateToBottomBar: () -> Unit,
        changeLoadingState: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = booksRepository.login(userId, password)
                _apiState.update { currentState ->
                    currentState.copy(
                        token = response.access_token
                    )
                }
                navigateToBottomBar()
                changeLoadingState(false)
            } catch (e: Exception) {
                println("Login error: ${e.message}")
                changeLoadingState(false)
            }
        }
    }

    fun getBooks() {
        viewModelScope.launch {
            changeBooksLoading(isLoading = true, hasError = false)
            try {
                val response = booksRepository.getBooks("Bearer ${apiState.value.token}")
                _valueState.update { currentState ->
                    currentState.copy(
                        books = response,
                        booksLoading = false
                    )
                }
                println("Books: ${valueState.value.books}")
            } catch (e: Exception) {
                println("Error fetching books: ${e.message}")
                changeBooksLoading(isLoading = false, hasError = true)
            }
        }
    }

    private fun changeBooksLoading(isLoading: Boolean, hasError: Boolean) {
        _valueState.update { currentState ->
            currentState.copy(
                booksLoading = isLoading,
                booksError = hasError
            )
        }
    }

    companion object { //custom injection
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