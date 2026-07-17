package com.persons.finder.infrastructure.persistence

import com.persons.finder.data.Location
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * JPA entity for the locations table.
 *
 * Design decisions:
 * - One row per person (personId has a UNIQUE constraint) — PUT location is an upsert.
 * - Separate indexes on latitude and longitude support the bounding-box SQL pre-filter
 *   used in [LocationRepository.findWithinBoundingBox], which cuts the Haversine
 *   candidate set before exact distance is calculated in-memory.
 * - No FK to PersonEntity — intentional. The domain treats location as an independent
 *   aggregate updated at high frequency; a hard FK would add lock contention on writes.
 */
@Entity
@Table(
    name = "locations",
    uniqueConstraints = [UniqueConstraint(name = "uq_location_person_id", columnNames = ["person_id"])],
    indexes = [
        Index(name = "idx_location_latitude",  columnList = "latitude"),
        Index(name = "idx_location_longitude", columnList = "longitude")
    ]
)
class LocationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    /** References the person this location belongs to. Not a FK — see class KDoc. */
    @Column(name = "person_id", nullable = false)
    val personId: Long = 0,

    @Column(name = "latitude", nullable = false)
    val latitude: Double = 0.0,

    @Column(name = "longitude", nullable = false)
    val longitude: Double = 0.0
) {
    /** Map JPA entity → domain model. */
    fun toDomain(): Location = Location(
        referenceId = personId,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        /** Map domain model → JPA entity. */
        fun fromDomain(location: Location): LocationEntity = LocationEntity(
            personId = location.referenceId,
            latitude = location.latitude,
            longitude = location.longitude
        )
    }
}
