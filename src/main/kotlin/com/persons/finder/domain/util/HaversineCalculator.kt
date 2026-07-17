package com.persons.finder.domain.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Computes the great-circle distance between two points on Earth using the Haversine formula.
 *
 * The Haversine formula is numerically well-conditioned for small distances where
 * the law of cosines formula loses precision due to floating-point cancellation.
 *
 * Reference: R. W. Sinnott, "Virtues of the Haversine", Sky and Telescope, vol. 68, no. 2, 1984.
 *
 * Assumptions:
 *  - Earth is modelled as a perfect sphere with mean radius 6,371 km.
 *  - Coordinates are in decimal degrees (WGS-84).
 *
 * This is a pure Kotlin object — no framework dependencies.
 */
object HaversineCalculator {

    /** Mean radius of the Earth in kilometres (IUGG value). */
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Returns the great-circle distance in kilometres between two geographic coordinates.
     *
     * @param lat1 Latitude of point 1 in decimal degrees [-90, 90].
     * @param lon1 Longitude of point 1 in decimal degrees [-180, 180].
     * @param lat2 Latitude of point 2 in decimal degrees [-90, 90].
     * @param lon2 Longitude of point 2 in decimal degrees [-180, 180].
     * @return Distance in kilometres, always ≥ 0.
     */
    fun distanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        // a = sin²(Δlat/2) + cos(lat1) · cos(lat2) · sin²(Δlon/2)
        val a = sin(dLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)

        // c = 2 · asin(√a)  — numerically stable variant of 2·atan2(√a, √(1−a))
        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS_KM * c
    }
}
