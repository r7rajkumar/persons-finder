package com.persons.finder.domain.services

import com.persons.finder.data.Location
import com.persons.finder.domain.exception.InvalidLocationException
import com.persons.finder.domain.util.HaversineCalculator
import com.persons.finder.infrastructure.persistence.LocationEntity
import com.persons.finder.infrastructure.persistence.LocationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LocationsServiceImpl(
    private val locationRepository: LocationRepository
) : LocationsService {

    companion object {
        // Degrees-per-km approximation used for bounding-box expansion.
        // 1° latitude ≈ 111.195 km on a sphere of radius 6371 km.
        private const val KM_PER_DEGREE = 111.195
    }

    @Transactional
    override fun addLocation(location: Location) {
        validateCoordinates(location.latitude, location.longitude)

        // Upsert: update if a record already exists for this person, create otherwise.
        val existing = locationRepository.findByPersonId(location.referenceId)
        if (existing.isPresent) {
            // Replace the existing record entirely (id-preserving update via save)
            val updated = LocationEntity(
                id = existing.get().id,
                personId = location.referenceId,
                latitude = location.latitude,
                longitude = location.longitude
            )
            locationRepository.save(updated)
        } else {
            locationRepository.save(LocationEntity.fromDomain(location))
        }
    }

    @Transactional
    override fun removeLocation(locationReferenceId: Long) {
        locationRepository.deleteByPersonId(locationReferenceId)
    }

    /**
     * Two-pass proximity search:
     *
     * Pass 1 — SQL bounding box (cheap, uses indexes):
     *   Computes a lat/lon bounding rectangle around the query point and
     *   fetches only rows within it. This eliminates most records without
     *   any trigonometric computation.
     *
     * Pass 2 — Haversine exact filter (in-memory, small candidate set):
     *   Applies the exact great-circle distance formula to the small
     *   candidate set and filters + sorts by actual distance.
     */
    override fun findAround(latitude: Double, longitude: Double, radiusInKm: Double): List<Location> {
        validateCoordinates(latitude, longitude)

        // Bounding box: expand lat/lon by the radius in degrees (slight over-approximation).
        val deltaLat = radiusInKm / KM_PER_DEGREE
        // At higher latitudes lon degrees represent shorter distances, so expand more.
        val deltaLon = radiusInKm / (KM_PER_DEGREE * Math.cos(Math.toRadians(latitude)))
            .coerceAtLeast(0.0001) // guard against division near poles

        val candidates = locationRepository.findWithinBoundingBox(
            minLat = (latitude - deltaLat).coerceAtLeast(-90.0),
            maxLat = (latitude + deltaLat).coerceAtMost(90.0),
            minLon = (longitude - deltaLon).coerceAtLeast(-180.0),
            maxLon = (longitude + deltaLon).coerceAtMost(180.0)
        )

        // Pass 2: exact Haversine filter and sort by distance ascending
        return candidates
            .filter { entity ->
                HaversineCalculator.distanceInKm(
                    latitude, longitude,
                    entity.latitude, entity.longitude
                ) <= radiusInKm
            }
            .sortedBy { entity ->
                HaversineCalculator.distanceInKm(
                    latitude, longitude,
                    entity.latitude, entity.longitude
                )
            }
            .map { it.toDomain() }
    }

    private fun validateCoordinates(latitude: Double, longitude: Double) {
        if (latitude < -90.0 || latitude > 90.0) throw InvalidLocationException.forLatitude(latitude)
        if (longitude < -180.0 || longitude > 180.0) throw InvalidLocationException.forLongitude(longitude)
    }
}
