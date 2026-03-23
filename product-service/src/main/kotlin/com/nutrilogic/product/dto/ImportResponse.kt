package com.nutrilogic.product.dto

data class ImportResponse(
    val success: Boolean,
    val message: String,
    val importedCount: Int,
    val totalCount: Long,
    val deletedCount: Long = 0,
    val processingTimeMs: Long = 0,
    val error: String? = null
)