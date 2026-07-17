package com.persons.finder.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * Spring Data JPA repository for [LocationEntity].
 *
 * The nearby-search strategy is a two-pass approach for scalability:
 *
 * Pass 1 — [findWithinBoundingBox]: a cheap SQL BETWEEN filter that uses the
 *           latitude/longitude indexes to eliminate the vast majority of rows.
 *           The bounding box is an over-approximation of the target circle.
 *
 * Pass 2 — exact Haversine distance (in [LocationsServiceImpl]): applied only
 *           to the small candidate set returned by Pass 1. This avoids running
 *           the trigonometric distance formula against millions of rows.
 */
@Repository
interface LocationRepository : JpaRepository<LocationEntity, Long> {

    /**
     * Find all locations inside a lat/lon bounding box.
     *
     * This is an intentional over-approximation — rows near the corners of the
     * box but outside the actual radius circle are included and filtered out in
     * the service layer with Haversine. The SQL BETWEEN clauses hit the indexed
     * columns and run efficiently even at millions of rows.
     *
     * @param minLat  Southern boundary of the bounding box.
     * @param maxLat  Northern boundary of the bounding box.
     * @param minLon  Western boundary of the bounding box.
     * @param maxLon  Eastern boundary of the bounding box.
     */
    @Query(
        """
        SELECT l FROM LocationEntity l
        WHERE l.latitude  BETWEEN :minLat AND :maxLat
          AND l.longitude BETWEEN :minLon AND :maxLon
        """
    )
    fun findWithinBoundingBox(
        @Param("minLat") minLat: Double,
        @Param("maxLat") maxLat: Double,
        @Param("minLon") minLon: Double,
        @Param("maxLon") maxLon: Double
    ): List<LocationEntity>

    /**
     * Retrieve a person's current location by their person ID.
     * Used by the PUT location endpoint to check if a record exists before upsert.
     */
    fun findByPersonId(personId: Long): Optional<LocationEntity>

    /**
     * Delete a person's location record by person ID.
     * Used when a person is removed from the system.
     */
    @Modifying
    @Query("DELETE FROM LocationEntity l WHERE l.personId = :personId")
    fun deleteByPersonId(@Param("personId") personId: Long)
}
