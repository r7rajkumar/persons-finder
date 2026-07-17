package com.persons.finder.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

@Schema(description = "Request body for updating a person's current location")
data class UpdateLocationRequest(

    @field:NotNull(message = "Latitude must not be null")
    @field:DecimalMin(value = "-90.0",  message = "Latitude must be >= -90")
    @field:DecimalMax(value = "90.0",   message = "Latitude must be <= 90")
    @Schema(description = "New latitude", example = "48.8566")
    val latitude: Double,

    @field:NotNull(message = "Longitude must not be null")
    @field:DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @field:DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
    @Schema(description = "New longitude", example = "2.3522")
    val longitude: Double
)
