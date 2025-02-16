package com.example.threenitas_project.downloaders

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DownloadCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != -1L) {
                // Get the DownloadManager system service
                val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                // Query the status of the download
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)

                cursor?.let {
                    if (it.moveToFirst()) {
                        // Get the column index for status
                        val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)

                        // Check if the column exists (index >= 0)
                        if (statusIndex >= 0) {
                            // Get the status of the download
                            val status = it.getInt(statusIndex)
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    // Handle successful download
                                    println("Download with id $id finished successfully")
                                    Toast.makeText(context, "Download finished successfully", Toast.LENGTH_SHORT).show()
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    // Handle failed download
                                    println("Download with id $id failed")
                                    Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                                }
                                DownloadManager.STATUS_PAUSED -> {
                                    // Handle paused download
                                    println("Download with id $id paused")
                                    Toast.makeText(context, "Download paused", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // Handle other cases
                                    println("Download with id $id has an unknown status")
                                }
                            }
                        } else {
                            println("Download status column not found.")
                        }
                    } else {
                        println("Download query returned no results.")
                    }
                    it.close()
                }
            }
        }
    }
}
