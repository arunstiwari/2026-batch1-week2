# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1: split the Spring Boot fat jar into layers so that dependencies
# (which rarely change) land in different image layers than application code.
# Spring Boot 4 uses `-Djarmode=tools extract`, not the old `layertools`.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS extractor

WORKDIR /build

# Built by the CI `Package` stage before `docker build` runs.
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

# ---------------------------------------------------------------------------
# Stage 2: runtime image. Only a JRE and the extracted layers.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

# Provenance labels, populated by CI.
ARG APP_VERSION=unknown
ARG GIT_COMMIT=unknown
ARG BUILD_TIME=unknown
LABEL org.opencontainers.image.title="week2" \
      org.opencontainers.image.description="week2 Spring Boot service" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}" \
      org.opencontainers.image.created="${BUILD_TIME}"

# Pick up Alpine security patches at build time, then drop the package cache.
RUN apk upgrade --no-cache && \
    addgroup -S -g 10001 app && \
    adduser -S -u 10001 -G app -h /app app

WORKDIR /app

# Ordered least- to most-frequently changing for layer cache reuse.
COPY --from=extractor --chown=app:app /build/extracted/dependencies/ ./
COPY --from=extractor --chown=app:app /build/extracted/spring-boot-loader/ ./
COPY --from=extractor --chown=app:app /build/extracted/snapshot-dependencies/ ./
COPY --from=extractor --chown=app:app /build/extracted/application/ ./

USER 10001:10001

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport" \
    SPRING_PROFILES_ACTIVE=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
