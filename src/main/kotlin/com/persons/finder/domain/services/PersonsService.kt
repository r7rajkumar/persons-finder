package com.persons.finder.domain.services

import com.persons.finder.data.Person

interface PersonsService {
    /**
     * Retrieve a person by ID.
     * @throws com.persons.finder.domain.exception.PersonNotFoundException if not found.
     */
    fun getById(id: Long): Person

    /**
     * Retrieve multiple persons by their IDs in a single query.
     * Missing IDs are silently skipped.
     */
    fun getByIds(ids: Collection<Long>): List<Person>

    /**
     * Persist a new person and return the saved instance with its assigned ID.
     */
    fun create(person: Person): Person

    /**
     * Legacy alias kept so existing callers (skeleton) still compile.
     * Delegates to [create].
     */
    fun save(person: Person) { create(person) }
}
