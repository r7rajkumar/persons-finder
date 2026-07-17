package com.persons.finder.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import com.persons.finder.application.PersonFacade
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.presentation.dto.CreatePersonRequest
import com.persons.finder.presentation.dto.NearbyPersonResponse
import com.persons.finder.presentation.dto.PersonResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(PersonController::class)
@DisplayName("PersonController")
class PersonControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockitoBean lateinit var facade: PersonFacade

    private val validRequest = CreatePersonRequest(
        name = "Alice",
        jobTitle = "Engineer",
        hobbies = listOf("Cycling", "Photography"),
        latitude = 51.5074,
        longitude = -0.1278
    )

    private val personResponse = PersonResponse(
        id = 1L,
        name = "Alice",
        jobTitle = "Engineer",
        hobbies = listOf("Cycling", "Photography"),
        bio = "A fearless Engineer who loves Cycling."
    )

    // ------------------------------------------------------------------
    // POST /api/v1/persons
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/persons")
    inner class CreatePerson {

        @Test
        @DisplayName("returns 201 with PersonResponse on valid request")
        fun `valid request returns 201`() {
            whenever(facade.createPerson(any())).thenReturn(personResponse)

            mockMvc.post("/api/v1/persons") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(validRequest)
            }.andExpect {
                status { isCreated() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.name") { value("Alice") }
                jsonPath("$.jobTitle") { value("Engineer") }
                jsonPath("$.bio") { exists() }
            }
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        fun `blank name returns 400`() {
            val bad = validRequest.copy(name = "  ")
            mockMvc.post("/api/v1/persons") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bad)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.status") { value(400) }
                jsonPath("$.details") { isArray() }
            }
        }

        @Test
        @DisplayName("returns 400 when hobbies list is empty")
        fun `empty hobbies returns 400`() {
            val bad = validRequest.copy(hobbies = emptyList())
            mockMvc.post("/api/v1/persons") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bad)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when latitude is out of range")
        fun `invalid latitude returns 400`() {
            val bad = validRequest.copy(latitude = 91.0)
            mockMvc.post("/api/v1/persons") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bad)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when longitude is out of range")
        fun `invalid longitude returns 400`() {
            val bad = validRequest.copy(longitude = -181.0)
            mockMvc.post("/api/v1/persons") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bad)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when job title is blank")
        fun `blank job title returns 400`() {
            val bad = validRequest.copy(jobTitle = "")
            mockMvc.post("/api/v1/persons") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bad)
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }

    // ------------------------------------------------------------------
    // GET /api/v1/persons/nearby
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/persons/nearby")
    inner class FindNearby {

        @Test
        @DisplayName("returns 200 with list of nearby persons")
        fun `valid params returns 200 with results`() {
            val nearby = listOf(
                NearbyPersonResponse(
                    id = 1L, name = "Alice", jobTitle = "Engineer",
                    hobbies = listOf("Cycling"), bio = "A fearless engineer.",
                    distanceKm = 2.5
                )
            )
            whenever(facade.findNearby(any(), any(), any())).thenReturn(nearby)

            mockMvc.get("/api/v1/persons/nearby") {
                param("lat", "51.5074")
                param("lon", "-0.1278")
                param("radiusKm", "10.0")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value(1) }
                jsonPath("$[0].distanceKm") { value(2.5) }
            }
        }

        @Test
        @DisplayName("returns 200 with empty list when nobody is nearby")
        fun `empty result returns 200`() {
            whenever(facade.findNearby(any(), any(), any())).thenReturn(emptyList())

            mockMvc.get("/api/v1/persons/nearby") {
                param("lat", "0.0")
                param("lon", "0.0")
                param("radiusKm", "1.0")
            }.andExpect {
                status { isOk() }
                jsonPath("$") { isArray() }
            }
        }

        @Test
        @DisplayName("returns 400 when radiusKm is missing")
        fun `missing radiusKm returns 400`() {
            mockMvc.get("/api/v1/persons/nearby") {
                param("lat", "51.5074")
                param("lon", "-0.1278")
                // radiusKm omitted
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when lat is out of range")
        fun `invalid lat returns 400`() {
            mockMvc.get("/api/v1/persons/nearby") {
                param("lat", "91.0")
                param("lon", "0.0")
                param("radiusKm", "10.0")
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when radiusKm exceeds maximum")
        fun `radius over 500 returns 400`() {
            mockMvc.get("/api/v1/persons/nearby") {
                param("lat", "0.0")
                param("lon", "0.0")
                param("radiusKm", "501.0")
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }
}
