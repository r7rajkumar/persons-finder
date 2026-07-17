package com.persons.finder.presentation.dto

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

/**
 * Validates each entry in a List<String> hobbies field:
 * - must not be blank
 * - must not exceed 100 characters
 *
 * A single annotation on the field is enough; no type-use / @Valid cascade needed.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidHobbiesValidator::class])
annotation class ValidHobbies(
    val message: String = "Each hobby must not be blank and must not exceed 100 characters",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out jakarta.validation.Payload>> = []
)

class ValidHobbiesValidator : ConstraintValidator<ValidHobbies, List<String>?> {
    override fun isValid(hobbies: List<String>?, context: ConstraintValidatorContext): Boolean {
        if (hobbies == null) return true // @NotNull handles the null case separately
        return hobbies.all { it.isNotBlank() && it.length <= 100 }
    }
}
