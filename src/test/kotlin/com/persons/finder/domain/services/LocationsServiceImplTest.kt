package com.persons.finder.domain.services

import com.persons.finder.data.Location
import com.persons.finder.domain.exception.InvalidLocationException
import com.persons.finder.infrastructure.persistence.LocationEntity
import com.persons.finder.infrastructure.persistence.LocationRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("LocationsServiceImpl")
class LocationsServiceImplTest {

    @Mock
    lateinit var locationRepository: LocationRepository

    private lateinit var service: LocationsServiceImpl

    @BeforeEach
    fun setUp() {
        service = LocationsServiceImpl(locationRepository)
    }

    @Nested
    @DisplayName("addLocation")
    inner class AddLocation {

        @Test
        @DisplayName("inserts a new location when none exists for the person")
        fun `creates new location`() {
            whenever(locationRepository.findByPersonId(1L)).thenReturn(Optional.empty())

            service.addLocation(Location(referenceId = 1L, latitude = 51.5, longitude = -0.12))

            verify(locationRepository).save(
                argThat { personId == 1L && latitude == 51.5 && longitude == -0.12 }
            )
        }

        @Test
        @DisplayName("updates the existing location (upsert) when one already exists")
        fun `updates existing location`() {
            val existing = LocationEntity(id = 10L, personId = 1L, latitude = 0.0, longitude = 0.0)
            whenever(locationRepository.findByPersonId(1L)).thenReturn(Optional.of(existing))

            service.addLocation(Location(referenceId = 1L, latitude = 48.85, longitude = 2.35))

            verify(locationRepository).save(
                argThat { id == 10L && personId == 1L && latitude == 48.85 && longitude == 2.35 }
            )
        }

        @Test
        @DisplayName("throws InvalidLocationException for an out-of-range latitude")
        fun `invalid latitude rejected`() {
            assertThatThrownBy {
                service.addLocation(Location(referenceId = 1L, latitude = 91.0, longitude = 0.0))
            }.isInstanceOf(InvalidLocationException::class.java)
        }

        @Test
        @DisplayName("throws InvalidLocationException for an out-of-range longitude")
        fun `invalid longitude rejected`() {
            assertThatThrownBy {
                service.addLocation(Location(referenceId = 1L, latitude = 0.0, longitude = 181.0))
            }.isInstanceOf(InvalidLocationException::class.java)
        }
    }

    @Nested
    @DisplayName("removeLocation")
    inner class RemoveLocation {

        @Test
        @DisplayName("delegates to the repository delete-by-person-id query")
        fun `deletes by person id`() {
            service.removeLocation(7L)

            verify(locationRepository).deleteByPersonId(7L)
        }
    }

    @Nested
    @DisplayName("findAround")
    inner class FindAround {

        @Test
        @DisplayName("queries the bounding box then filters candidates by exact Haversine distance")
        fun `nearby search filters by radius`() {
            // London (51.5074, -0.1278) as the query point.
            // Paris (~344 km away) is inside the bounding box pre-filter but must be
            // excluded by the exact Haversine filter for a 50 km radius.
            val closeBy = LocationEntity(id = 1L, personId = 1L, latitude = 51.51, longitude = -0.13) // ~0.1km
            val tooFar = LocationEntity(id = 2L, personId = 2L, latitude = 48.8566, longitude = 2.3522) // Paris

            whenever(
                locationRepository.findWithinBoundingBox(any(), any(), any(), any())
            ).thenReturn(listOf(closeBy, tooFar))

            val result = service.findAround(51.5074, -0.1278, 50.0)

            assertThat(result).hasSize(1)
            assertThat(result[0].referenceId).isEqualTo(1L)
        }

        @Test
        @DisplayName("returns results sorted by ascending distance")
        fun `sorted by distance ascending`() {
            val queryLat = 0.0
            val queryLon = 0.0
            val far = LocationEntity(id = 1L, personId = 1L, latitude = 0.5, longitude = 0.5)   // farther
            val near = LocationEntity(id = 2L, personId = 2L, latitude = 0.01, longitude = 0.01) // closer

            whenever(
                locationRepository.findWithinBoundingBox(any(), any(), any(), any())
            ).thenReturn(listOf(far, near))

            val result = service.findAround(queryLat, queryLon, 1000.0)

            assertThat(result).hasSize(2)
            assertThat(result[0].referenceId).isEqualTo(2L) // nearer one first
            assertThat(result[1].referenceId).isEqualTo(1L)
        }

        @Test
        @DisplayName("throws InvalidLocationException for an out-of-range query latitude")
        fun `invalid query latitude rejected`() {
            assertThatThrownBy {
                service.findAround(-91.0, 0.0, 10.0)
            }.isInstanceOf(InvalidLocationException::class.java)
        }
    }
}
