package com.persons.finder.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A person returned by a nearby search, enriched with their distance from the query point")
data class NearbyPersonResponse(

    @Schema(description = "Unique person ID", example = "42")
    val id: Long,

    @Schema(description = "Full display name", example = "Bob Jones")
    val name: String,

    @Schema(description = "Professional title", example = "Designer")
    val jobTitle: String,

    @Schema(description = "List of hobbies", example = "[\"Chess\", \"Cooking\"]")
    val hobbies: List<String>,

    @Schema(description = "AI-generated quirky bio")
    val bio: String?,

    @Schema(description = "Great-circle distance from the query point in kilometres", example = "3.72")
    val distanceKm: Double
)
