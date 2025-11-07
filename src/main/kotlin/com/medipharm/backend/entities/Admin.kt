package com.medipharm.backend.entities

data class ImportResult(
    val imported: Int,
    val failed: Int,
    val errors: List<String>
)
