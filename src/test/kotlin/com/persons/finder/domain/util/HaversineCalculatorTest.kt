package com.persons.finder.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [HaversineCalculator].
 *
 * No Spring context — pure JUnit 5. Each test group covers a distinct geometric scenario.
 * Tolerance of 0.5 km is used for real-world coordinate pairs to account for the
 * spherical Earth approximation (actual Earth is an oblate spheroid).
 */
@DisplayName("HaversineCalculator")
class HaversineCalculatorTest {

    companion object {
        // Accepted delta for real-world coordinate pairs (km).
        // The Haversine sphere model differs from the WGS-84 ellipsoid by < 0.3% for most pairs.
        private const val TOLERANCE_KM = 0.5
        // Tighter tolerance for trivial cases (same point, symmetry checks).
        private const val ZERO_TOLERANCE_KM = 0.0001
    }

    // -------------------------------------------------------------------------
    // Same coordinates
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Same coordinates")
    inner class SameCoordinates {

        @Test
        @DisplayName("identical points return 0 km")
        fun `identical points return zero distance`() {
            val distance = HaversineCalculator.distanceInKm(51.5074, -0.1278, 51.5074, -0.1278)
            assertEquals(0.0, distance, ZERO_TOLERANCE_KM)
        }

        @Test
        @DisplayName("origin (0, 0) to itself returns 0 km")
        fun `origin to itself returns zero distance`() {
            val distance = HaversineCalculator.distanceInKm(0.0, 0.0, 0.0, 0.0)
            assertEquals(0.0, distance, ZERO_TOLERANCE_KM)
        }

        @Test
        @DisplayName("North Pole to itself returns 0 km")
        fun `north pole to itself returns zero distance`() {
            val distance = HaversineCalculator.distanceInKm(90.0, 0.0, 90.0, 0.0)
            assertEquals(0.0, distance, ZERO_TOLERANCE_KM)
        }
    }

    // -------------------------------------------------------------------------
    // Known real-world distances
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Known real-world distances")
    inner class KnownDistances {

        @Test
        @DisplayName("London to Paris ≈ 343 km")
        fun `London to Paris`() {
            // London:  51.5074° N,  0.1278° W
            // Paris:   48.8566° N,  2.3522° E
            val distance = HaversineCalculator.distanceInKm(51.5074, -0.1278, 48.8566, 2.3522)
            assertEquals(343.0, distance, TOLERANCE_KM * 2) // ~341 km on sphere
        }

        @Test
        @DisplayName("New York to Los Angeles ≈ 3940 km")
        fun `New York to Los Angeles`() {
            // New York:    40.7128° N,  74.0060° W
            // Los Angeles: 34.0522° N, 118.2437° W
            val distance = HaversineCalculator.distanceInKm(40.7128, -74.0060, 34.0522, -118.2437)
            assertEquals(3940.0, distance, 10.0)
        }

        @Test
        @DisplayName("Sydney to Auckland ≈ 2156 km")
        fun `Sydney to Auckland`() {
            // Sydney:    -33.8688° S, 151.2093° E
            // Auckland:  -36.8485° S, 174.7633° E
            val distance = HaversineCalculator.distanceInKm(-33.8688, 151.2093, -36.8485, 174.7633)
            assertEquals(2156.0, distance, 10.0)
        }

        @Test
        @DisplayName("distance is commutative: A→B == B→A")
        fun `distance is symmetric`() {
            val ab = HaversineCalculator.distanceInKm(51.5074, -0.1278, 48.8566, 2.3522)
            val ba = HaversineCalculator.distanceInKm(48.8566, 2.3522, 51.5074, -0.1278)
            assertEquals(ab, ba, ZERO_TOLERANCE_KM)
        }
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("antipodal points ≈ half Earth circumference (~20,015 km)")
        fun `antipodal points are half circumference`() {
            // Antipodal: (0, 0) and (0, 180)
            val distance = HaversineCalculator.distanceInKm(0.0, 0.0, 0.0, 180.0)
            // Half circumference = π * R = π * 6371 ≈ 20,015 km
            assertEquals(20015.0, distance, 1.0)
        }

        @Test
        @DisplayName("North Pole to South Pole ≈ 20,015 km")
        fun `pole to pole is half circumference`() {
            val distance = HaversineCalculator.distanceInKm(90.0, 0.0, -90.0, 0.0)
            assertEquals(20015.0, distance, 1.0)
        }

        @Test
        @DisplayName("crossing the date line (±180° longitude) is handled correctly")
        fun `date line crossing`() {
            // Two points either side of the 180° meridian — should be a short distance
            // Point A: (0°,  179°E), Point B: (0°, 179°W = -179°)
            // Naively: |179 - (-179)| = 358° but true angular difference is 2°
            val distance = HaversineCalculator.distanceInKm(0.0, 179.0, 0.0, -179.0)
            // 2° of longitude at the equator ≈ 2 * 111.32 km ≈ 222.6 km
            assertEquals(222.6, distance, 1.0)
        }

        @Test
        @DisplayName("equator traversal: 1 degree of longitude ≈ 111.195 km on a sphere")
        fun `one degree longitude at equator`() {
            // On a perfect sphere: circumference = 2π * R = 2π * 6371 ≈ 40,030 km
            // 1° = 40,030 / 360 ≈ 111.195 km
            // (The commonly cited 111.32 km is the WGS-84 ellipsoid value at the equator,
            //  which differs from the sphere model used here by ~0.11%.)
            val distance = HaversineCalculator.distanceInKm(0.0, 0.0, 0.0, 1.0)
            assertEquals(111.195, distance, 0.1)
        }

        @Test
        @DisplayName("result is always non-negative")
        fun `result is always non-negative`() {
            // Reversed coordinate order should never produce negative distance
            val distance = HaversineCalculator.distanceInKm(48.8566, 2.3522, 51.5074, -0.1278)
            assertTrue(distance >= 0.0)
        }

        @Test
        @DisplayName("extreme southern hemisphere coordinates")
        fun `southern hemisphere coordinates`() {
            // Cape Town, South Africa to Buenos Aires, Argentina
            // Cape Town:      -33.9249° S, 18.4241° E
            // Buenos Aires:   -34.6037° S, 58.3816° W
            val distance = HaversineCalculator.distanceInKm(-33.9249, 18.4241, -34.6037, -58.3816)
            assertEquals(6870.0, distance, 20.0)
        }
    }
}
