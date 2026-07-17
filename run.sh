#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Persons Finder — one-command helper for macOS / Linux.
#
# Usage:
#   ./run.sh test     Run the full test suite (./gradlew test)
#   ./run.sh build     Compile and run tests (./gradlew clean build)
#   ./run.sh start     Build (skip tests) and run the app locally with the Gradle wrapper
#   ./run.sh docker     Build and run the app in Docker via docker compose
#   ./run.sh clean     Remove build output
#
# No arguments defaults to "start".
#
# Requirements for the non-docker options: JDK 21 on PATH (or JAVA_HOME set).
# The docker option only requires Docker Desktop / Docker Engine — no local
# JDK needed.
# ---------------------------------------------------------------------------
set -euo pipefail
cd "$(dirname "$0")"

CMD="${1:-start}"

check_java() {
  if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: Java was not found on PATH." >&2
    echo "Install Temurin JDK 21 (https://adoptium.net/) or run './run.sh docker' instead," >&2
    echo "which only needs Docker and builds/runs the app in a container." >&2
    exit 1
  fi
  # Best-effort version check only — never let a parsing miss abort the script.
  # (grep returning no match, combined with `set -o pipefail`, would otherwise
  # kill the whole script here even though Java is actually present and fine.)
  JAVA_VERSION="$(java -version 2>&1 | head -n1 | grep -oE '"[0-9]+' | tr -d '"' || true)"
  if [ -n "$JAVA_VERSION" ] && [ "$JAVA_VERSION" -lt 21 ] 2>/dev/null; then
    echo "WARNING: Detected Java $JAVA_VERSION. This project targets Java 21." >&2
    echo "If the build fails, install Temurin 21 (https://adoptium.net/) or use './run.sh docker'." >&2
  fi
}

case "$CMD" in
  test)
    check_java
    ./gradlew test
    ;;
  build)
    check_java
    ./gradlew clean build
    ;;
  start)
    check_java
    ./gradlew clean bootRun
    ;;
  docker)
    if ! command -v docker >/dev/null 2>&1; then
      echo "ERROR: Docker was not found on PATH. Install Docker Desktop: https://www.docker.com/products/docker-desktop/" >&2
      exit 1
    fi
    if docker compose version >/dev/null 2>&1; then
      docker compose up --build
    else
      docker-compose up --build
    fi
    ;;
  clean)
    ./gradlew clean
    ;;
  *)
    echo "Usage: ./run.sh [test|build|start|docker|clean]" >&2
    exit 1
    ;;
esac