package com.example.threenitas_project.network

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.threenitas_project.downloaders.AndroidDownloader
import com.example.threenitas_project.BookApplication
import com.example.threenitas_project.data.BooksRepository
import com.example.threenitas_project.model.Book
import com.example.threenitas_project.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ApiState(
    val token: String = "", //token stays on a private state
)

data class ApiResults(
    val books: List<Book> = listOf(),
    val loginError: Boolean = false,
    val booksError: Boolean = false,
    val booksLoading: Boolean = false,
    val groupedBooks: Map<String, List<Book>> = emptyMap()
)

class ApiViewModel(private val booksRepository: BooksRepository) : ViewModel() {

    private val _apiState = MutableStateFlow(ApiState())
    private val apiState: StateFlow<ApiState> =
        _apiState.asStateFlow() //token stays on a private state

    private val _valueState = MutableStateFlow(ApiResults())
    val valueState: StateFlow<ApiResults> = _valueState.asStateFlow()


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
                changeLoginError(true)
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
                        groupedBooks = response
                            .groupBy {
                                it.date_released.substring(0, 7) // Extracting YYYY-MM for grouping
                            }
                            .toSortedMap(compareByDescending { it }).mapValues { (_, books) ->
                                books.map { book ->
                                    book.copy(isDownloaded = isBookDownloaded(book))
                                }
                            }, // Sort keys in descending order
                        booksLoading = false
                    )
                }
            } catch (e: Exception) {
                println("Books: ${valueState.value.books}")
                println("Using ${apiState.value.token}")
                println("Error fetching books: ${e.message}")
                changeBooksLoading(isLoading = false, hasError = true)
            }
        }
    }

    fun downloadPdf(context: Context, book: Book) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            book.title
        )

        if (file.exists()) {
            Toast.makeText(context, "File already downloaded!", Toast.LENGTH_SHORT).show()
            changeDownloadingState(book, DownloadStatus.DOWNLOADED)
            openPdf(context, book.title)
            return
        }
        changeDownloadingState(book, DownloadStatus.DOWNLOADING)

        val downloader = AndroidDownloader(context)

        downloader.downloadFile(book.pdf_url, book.title)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        monitorDownload(book)
    }


    fun openPdf(context: Context, fileName: String) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        val uri =
            FileProvider.getUriForFile(context, "com.example.threenitas_project.provider", file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
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

    fun changeLoginError(newError: Boolean) {
        _valueState.update { currentState ->
            currentState.copy(
                loginError = newError,
            )
        }
    }

    private fun changeDownloadingState(book: Book, newValue: DownloadStatus) {
        _valueState.update { currentState ->
            val updatedGroupedBooks = currentState.groupedBooks.mapValues { (_, books) ->
                books.map { currentBook ->
                    if (currentBook.id == book.id) {
                        currentBook.copy(isDownloaded = newValue)
                    } else {
                        currentBook
                    }
                }
            }

            currentState.copy(
                groupedBooks = updatedGroupedBooks
            )
        }
    }


    // Function to check if a file exists in the Downloads folder
    fun isBookDownloaded(book: Book): DownloadStatus {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, book.title + ".pdf")
        val file2 = File(downloadDir, book.title)
        return if (file.exists() || file2.exists()) {
            DownloadStatus.DOWNLOADED
        } else {
            DownloadStatus.NOT_DOWNLOADED
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

    private fun monitorDownload(book: Book) {
        changeDownloadingState(book, DownloadStatus.DOWNLOADING) // Set initial state
        CoroutineScope(Dispatchers.IO).launch {
            repeat(3) { // Repeats 3 times instead of manually tracking `repeats`
                delay(4000)
                val status = isBookDownloaded(book)
                changeDownloadingState(book, status) // Update state
                if (status == DownloadStatus.DOWNLOADED) { // Stop if downloaded
                    return@launch
                } else {
                    changeDownloadingState(book, DownloadStatus.DOWNLOADING)
                }
            }
            // If after 10 attempts the book is still not downloaded, update the status
            changeDownloadingState(book, DownloadStatus.NOT_DOWNLOADED)
        }
    }
}