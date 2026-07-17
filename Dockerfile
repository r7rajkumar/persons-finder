# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1 — build the application with the Gradle wrapper.
# Using the wrapper (not a system Gradle) guarantees the exact Gradle version
# (8.13) declared in gradle/wrapper/gradle-wrapper.properties is used,
# regardless of the host machine.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

# Copy build files first so dependency resolution is cached across builds
# whenever only application source changes.
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --version

# Now copy the rest of the source and build.
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon -x test

# ---------------------------------------------------------------------------
# Stage 2 — slim runtime image, JRE only.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# curl is needed for the HEALTHCHECK below (not present in the base JRE image).
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as a non-root user.
RUN useradd --system --create-home --shell /usr/sbin/nologin appuser
USER appuser

COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

# No actuator dependency is included in this project, so the health check
# targets the Swagger UI page (always served once the app is up) instead.
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD curl -fsS http://localhost:8080/swagger-ui/index.html || exit 1

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
