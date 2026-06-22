#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

IMAGE_TAG="${1:-${KKREPO_IMAGE_TAG:-kkrepo:38090}}"
PROJECT_VERSION="$(mvn -q -N -DforceStdout help:evaluate -Dexpression=project.version)"
JAR_FILE="server/target/kkrepo-server-${PROJECT_VERSION}.jar"
START_CLASS="com.github.klboke.kkrepo.server.KkRepoApplication"

echo "[image] building Spring Boot jar..."
mvn -pl server -am -DskipTests package spring-boot:repackage

echo "[image] verifying executable jar manifest..."
manifest="$(unzip -p "$JAR_FILE" META-INF/MANIFEST.MF)"
jar_listing="$(jar tf "$JAR_FILE")"

if [[ "$manifest" != *"Main-Class: org.springframework.boot.loader.launch.JarLauncher"* ]]; then
  echo "[image] $JAR_FILE is not a Spring Boot executable jar: missing JarLauncher manifest" >&2
  exit 1
fi

if [[ "$manifest" != *"Start-Class: $START_CLASS"* ]]; then
  echo "[image] $JAR_FILE is not a Spring Boot executable jar: missing Start-Class $START_CLASS" >&2
  exit 1
fi

if [[ "$jar_listing" != BOOT-INF/* && "$jar_listing" != *$'\n'BOOT-INF/* ]]; then
  echo "[image] $JAR_FILE is not a Spring Boot executable jar: missing BOOT-INF layout" >&2
  exit 1
fi

echo "[image] building Docker image $IMAGE_TAG..."
docker build --build-arg "JAR_FILE=$JAR_FILE" -t "$IMAGE_TAG" .

echo "[image] built $IMAGE_TAG"
