package com.example.threenitas_project.downloaders

import android.content.Context
import com.example.threenitas_project.model.Book
import com.example.threenitas_project.model.DownloadStatus

interface Downloader {
    fun downloadFile(url: String, title: String): Long

    fun trackDownload(
        downloadId: Long,
        context: Context,
        book: Book,
        changeDownloadingState: (book: Book, newValue: DownloadStatus) -> Unit
    )
}