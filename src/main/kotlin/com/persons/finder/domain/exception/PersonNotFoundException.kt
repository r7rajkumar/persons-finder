package com.persons.finder.domain.exception

/**
 * Thrown when a person lookup by ID yields no result.
 * Maps to HTTP 404 in the global exception handler.
 */
class PersonNotFoundException(
    val personId: Long
) : PersonsFinderException("Person not found with id: $personId")
