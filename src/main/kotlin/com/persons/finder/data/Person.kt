package com.persons.finder.data

/**
 * Domain model representing a person in the system.
 *
 * Intentionally framework-free — no JPA, Spring, or Jackson annotations.
 * The infrastructure layer (PersonEntity) handles persistence mapping.
 *
 * @param id       Null before the entity is persisted; assigned by the database on first save.
 * @param name     Full display name.
 * @param jobTitle Professional title used for AI bio generation.
 * @param hobbies  List of hobbies used for AI bio generation.
 * @param bio      AI-generated quirky bio. Null until the AI service has run.
 */
data class Person(
    val id: Long? = null,
    val name: String,
    val jobTitle: String,
    val hobbies: List<String>,
    val bio: String? = null
)
