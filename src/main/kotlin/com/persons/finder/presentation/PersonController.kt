package com.persons.finder.presentation

import com.persons.finder.application.PersonFacade
import com.persons.finder.presentation.dto.CreatePersonRequest
import com.persons.finder.presentation.dto.ErrorResponse
import com.persons.finder.presentation.dto.NearbyPersonResponse
import com.persons.finder.presentation.dto.PersonResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/persons")
@Validated
@Tag(name = "Persons", description = "Create persons and search nearby")
class PersonController(
    private val facade: PersonFacade
) {

    // ------------------------------------------------------------------
    // POST /api/v1/persons
    // ------------------------------------------------------------------

    @Operation(
        summary = "Create a new person",
        description = "Creates a person with an AI-generated bio. " +
                "Job title and hobbies are sanitised for prompt injection before being sent to the AI service."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201", description = "Person created successfully",
            content = [Content(schema = Schema(implementation = PersonResponse::class))]
        ),
        ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPerson(
        @Valid @RequestBody request: CreatePersonRequest
    ): PersonResponse = facade.createPerson(request)

    // ------------------------------------------------------------------
    // GET /api/v1/persons/nearby
    // ------------------------------------------------------------------

    @Operation(
        summary = "Find persons nearby",
        description = "Returns persons within the given radius (km) of the query coordinates, " +
                "sorted by ascending distance. Uses Haversine great-circle distance."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200", description = "List of nearby persons (may be empty)",
            content = [Content(array = ArraySchema(schema = Schema(implementation = NearbyPersonResponse::class)))]
        ),
        ApiResponse(
            responseCode = "400", description = "Invalid coordinates or radius",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @GetMapping("/nearby")
    fun findNearby(
        @Parameter(description = "Query latitude [-90, 90]", example = "51.5074")
        @RequestParam
        @DecimalMin("-90.0") @DecimalMax("90.0")
        lat: Double,

        @Parameter(description = "Query longitude [-180, 180]", example = "-0.1278")
        @RequestParam
        @DecimalMin("-180.0") @DecimalMax("180.0")
        lon: Double,

        @Parameter(description = "Search radius in kilometres (0 < radius ≤ 500)", example = "10.0")
        @RequestParam
        @Positive
        @DecimalMax("500.0")
        radiusKm: Double
    ): List<NearbyPersonResponse> = facade.findNearby(lat, lon, radiusKm)
}
