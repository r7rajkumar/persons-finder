package com.persons.finder.domain.services

import com.persons.finder.data.Person
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.infrastructure.persistence.PersonEntity
import com.persons.finder.infrastructure.persistence.PersonRepository
import org.springframework.stereotype.Service

@Service
class PersonsServiceImpl(
    private val personRepository: PersonRepository
) : PersonsService {

    override fun getById(id: Long): Person =
        personRepository.findById(id)
            .map { it.toDomain() }
            .orElseThrow { PersonNotFoundException(id) }

    override fun getByIds(ids: Collection<Long>): List<Person> {
        if (ids.isEmpty()) return emptyList()
        return personRepository.findAllByIdIn(ids).map { it.toDomain() }
    }

    override fun create(person: Person): Person {
        val entity = PersonEntity.fromDomain(person)
        return personRepository.save(entity).toDomain()
    }
}
