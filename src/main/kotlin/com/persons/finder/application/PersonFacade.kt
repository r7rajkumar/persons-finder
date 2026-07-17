package com.persons.finder.application

import com.persons.finder.data.Location
import com.persons.finder.data.Person
import com.persons.finder.domain.ai.AiBioService
import com.persons.finder.domain.services.LocationsService
import com.persons.finder.domain.services.PersonsService
import com.persons.finder.domain.util.HaversineCalculator
import com.persons.finder.infrastructure.ai.PromptSanitiser
import com.persons.finder.presentation.dto.CreatePersonRequest
import com.persons.finder.presentation.dto.NearbyPersonResponse
import com.persons.finder.presentation.dto.PersonResponse
import com.persons.finder.presentation.dto.UpdateLocationRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application-layer façade that orchestrates the three core use cases.
 *
 * This class is the only place that knows about all the moving parts:
 * - [PersonsService] — person CRUD
 * - [LocationsService] — location upsert + proximity search
 * - [AiBioService] — bio generation
 * - [PromptSanitiser] — prompt injection defence
 *
 * Controllers call the façade; the façade coordinates domain services.
 * No business logic lives here — it is pure orchestration.
 */
@Service
class PersonFacade(
    private val personsService: PersonsService,
    private val locationsService: LocationsService,
    private val aiBioService: AiBioService,
    private val promptSanitiser: PromptSanitiser
) {

    /**
     * Create a new person:
     * 1. Sanitise job title + hobbies against prompt injection.
     * 2. Generate AI bio from sanitised inputs.
     * 3. Persist the person with the bio.
     * 4. Store the initial location.
     *
     * The person save and location save are wrapped in a single transaction so
     * a location failure rolls back the person record.
     */
    @Transactional
    fun createPerson(request: CreatePersonRequest): PersonResponse {
        val cleanJobTitle = promptSanitiser.sanitiseJobTitle(request.jobTitle)
        val cleanHobbies  = promptSanitiser.sanitiseHobbies(request.hobbies)

        val bio = aiBioService.generateBio(cleanJobTitle, cleanHobbies)

        val person = Person(
            name     = request.name,
            jobTitle = cleanJobTitle,
            hobbies  = cleanHobbies,
            bio      = bio
        )
        val saved = personsService.create(person)

        locationsService.addLocation(
            Location(
                referenceId = saved.id!!,
                latitude    = request.latitude,
                longitude   = request.longitude
            )
        )

        return PersonResponse.from(saved)
    }

    /**
     * Update the location for an existing person (upsert).
     * Validates the person exists before writing the location.
     *
     * @throws com.persons.finder.domain.exception.PersonNotFoundException if [personId] is unknown.
     */
    @Transactional
    fun updateLocation(personId: Long, request: UpdateLocationRequest): PersonResponse {
        val person = personsService.getById(personId)   // throws 404 if not found
        locationsService.addLocation(
            Location(
                referenceId = personId,
                latitude    = request.latitude,
                longitude   = request.longitude
            )
        )
        return PersonResponse.from(person)
    }

    /**
     * Find all persons within [radiusKm] of the given coordinates, sorted by
     * ascending distance.
     *
     * Flow:
     * 1. [LocationsService.findAround] — bounding-box SQL pre-filter + Haversine
     *    exact filter, returns locations sorted by distance.
     * 2. Batch-fetch the corresponding persons in one query.
     * 3. Merge person data with location data and distance, preserve sort order.
     */
    fun findNearby(latitude: Double, longitude: Double, radiusKm: Double): List<NearbyPersonResponse> {
        val locations = locationsService.findAround(latitude, longitude, radiusKm)
        if (locations.isEmpty()) return emptyList()

        val personIds = locations.map { it.referenceId }
        val personsById = personsService.getByIds(personIds).associateBy { it.id }

        return locations.mapNotNull { location ->
            val person = personsById[location.referenceId] ?: return@mapNotNull null
            val distanceKm = HaversineCalculator.distanceInKm(
                latitude, longitude,
                location.latitude, location.longitude
            )
            NearbyPersonResponse(
                id          = person.id!!,
                name        = person.name,
                jobTitle    = person.jobTitle,
                hobbies     = person.hobbies,
                bio         = person.bio,
                distanceKm  = distanceKm
            )
        }
    }
}
