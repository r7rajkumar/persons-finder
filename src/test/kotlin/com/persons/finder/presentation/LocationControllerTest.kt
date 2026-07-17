package com.persons.finder.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import com.persons.finder.application.PersonFacade
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.presentation.dto.PersonResponse
import com.persons.finder.presentation.dto.UpdateLocationRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put

@WebMvcTest(LocationController::class)
@DisplayName("LocationController")
class LocationControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockitoBean lateinit var facade: PersonFacade

    private val validRequest = UpdateLocationRequest(latitude = 48.8566, longitude = 2.3522)

    private val personResponse = PersonResponse(
        id = 7L,
        name = "Bob",
        jobTitle = "Designer",
        hobbies = listOf("Chess"),
        bio = "A chess-obsessed designer."
    )

    // ------------------------------------------------------------------
    // PUT /api/v1/persons/{id}/location
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/v1/persons/{id}/location")
    inner class UpdateLocation {

        @Test
        @DisplayName("returns 200 with PersonResponse on valid update")
        fun `valid update returns 200`() {
            whenever(facade.updateLocation(eq(7L), any())).thenReturn(personResponse)

            mockMvc.put("/api/v1/persons/7/location") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(validRequest)
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(7) }
                jsonPath("$.name") { value("Bob") }
            }
        }

        @Test
        @DisplayName("returns 404 when person does not exist")
        fun `unknown person returns 404`() {
            whenever(facade.updateLocation(eq(999L), any()))
                .thenThrow(PersonNotFoundException(999L))

            mockMvc.put("/api/v1/persons/999/location") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(validRequest)
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
            }
        }

        @Test
        @DisplayName("returns 400 when latitude is out of range")
        fun `invalid latitude returns 400`() {
            val bad = UpdateLocationRequest(latitude = 91.0, longitude = 0.0)
            mockMvc.put("/api/v1/persons/1/location") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bad)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when longitude is out of range")
        fun `invalid longitude returns 400`() {
            val bad = UpdateLocationRequest(latitude = 0.0, longitude = 200.0)
            mockMvc.put("/api/v1/persons/1/location") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bad)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when path variable id is not a number")
        fun `non-numeric id returns 400`() {
            mockMvc.put("/api/v1/persons/abc/location") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(validRequest)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("returns 400 when request body is missing")
        fun `missing body returns 400`() {
            mockMvc.put("/api/v1/persons/1/location") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }
}
