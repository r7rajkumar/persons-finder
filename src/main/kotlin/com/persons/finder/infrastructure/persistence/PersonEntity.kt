package com.persons.finder.infrastructure.persistence

import com.persons.finder.data.Person
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

/**
 * JPA entity for the persons table.
 *
 * Deliberately separate from the [Person] domain model — ORM annotations
 * stay in the infrastructure layer, keeping the domain clean.
 *
 * Hobbies are stored as a child collection table (person_hobbies) rather than
 * a comma-delimited string, preserving query-ability and avoiding string parsing.
 *
 * The no-arg constructor required by Hibernate is generated automatically by
 * the kotlin("plugin.jpa") Gradle plugin.
 */
@Entity
@Table(name = "persons")
class PersonEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @Column(name = "name", nullable = false, length = 100)
    val name: String = "",

    @Column(name = "job_title", nullable = false, length = 100)
    val jobTitle: String = "",

    /**
     * Stored in a separate join table: person_hobbies(person_id, hobby).
     * EAGER fetch is acceptable here — hobbies are always needed with the person.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "person_hobbies",
        joinColumns = [JoinColumn(name = "person_id")]
    )
    @Column(name = "hobby", nullable = false, length = 100)
    val hobbies: MutableList<String> = mutableListOf(),

    /**
     * AI-generated bio. Nullable — populated after the AI service runs,
     * within the same transaction as person creation.
     */
    @Column(name = "bio", nullable = true, length = 500)
    val bio: String? = null
) {
    /** Map JPA entity → domain model. */
    fun toDomain(): Person = Person(
        id = id,
        name = name,
        jobTitle = jobTitle,
        hobbies = hobbies.toList(),
        bio = bio
    )

    companion object {
        /** Map domain model → JPA entity (id=0 signals a new record to Hibernate). */
        fun fromDomain(person: Person): PersonEntity = PersonEntity(
            id = person.id ?: 0,
            name = person.name,
            jobTitle = person.jobTitle,
            hobbies = person.hobbies.toMutableList(),
            bio = person.bio
        )
    }
}
