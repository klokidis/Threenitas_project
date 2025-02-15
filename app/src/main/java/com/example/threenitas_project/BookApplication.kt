package com.example.threenitas_project

import android.app.Application
import com.example.threenitas_project.data.AppContainer
import com.example.threenitas_project.data.DefaultAppContainer

class BookApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()//This container holds all the dependencies (like databases, network clients, or repositories) that the app needs.
    }
}