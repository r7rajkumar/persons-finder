package com.persons.finder

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

	@Autowired
	lateinit var restTemplate: TestRestTemplate

	@LocalServerPort
	var port: Int = 0

	@Test
	fun contextLoads() {
	}

	/**
	 * Regression test: an unmapped path (e.g. the bare root "/") must
	 * return 404, not 500. This previously broke because the generic
	 * @ExceptionHandler(Exception::class) catch-all in
	 * GlobalExceptionHandler intercepted NoResourceFoundException before
	 * Spring's own default 404 handling could apply.
	 */
	@Test
	fun `unmapped root path returns 404 not 500`() {
		val response = restTemplate.getForEntity("http://localhost:$port/", String::class.java)
		assert(response.statusCode == HttpStatus.NOT_FOUND) {
			"Expected 404 for unmapped path but got ${response.statusCode}"
		}
	}
}