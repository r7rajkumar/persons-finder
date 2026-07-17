package com.persons.finder.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for [PersonEntity].
 *
 * [findAllByIdIn] is used by the nearby-search flow: after the location search
 * returns a list of person IDs, this fetches all matching persons in a single query
 * rather than N individual lookups.
 */
@Repository
interface PersonRepository : JpaRepository<PersonEntity, Long> {

    /**
     * Fetch multiple persons by their IDs in a single SELECT … WHERE id IN (…) query.
     *
     * @param ids Collection of person IDs to retrieve.
     * @return List of found entities (may be shorter than [ids] if some don't exist).
     */
    fun findAllByIdIn(ids: Collection<Long>): List<PersonEntity>
}
