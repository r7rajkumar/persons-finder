package com.persons.finder.infrastructure.ai

import com.persons.finder.domain.ai.AiBioService
import org.springframework.stereotype.Service

/**
 * Mock implementation of [AiBioService].
 *
 * Uses a pool of sentence templates to produce plausible, varied bios without
 * making any external HTTP calls. This is the default (and only) implementation
 * — the architecture is designed so a live LLM implementation can be added later
 * by creating a new class annotated @Profile("ai-live") and removing @Primary here.
 *
 * ## Why templates instead of random strings?
 * Determinism is valuable for testing: given the same inputs, this implementation
 * returns the same output, so tests can make specific assertions rather than
 * just contract checks. The template selection is driven by a hash of the input
 * so it is stable across JVM restarts.
 *
 * ## Testing a "non-deterministic" AI service
 * The approach here demonstrates the recommended pattern:
 *   1. Test the *contract*: non-null, non-blank, within length bounds.
 *   2. Test *negative space*: injection payloads must not appear in output.
 *   3. For a real LLM, use WireMock to stub the HTTP response and test the
 *      parsing/error-handling code without a live API key.
 */
@Service
class MockAiBioService : AiBioService {

    companion object {
        /** Bio length bounds enforced on every generated output. */
        const val MIN_BIO_LENGTH = 20
        const val MAX_BIO_LENGTH = 300

        /**
         * Template pool. Each template uses %s placeholders:
         *   position 0 → jobTitle
         *   position 1 → first hobby (or "adventure" if hobbies is empty)
         *   position 2 → second hobby (or "exploring new ideas" if fewer than 2)
         */
        private val TEMPLATES = listOf(
            "By day a fearless %s, by night completely consumed by %s. Will talk to strangers about %s.",
            "Professional %s who secretly believes %s is an Olympic sport. Also into %s.",
            "A %s with a passion for %s and an unhealthy obsession with %s. Snacks welcomed.",
            "Equal parts %s and %s enthusiast. Currently trying to convince everyone that %s is underrated.",
            "World-class %s in the making. Spends evenings perfecting %s skills. Moonlights in %s.",
            "Self-described %s by trade, %s philosopher by choice. Has strong opinions about %s.",
            "The kind of %s who shows up early, stays late, and still finds time for %s and %s.",
            "Aspiring %s turned full-time %s devotee. Also does %s, but don't tell the boss.",
            "Once a humble %s, now a certified expert in %s. Trying to fit %s in there somewhere.",
            "If there were a PhD in %s, they'd have three. Also casually good at %s and %s.",
        )
    }

    override fun generateBio(jobTitle: String, hobbies: List<String>): String {
        val hobby1 = hobbies.getOrElse(0) { "adventure" }
        val hobby2 = hobbies.getOrElse(1) { "exploring new ideas" }

        // Use a stable hash to pick the template — same inputs always → same bio.
        val templateIndex = Math.abs((jobTitle + hobby1).hashCode()) % TEMPLATES.size
        val template = TEMPLATES[templateIndex]

        val bio = template.format(jobTitle, hobby1, hobby2)

        // Enforce bounds (defensive — templates are designed to stay within limits)
        return bio.take(MAX_BIO_LENGTH)
    }
}
