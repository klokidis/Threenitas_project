package com.example.threenitas_project.network

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Environment
import android.util.Log
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
                            },
                        booksLoading = false
                    )
                }
            } catch (e: Exception) {
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

        val id = downloader.downloadFile(book.pdf_url, book.title)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        trackDownload(context = context, downloadId = id, book = book)
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

    private fun trackDownload(downloadId: Long, context: Context, book: Book) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val query = DownloadManager.Query().setFilterById(downloadId)

        CoroutineScope(Dispatchers.IO).launch {
            var downloading = true
            while (downloading) {
                val cursor: Cursor? = downloadManager.query(query)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val status =
                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val bytesDownloaded =
                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalBytes =
                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                        if (totalBytes > 0) {
                            val progress = (bytesDownloaded * 100L) / totalBytes
                            changeDownloadingState(book, DownloadStatus.DOWNLOADING)
                            Log.d("DownloadTracker", "Download Progress: $progress%")
                        }

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                changeDownloadingState(book, DownloadStatus.DOWNLOADED)
                                downloading = false
                            }

                            DownloadManager.STATUS_FAILED -> {
                                changeDownloadingState(book, DownloadStatus.NOT_DOWNLOADED)
                                downloading = false
                            }

                            DownloadManager.STATUS_PAUSED -> Log.d(
                                "DownloadTracker",
                                "Download Paused"
                            )

                            DownloadManager.STATUS_PENDING -> Log.d(
                                "DownloadTracker",
                                "Download Pending"
                            )

                            DownloadManager.STATUS_RUNNING -> Log.d(
                                "DownloadTracker",
                                "Download In Progress..."
                            )
                        }
                    }
                }
                cursor?.close()
                delay(1000) // Check every second
            }
        }
    }
}