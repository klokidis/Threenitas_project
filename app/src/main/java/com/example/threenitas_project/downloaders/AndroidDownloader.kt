package com.example.threenitas_project.downloaders

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.example.threenitas_project.model.Book
import com.example.threenitas_project.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AndroidDownloader(
    context: Context
) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String, title: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("application/pdf")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title)
            .setDescription("Downloading $title")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
        return downloadManager.enqueue(request)
    }

    override fun trackDownload(
        downloadId: Long,
        context: Context,
        book: Book,
        changeDownloadingState: (Book,DownloadStatus) -> Unit
    ) {
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