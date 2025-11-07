package com.medipharm.backend.entities

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: UserDto
)