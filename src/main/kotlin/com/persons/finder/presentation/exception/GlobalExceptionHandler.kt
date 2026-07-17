package com.persons.finder.presentation.exception

import com.persons.finder.domain.exception.InvalidLocationException
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.domain.exception.PersonsFinderException
import com.persons.finder.presentation.dto.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * Centralised HTTP error mapping.
 *
 * Rules:
 * - 4xx errors are logged at DEBUG (expected client mistakes).
 * - 5xx errors are logged at ERROR (unexpected server failures).
 * - Stack traces are never exposed to callers — only [ErrorResponse] JSON.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // -------------------------------------------------------------------------
    // Domain exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(PersonNotFoundException::class)
    fun handlePersonNotFound(ex: PersonNotFoundException): ResponseEntity<ErrorResponse> {
        log.debug("Person not found: id={}", ex.personId)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error  = ex.message ?: "Person not found"
            ))
    }

    @ExceptionHandler(InvalidLocationException::class)
    fun handleInvalidLocation(ex: InvalidLocationException): ResponseEntity<ErrorResponse> {
        log.debug("Invalid location: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error  = ex.message ?: "Invalid location"
            ))
    }

    /** Catch-all for any other domain exception not handled above. */
    @ExceptionHandler(PersonsFinderException::class)
    fun handlePersonsFinderException(ex: PersonsFinderException): ResponseEntity<ErrorResponse> {
        log.error("Unhandled domain exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error  = "An unexpected error occurred"
            ))
    }

    // -------------------------------------------------------------------------
    // Validation exceptions
    // -------------------------------------------------------------------------

    /** Triggered by @Valid on @RequestBody — returns per-field error list. */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map { fe ->
            "${fe.field}: ${fe.defaultMessage}"
        }
        log.debug("Request body validation failed: {}", details)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status  = HttpStatus.BAD_REQUEST.value(),
                error   = "Validation failed",
                details = details
            ))
    }

    /** Triggered by @Validated on @RequestParam — constraint violations. */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val details = ex.constraintViolations.map { cv ->
            "${cv.propertyPath}: ${cv.message}"
        }
        log.debug("Constraint violation: {}", details)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status  = HttpStatus.BAD_REQUEST.value(),
                error   = "Validation failed",
                details = details
            ))
    }

    /** Missing required query parameter. */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        log.debug("Missing request parameter: {}", ex.parameterName)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error  = "Missing required parameter: '${ex.parameterName}'"
            ))
    }

    /** Request body missing, empty, or not valid JSON for the target DTO. */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.debug("Request body missing or unreadable: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error  = "Request body is missing or not valid JSON"
            ))
    }

    /** Wrong type for a path variable or query parameter. */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        log.debug("Type mismatch for parameter '{}': {}", ex.name, ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error  = "Invalid value for parameter '${ex.name}'"
            ))
    }

    /**
     * No controller/static resource matches the requested path (e.g. GET
     * "/" or any unmapped route). Without this handler, the catch-all
     * below would incorrectly turn a routine 404 into a 500.
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        log.debug("No resource found: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error  = "Resource not found. See /swagger-ui/index.html for available endpoints."
            ))
    }

    // -------------------------------------------------------------------------
    // Fallback
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error  = "Internal server error"
            ))
    }
}