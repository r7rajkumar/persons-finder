package com.persons.finder.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

/**
 * Slice tests for [PersonRepository].
 *
 * Uses @DataJpaTest — loads only the JPA layer (entities, repositories)
 * with an in-memory H2 database. No web layer, no service beans.
 * Each test runs in a transaction that is rolled back after completion.
 */
@DataJpaTest
@DisplayName("PersonRepository")
class PersonRepositoryTest {

    @Autowired
    private lateinit var repository: PersonRepository

    @Autowired
    private lateinit var em: TestEntityManager

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun persist(
        name: String = "Alice",
        jobTitle: String = "Engineer",
        hobbies: List<String> = listOf("Cycling", "Cooking"),
        bio: String? = "A curious engineer."
    ): PersonEntity {
        val entity = PersonEntity(
            name = name,
            jobTitle = jobTitle,
            hobbies = hobbies.toMutableList(),
            bio = bio
        )
        return em.persistFlushFind(entity)
    }

    // -------------------------------------------------------------------------
    // Save & retrieve
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Save and retrieve")
    inner class SaveAndRetrieve {

        @Test
        @DisplayName("saved entity is assigned a generated id")
        fun `save assigns generated id`() {
            val saved = persist()
            assertNotNull(saved.id)
            assertTrue(saved.id > 0)
        }

        @Test
        @DisplayName("findById returns the correct entity")
        fun `findById returns correct entity`() {
            val saved = persist(name = "Bob", jobTitle = "Designer")
            val found = repository.findById(saved.id)
            assertTrue(found.isPresent)
            assertEquals("Bob", found.get().name)
            assertEquals("Designer", found.get().jobTitle)
        }

        @Test
        @DisplayName("hobbies are persisted and retrieved correctly")
        fun `hobbies round-trip correctly`() {
            val hobbies = listOf("Reading", "Hiking", "Gaming")
            val saved = persist(hobbies = hobbies)
            val found = repository.findById(saved.id).get()
            assertEquals(hobbies.sorted(), found.hobbies.sorted())
        }

        @Test
        @DisplayName("bio is nullable — persists as null")
        fun `bio persists as null`() {
            val saved = persist(bio = null)
            val found = repository.findById(saved.id).get()
            assertTrue(found.bio == null)
        }

        @Test
        @DisplayName("bio is persisted when provided")
        fun `bio persists when set`() {
            val saved = persist(bio = "A brilliant mind.")
            val found = repository.findById(saved.id).get()
            assertEquals("A brilliant mind.", found.bio)
        }
    }

    // -------------------------------------------------------------------------
    // findAllByIdIn
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findAllByIdIn")
    inner class FindAllByIdIn {

        @Test
        @DisplayName("returns all entities whose id is in the given list")
        fun `returns matched entities`() {
            val a = persist(name = "Alice")
            val b = persist(name = "Bob")
            val c = persist(name = "Charlie")

            val result = repository.findAllByIdIn(listOf(a.id, c.id))
            assertEquals(2, result.size)
            val names = result.map { it.name }.toSet()
            assertTrue(names.containsAll(setOf("Alice", "Charlie")))
            assertTrue("Bob" !in names)
        }

        @Test
        @DisplayName("returns empty list when no ids match")
        fun `returns empty list for unknown ids`() {
            persist()
            val result = repository.findAllByIdIn(listOf(99999L, 88888L))
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("returns empty list for empty id collection")
        fun `returns empty list for empty input`() {
            persist()
            val result = repository.findAllByIdIn(emptyList())
            assertTrue(result.isEmpty())
        }
    }

    // -------------------------------------------------------------------------
    // Domain mapping
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Domain mapping")
    inner class DomainMapping {

        @Test
        @DisplayName("toDomain maps all fields correctly")
        fun `toDomain maps correctly`() {
            val entity = persist(
                name = "Dana",
                jobTitle = "Architect",
                hobbies = listOf("Chess", "Piano"),
                bio = "Builds dreams."
            )
            val domain = entity.toDomain()
            assertEquals(entity.id, domain.id)
            assertEquals("Dana", domain.name)
            assertEquals("Architect", domain.jobTitle)
            assertEquals(listOf("Chess", "Piano").sorted(), domain.hobbies.sorted())
            assertEquals("Builds dreams.", domain.bio)
        }

        @Test
        @DisplayName("fromDomain → save → toDomain round-trips all fields")
        fun `fromDomain round-trips through persistence`() {
            val domainPerson = com.persons.finder.data.Person(
                id = null,
                name = "Eve",
                jobTitle = "Scientist",
                hobbies = listOf("Astronomy", "Baking"),
                bio = "Studies stars by night, bakes by day."
            )
            val entity = PersonEntity.fromDomain(domainPerson)
            val saved = em.persistFlushFind(entity)
            val roundTripped = saved.toDomain()

            assertNotNull(roundTripped.id)
            assertEquals("Eve", roundTripped.name)
            assertEquals("Scientist", roundTripped.jobTitle)
            assertEquals(listOf("Astronomy", "Baking").sorted(), roundTripped.hobbies.sorted())
            assertEquals("Studies stars by night, bakes by day.", roundTripped.bio)
        }
    }
}
