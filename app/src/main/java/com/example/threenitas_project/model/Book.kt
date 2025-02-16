package com.example.threenitas_project.model

import kotlinx.serialization.Serializable

enum class DownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED
}

@Serializable
data class LoginRequest(
    val UserName: String,
    val Password: String
)

@Serializable
data class LoginResponse(
    val expires_in: Int,
    val token_type: String,
    val refresh_token: String,
    val access_token: String
)

@Serializable
data class Book(
    val id: Int,
    val title: String,
    val img_url: String,
    val date_released: String,
    val pdf_url: String,
    val isDownloaded: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
)