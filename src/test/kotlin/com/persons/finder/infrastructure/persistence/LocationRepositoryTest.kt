package com.persons.finder.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

/**
 * Slice tests for [LocationRepository].
 *
 * Focuses on:
 * - Basic CRUD
 * - findByPersonId (upsert support)
 * - findWithinBoundingBox (core of the nearby-search pre-filter)
 * - deleteByPersonId
 * - Domain mapping round-trip
 */
@DataJpaTest
@DisplayName("LocationRepository")
class LocationRepositoryTest {

    @Autowired
    private lateinit var repository: LocationRepository

    @Autowired
    private lateinit var em: TestEntityManager

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun persist(
        personId: Long,
        latitude: Double,
        longitude: Double
    ): LocationEntity {
        val entity = LocationEntity(
            personId = personId,
            latitude = latitude,
            longitude = longitude
        )
        return em.persistFlushFind(entity)
    }

    // -------------------------------------------------------------------------
    // Save & retrieve
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Save and retrieve")
    inner class SaveAndRetrieve {

        @Test
        @DisplayName("saved entity is assigned a generated id")
        fun `save assigns generated id`() {
            val saved = persist(personId = 1L, latitude = 51.5074, longitude = -0.1278)
            assertNotNull(saved.id)
            assertTrue(saved.id > 0)
        }

        @Test
        @DisplayName("coordinates are persisted with full precision")
        fun `coordinates round-trip with full precision`() {
            val lat = 48.858844
            val lon = 2.294351
            val saved = persist(personId = 2L, latitude = lat, longitude = lon)
            val found = repository.findById(saved.id).get()
            assertEquals(lat, found.latitude, 0.000001)
            assertEquals(lon, found.longitude, 0.000001)
        }
    }

    // -------------------------------------------------------------------------
    // findByPersonId
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findByPersonId")
    inner class FindByPersonId {

        @Test
        @DisplayName("returns the location for an existing person")
        fun `returns present optional for existing person`() {
            persist(personId = 10L, latitude = 40.7128, longitude = -74.0060)
            val result = repository.findByPersonId(10L)
            assertTrue(result.isPresent)
            assertEquals(40.7128, result.get().latitude, 0.0001)
        }

        @Test
        @DisplayName("returns empty optional for a person with no location")
        fun `returns empty optional for unknown person`() {
            val result = repository.findByPersonId(99999L)
            assertFalse(result.isPresent)
        }
    }

    // -------------------------------------------------------------------------
    // findWithinBoundingBox
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findWithinBoundingBox")
    inner class FindWithinBoundingBox {

        @Test
        @DisplayName("returns only locations within the bounding box")
        fun `returns locations within box`() {
            // London area
            persist(personId = 1L, latitude = 51.5074, longitude = -0.1278)  // inside
            persist(personId = 2L, latitude = 51.5200, longitude = -0.0900)  // inside
            // Far away — Paris
            persist(personId = 3L, latitude = 48.8566, longitude = 2.3522)   // outside

            val results = repository.findWithinBoundingBox(
                minLat = 51.0, maxLat = 52.0,
                minLon = -1.0, maxLon =  1.0
            )
            assertEquals(2, results.size)
            val personIds = results.map { it.personId }.toSet()
            assertTrue(personIds.containsAll(setOf(1L, 2L)))
            assertFalse(3L in personIds)
        }

        @Test
        @DisplayName("returns empty list when no locations are in the box")
        fun `returns empty list when box has no matches`() {
            persist(personId = 1L, latitude = 51.5074, longitude = -0.1278)

            val results = repository.findWithinBoundingBox(
                minLat = 0.0, maxLat = 1.0,
                minLon = 0.0, maxLon = 1.0
            )
            assertTrue(results.isEmpty())
        }

        @Test
        @DisplayName("includes location exactly on the bounding box boundary")
        fun `includes boundary locations`() {
            persist(personId = 1L, latitude = 10.0, longitude = 20.0)

            val results = repository.findWithinBoundingBox(
                minLat = 10.0, maxLat = 10.0,
                minLon = 20.0, maxLon = 20.0
            )
            assertEquals(1, results.size)
        }

        @Test
        @DisplayName("returns all locations when box covers the whole world")
        fun `full-world box returns all locations`() {
            persist(personId = 1L, latitude =  90.0, longitude = -180.0)
            persist(personId = 2L, latitude = -90.0, longitude =  180.0)
            persist(personId = 3L, latitude =   0.0, longitude =    0.0)

            val results = repository.findWithinBoundingBox(
                minLat = -90.0, maxLat = 90.0,
                minLon = -180.0, maxLon = 180.0
            )
            assertEquals(3, results.size)
        }
    }

    // -------------------------------------------------------------------------
    // deleteByPersonId
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteByPersonId")
    inner class DeleteByPersonId {

        @Test
        @DisplayName("removes the location for the given person")
        fun `deletes location by person id`() {
            persist(personId = 42L, latitude = 51.5074, longitude = -0.1278)

            repository.deleteByPersonId(42L)
            em.flush()

            assertFalse(repository.findByPersonId(42L).isPresent)
        }

        @Test
        @DisplayName("does not affect other persons locations")
        fun `delete does not affect other persons`() {
            persist(personId = 1L, latitude = 51.5074, longitude = -0.1278)
            persist(personId = 2L, latitude = 48.8566, longitude = 2.3522)

            repository.deleteByPersonId(1L)
            em.flush()

            assertTrue(repository.findByPersonId(2L).isPresent)
        }
    }

    // -------------------------------------------------------------------------
    // Domain mapping
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Domain mapping")
    inner class DomainMapping {

        @Test
        @DisplayName("toDomain maps all fields correctly")
        fun `toDomain maps correctly`() {
            val entity = persist(personId = 7L, latitude = -33.8688, longitude = 151.2093)
            val domain = entity.toDomain()
            assertEquals(7L, domain.referenceId)
            assertEquals(-33.8688, domain.latitude, 0.000001)
            assertEquals(151.2093, domain.longitude, 0.000001)
        }

        @Test
        @DisplayName("fromDomain → save → toDomain round-trips all fields")
        fun `fromDomain round-trips through persistence`() {
            val domainLocation = com.persons.finder.data.Location(
                referenceId = 99L,
                latitude = 35.6762,
                longitude = 139.6503
            )
            val entity = LocationEntity.fromDomain(domainLocation)
            val saved = em.persistFlushFind(entity)
            val roundTripped = saved.toDomain()

            assertEquals(99L, roundTripped.referenceId)
            assertEquals(35.6762, roundTripped.latitude, 0.000001)
            assertEquals(139.6503, roundTripped.longitude, 0.000001)
        }
    }
}
