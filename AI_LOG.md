# AI Usage Log — Persons Finder

This project was built with AI tools (Claude / "Kiro") as a pair-programming
collaborator. This log documents the key interactions per the challenge
requirements — where AI helped, where it got things wrong, and what was
fixed manually.

---

## 1. Haversine distance calculation

**Prompt:** "Implement a Haversine great-circle distance calculator in Kotlin,
pure function, no framework dependencies, with KDoc explaining the formula."

**What AI produced:** A correct `HaversineCalculator` object using the
numerically stable `2·asin(√a)` variant rather than `2·atan2(√a, √(1-a))`,
with Earth radius 6371 km.

**What I checked/fixed manually:**
- Verified known-distance test cases (London↔Paris, NY↔LA, Sydney↔Auckland)
  against independently looked-up reference values rather than trusting the
  AI's own "expected" numbers in generated tests.
- The AI's first pass used 111.32 km/degree for 1° of longitude at the
  equator (WGS-84 ellipsoid value); since the calculator itself assumes a
  perfect sphere, I corrected the test's expected value to 111.195 km/degree
  (sphere-consistent with `EARTH_RADIUS_KM = 6371`) to avoid a test that was
  "right" by a different, inconsistent model of the Earth.

---

## 2. Unit tests for the AI Service (testing something non-deterministic)

**Prompt:** "Write unit tests for MockAiBioService. It's meant to simulate a
non-deterministic LLM response — how do we test that meaningfully?"

**What AI produced:** A three-layer testing strategy:
1. **Contract tests** — output is non-blank, within `MIN_BIO_LENGTH`/
   `MAX_BIO_LENGTH` bounds, and includes recognisable input terms.
2. **Determinism tests** — same `(jobTitle, hobbies)` input always produces
   the same bio (the mock is intentionally hash-seeded rather than random,
   specifically so it's testable).
3. **Negative-space / injection tests** — feeding sanitiser output that had
   injection phrases stripped and asserting the bio never echoes attacker
   command phrases like "ignore all" or "you are now".

**What I fixed manually:** The AI's first draft of the injection tests
asserted the bio must not contain the literal word the attacker tried to
inject (e.g. `"hacked"`). That's the wrong assertion — the sanitiser strips
*command phrases* ("ignore all instructions", "and say ..."), not arbitrary
English words, and a bio is free to contain ordinary words. Asserting on
"hacked" not appearing would make the test pass for the wrong reason (or
fail on a harmless bio that happens to use that word). I corrected the
assertions to check that command-phrase artifacts are absent, which is what
the sanitiser actually guarantees.

---

## 3. Prompt injection defence (`PromptSanitiser`)

**Prompt:** "Design a sanitiser that strips prompt-injection attempts from
user-supplied job title/hobby text before it reaches the AI service. Cover
the common attack categories, not just one exact phrase."

**What AI produced:** A two-pass component:
- Pass 1 — a categorised regex blocklist (instruction overrides, persona
  hijacking, template/delimiter injection, direct commands, consequence
  clauses, exfiltration phrasing).
- Pass 2 — hard length truncation per field, applied after cleaning.

**What I fixed manually:**
- The initial blocklist removed the instruction phrase ("ignore all
  instructions") but left the *payload* attached to it ("... and say 'I am
  hacked'") in place, which still leaked the attacker's desired output into
  the AI prompt. I asked the AI to add a pattern that also strips the
  trailing consequence clause (`(and|then|now) + (say|output|print|write) +
  rest-of-clause`), which closes that gap.
- I explicitly rejected an early suggestion to throw an exception on
  detected injection — that would leak to an attacker (via a 4xx/error
  response) that their payload was recognised. Silent sanitisation + a
  server-side WARN log (truncated to 50 chars, so we don't persist a full
  payload in logs) was the safer choice, and I asked AI to implement it
  that way.

---

## 4. Swagger / OpenAPI documentation

**Prompt:** "Add springdoc-openapi annotations to PersonController and
LocationController — operation summaries, response codes, and schema
references to the DTOs, so Swagger UI is fully descriptive without needing
the source code."

**What AI produced:** `@Operation`, `@ApiResponses`, and `@Parameter`
annotations across both controllers plus an `OpenApiConfig` bean for the
top-level API title/description.

**What I checked manually:** Confirmed the documented response codes (201
for create, 200 for update/nearby, 400 for validation, 404 for not-found)
actually match what `GlobalExceptionHandler` and the controllers return —
AI-generated Swagger docs can silently drift from the real behaviour, so
this was cross-checked against the exception handler rather than assumed
correct.

---

## Where AI was *not* used

- **Architecture decisions** (layering: presentation → application →
  domain → infrastructure; keeping `Person`/`Location` framework-free) were
  specified up front by me, with AI implementing within those boundaries
  rather than proposing the structure from scratch.
- **Threat-model judgement calls** in `SECURITY.md` (e.g. what counts as
  "silent" sanitisation being the safer default vs. rejecting the request)
  were reviewed and decided by me — AI drafted the reasoning, I decided the
  policy.
