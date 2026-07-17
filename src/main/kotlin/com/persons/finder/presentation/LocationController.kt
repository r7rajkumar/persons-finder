package com.persons.finder.presentation

import com.persons.finder.application.PersonFacade
import com.persons.finder.presentation.dto.ErrorResponse
import com.persons.finder.presentation.dto.PersonResponse
import com.persons.finder.presentation.dto.UpdateLocationRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/persons")
@Tag(name = "Locations", description = "Update person locations")
class LocationController(
    private val facade: PersonFacade
) {

    // ------------------------------------------------------------------
    // PUT /api/v1/persons/{id}/location
    // ------------------------------------------------------------------

    @Operation(
        summary = "Update a person's location",
        description = "Upserts the current lat/lon for the given person. " +
                "Creates a location record if none exists; replaces it otherwise."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200", description = "Location updated — returns the person with updated details",
            content = [Content(schema = Schema(implementation = PersonResponse::class))]
        ),
        ApiResponse(
            responseCode = "400", description = "Invalid coordinates",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404", description = "Person not found",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @PutMapping("/{id}/location")
    fun updateLocation(
        @Parameter(description = "ID of the person whose location to update", example = "1")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateLocationRequest
    ): PersonResponse = facade.updateLocation(id, request)
}
