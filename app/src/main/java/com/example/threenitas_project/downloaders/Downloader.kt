package com.example.threenitas_project.downloaders

interface Downloader {
    fun downloadFile(url: String, title: String): Long
}