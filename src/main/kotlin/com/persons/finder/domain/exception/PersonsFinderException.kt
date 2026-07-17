package com.persons.finder.domain.exception

/**
 * Base exception for all domain-level errors in the Persons Finder application.
 *
 * Using a sealed hierarchy means the global exception handler can exhaustively
 * dispatch on subtype without a catch-all fallthrough.
 */
sealed class PersonsFinderException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
