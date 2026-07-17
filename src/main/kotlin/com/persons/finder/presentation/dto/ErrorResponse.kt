package com.persons.finder.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Standard error response body")
data class ErrorResponse(

    @Schema(description = "HTTP status code", example = "404")
    val status: Int,

    @Schema(description = "Short error description", example = "Person not found")
    val error: String,

    @Schema(description = "Optional list of field-level validation errors")
    val details: List<String>? = null
)
