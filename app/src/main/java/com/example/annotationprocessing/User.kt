package com.example.annotationprocessing

import com.example.mylibrary.AutoDto

@AutoDto
data class User(
    val id: Int,
    val email: String,
    val accountBalance: Double
)

