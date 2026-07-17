package com.persons.finder.presentation.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Validates the jakarta.validation constraint annotations declared on the
 * request DTOs directly (without spinning up a full Spring context), using
 * a standalone Validator — fast, isolated unit tests for Step 4.3.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DTO Bean Validation")
class DtoValidationTest {

    private lateinit var validator: Validator

    @BeforeAll
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    private fun validRequest() = CreatePersonRequest(
        name = "Alice",
        jobTitle = "Engineer",
        hobbies = listOf("Cycling", "Photography"),
        latitude = 51.5074,
        longitude = -0.1278
    )

    @Nested
    @DisplayName("CreatePersonRequest")
    inner class CreatePersonRequestValidation {

        @Test
        @DisplayName("a fully valid request produces no violations")
        fun `valid request has no violations`() {
            val violations = validator.validate(validRequest())
            assertThat(violations).isEmpty()
        }

        @Test
        @DisplayName("blank name is rejected")
        fun `blank name rejected`() {
            val violations = validator.validate(validRequest().copy(name = "   "))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "name" }
        }

        @Test
        @DisplayName("name longer than 100 characters is rejected")
        fun `name too long rejected`() {
            val violations = validator.validate(validRequest().copy(name = "a".repeat(101)))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "name" }
        }

        @Test
        @DisplayName("blank job title is rejected")
        fun `blank job title rejected`() {
            val violations = validator.validate(validRequest().copy(jobTitle = ""))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "jobTitle" }
        }

        @Test
        @DisplayName("job title longer than 100 characters is rejected")
        fun `job title too long rejected`() {
            val violations = validator.validate(validRequest().copy(jobTitle = "a".repeat(101)))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "jobTitle" }
        }

        @Test
        @DisplayName("empty hobbies list is rejected (minimum 1 item)")
        fun `empty hobbies rejected`() {
            val violations = validator.validate(validRequest().copy(hobbies = emptyList()))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "hobbies" }
        }

        @Test
        @DisplayName("more than 10 hobbies is rejected (maximum 10 items)")
        fun `too many hobbies rejected`() {
            val hobbies = (1..11).map { "hobby$it" }
            val violations = validator.validate(validRequest().copy(hobbies = hobbies))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "hobbies" }
        }

        @Test
        @DisplayName("exactly 10 hobbies is accepted")
        fun `ten hobbies accepted`() {
            val hobbies = (1..10).map { "hobby$it" }
            val violations = validator.validate(validRequest().copy(hobbies = hobbies))
            assertThat(violations).isEmpty()
        }

        @Test
        @DisplayName("a hobby longer than 100 characters is rejected")
        fun `hobby too long rejected`() {
            val violations = validator.validate(validRequest().copy(hobbies = listOf("a".repeat(101))))
            assertThat(violations).isNotEmpty()
        }

        @Test
        @DisplayName("a blank hobby entry is rejected")
        fun `blank hobby rejected`() {
            val violations = validator.validate(validRequest().copy(hobbies = listOf("Cycling", "  ")))
            assertThat(violations).isNotEmpty()
        }

        @Test
        @DisplayName("latitude below -90 is rejected")
        fun `latitude below range rejected`() {
            val violations = validator.validate(validRequest().copy(latitude = -90.1))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "latitude" }
        }

        @Test
        @DisplayName("latitude above 90 is rejected")
        fun `latitude above range rejected`() {
            val violations = validator.validate(validRequest().copy(latitude = 90.1))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "latitude" }
        }

        @Test
        @DisplayName("boundary latitude values -90 and 90 are accepted")
        fun `latitude boundary accepted`() {
            assertThat(validator.validate(validRequest().copy(latitude = -90.0))).isEmpty()
            assertThat(validator.validate(validRequest().copy(latitude = 90.0))).isEmpty()
        }

        @Test
        @DisplayName("longitude below -180 is rejected")
        fun `longitude below range rejected`() {
            val violations = validator.validate(validRequest().copy(longitude = -180.1))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "longitude" }
        }

        @Test
        @DisplayName("longitude above 180 is rejected")
        fun `longitude above range rejected`() {
            val violations = validator.validate(validRequest().copy(longitude = 180.1))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "longitude" }
        }

        @Test
        @DisplayName("boundary longitude values -180 and 180 are accepted")
        fun `longitude boundary accepted`() {
            assertThat(validator.validate(validRequest().copy(longitude = -180.0))).isEmpty()
            assertThat(validator.validate(validRequest().copy(longitude = 180.0))).isEmpty()
        }
    }

    @Nested
    @DisplayName("UpdateLocationRequest")
    inner class UpdateLocationRequestValidation {

        @Test
        @DisplayName("a valid request produces no violations")
        fun `valid request has no violations`() {
            val violations = validator.validate(UpdateLocationRequest(latitude = 48.8566, longitude = 2.3522))
            assertThat(violations).isEmpty()
        }

        @Test
        @DisplayName("latitude out of range is rejected")
        fun `invalid latitude rejected`() {
            val violations = validator.validate(UpdateLocationRequest(latitude = 91.0, longitude = 0.0))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "latitude" }
        }

        @Test
        @DisplayName("longitude out of range is rejected")
        fun `invalid longitude rejected`() {
            val violations = validator.validate(UpdateLocationRequest(latitude = 0.0, longitude = -181.0))
            assertThat(violations).anyMatch { it.propertyPath.toString() == "longitude" }
        }
    }
}
