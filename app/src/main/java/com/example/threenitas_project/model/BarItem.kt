package com.example.threenitas_project.model

import androidx.annotation.StringRes

data class BarItem(
    @StringRes val title: Int,
    val image: Int,
    val isCenter: Boolean
)
