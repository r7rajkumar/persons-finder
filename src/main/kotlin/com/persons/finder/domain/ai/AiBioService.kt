package com.persons.finder.domain.ai

/**
 * Domain contract for AI-powered bio generation.
 *
 * Lives in the domain layer — the application layer depends on this interface,
 * never on any concrete implementation. This means:
 *   - The mock implementation (no external calls) is the default.
 *   - A live LLM implementation can be swapped in via Spring profile without
 *     touching any business logic.
 *   - Tests can inject a deterministic stub without mocking HTTP clients.
 *
 * Inputs arriving here MUST already be sanitised by [com.persons.finder.infrastructure.ai.PromptSanitiser].
 * This interface makes no safety guarantees about raw user input.
 */
interface AiBioService {

    /**
     * Generate a short, quirky bio for a person based on their professional
     * identity and interests.
     *
     * @param jobTitle The person's job title (pre-sanitised, max 100 chars).
     * @param hobbies  The person's hobbies (pre-sanitised, each max 100 chars).
     * @return A non-blank bio string, typically 50–300 characters.
     */
    fun generateBio(jobTitle: String, hobbies: List<String>): String
}
