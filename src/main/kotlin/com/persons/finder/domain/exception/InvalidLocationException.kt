package com.persons.finder.domain.exception

/**
 * Thrown when a coordinate value is outside its valid range:
 *   latitude  ∈ [-90,  90]
 *   longitude ∈ [-180, 180]
 *
 * Maps to HTTP 400 in the global exception handler.
 */
class InvalidLocationException(
    message: String
) : PersonsFinderException(message) {

    companion object {
        fun forLatitude(value: Double): InvalidLocationException =
            InvalidLocationException(
                "Latitude $value is out of range. Must be between -90.0 and 90.0."
            )

        fun forLongitude(value: Double): InvalidLocationException =
            InvalidLocationException(
                "Longitude $value is out of range. Must be between -180.0 and 180.0."
            )
    }
}
