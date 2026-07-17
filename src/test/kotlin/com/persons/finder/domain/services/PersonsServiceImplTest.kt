package com.persons.finder.domain.services

import com.persons.finder.data.Person
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.infrastructure.persistence.PersonEntity
import com.persons.finder.infrastructure.persistence.PersonRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("PersonsServiceImpl")
class PersonsServiceImplTest {

    @Mock
    lateinit var personRepository: PersonRepository

    private lateinit var service: PersonsServiceImpl

    @BeforeEach
    fun setUp() {
        service = PersonsServiceImpl(personRepository)
    }

    @Nested
    @DisplayName("create")
    inner class Create {

        @Test
        @DisplayName("persists the person and returns it with an assigned id")
        fun `creates person`() {
            val toCreate = Person(name = "Alice", jobTitle = "Engineer", hobbies = listOf("Cycling"))
            val savedEntity = PersonEntity(id = 1L, name = "Alice", jobTitle = "Engineer", hobbies = mutableListOf("Cycling"))
            whenever(personRepository.save(any())).thenReturn(savedEntity)

            val result = service.create(toCreate)

            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("Alice")
            verify(personRepository).save(any())
        }
    }

    @Nested
    @DisplayName("getById")
    inner class GetById {

        @Test
        @DisplayName("returns the person when found")
        fun `person found`() {
            val entity = PersonEntity(id = 5L, name = "Bob", jobTitle = "Designer", hobbies = mutableListOf("Chess"))
            whenever(personRepository.findById(5L)).thenReturn(Optional.of(entity))

            val result = service.getById(5L)

            assertThat(result.id).isEqualTo(5L)
            assertThat(result.name).isEqualTo("Bob")
        }

        @Test
        @DisplayName("throws PersonNotFoundException when the person does not exist")
        fun `person not found`() {
            whenever(personRepository.findById(99L)).thenReturn(Optional.empty())

            assertThatThrownBy { service.getById(99L) }
                .isInstanceOf(PersonNotFoundException::class.java)
                .hasMessageContaining("99")
        }
    }

    @Nested
    @DisplayName("getByIds")
    inner class GetByIds {

        @Test
        @DisplayName("returns matching persons for a batch of ids")
        fun `returns multiple persons`() {
            val entities = listOf(
                PersonEntity(id = 1L, name = "Alice", jobTitle = "Engineer", hobbies = mutableListOf("Cycling")),
                PersonEntity(id = 2L, name = "Bob", jobTitle = "Designer", hobbies = mutableListOf("Chess"))
            )
            whenever(personRepository.findAllByIdIn(listOf(1L, 2L))).thenReturn(entities)

            val result = service.getByIds(listOf(1L, 2L))

            assertThat(result).hasSize(2)
            assertThat(result.map { it.id }).containsExactly(1L, 2L)
        }

        @Test
        @DisplayName("returns an empty list without querying when ids is empty")
        fun `empty ids short-circuits`() {
            val result = service.getByIds(emptyList())

            assertThat(result).isEmpty()
            verify(personRepository, never()).findAllByIdIn(any())
        }
    }
}
