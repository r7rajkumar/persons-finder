package com.persons.finder.presentation.dto

import com.persons.finder.data.Person
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Person details returned by the API")
data class PersonResponse(

    @Schema(description = "Unique person ID", example = "1")
    val id: Long,

    @Schema(description = "Full display name", example = "Alice Smith")
    val name: String,

    @Schema(description = "Professional title", example = "Software Engineer")
    val jobTitle: String,

    @Schema(description = "List of hobbies", example = "[\"Cycling\", \"Photography\"]")
    val hobbies: List<String>,

    @Schema(description = "AI-generated quirky bio", example = "By day a fearless Software Engineer…")
    val bio: String?
) {
    companion object {
        fun from(person: Person): PersonResponse = PersonResponse(
            id = requireNotNull(person.id) { "Cannot build PersonResponse from unsaved Person" },
            name = person.name,
            jobTitle = person.jobTitle,
            hobbies = person.hobbies,
            bio = person.bio
        )
    }
}
