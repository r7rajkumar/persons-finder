package com.persons.finder.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "Request body for creating a new person")
data class CreatePersonRequest(

    @field:NotBlank(message = "Name must not be blank")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Full display name", example = "Alice Smith")
    val name: String,

    @field:NotBlank(message = "Job title must not be blank")
    @field:Size(max = 100, message = "Job title must not exceed 100 characters")
    @Schema(description = "Professional title used for AI bio generation", example = "Software Engineer")
    val jobTitle: String,

    @field:NotNull(message = "Hobbies must not be null")
    @field:Valid
    @field:Size(min = 1, max = 10, message = "Provide between 1 and 10 hobbies")
    @field:ValidHobbies
    @Schema(description = "List of hobbies (1–10 items)", example = "[\"Cycling\", \"Photography\"]")
    val hobbies: List<@NotBlank(message = "Each hobby must not be blank") @Size(max = 100) String>,

    @field:NotNull(message = "Latitude must not be null")
    @field:DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @field:DecimalMax(value = "90.0",  message = "Latitude must be <= 90")
    @Schema(description = "Latitude of the person's current location", example = "51.5074")
    val latitude: Double,

    @field:NotNull(message = "Longitude must not be null")
    @field:DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @field:DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
    @Schema(description = "Longitude of the person's current location", example = "-0.1278")
    val longitude: Double
)
