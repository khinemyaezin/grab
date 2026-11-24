# syntax=docker/dockerfile:1.7
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl :authorization-server -am package -DskipTests -Dmaven.javadoc.skip=true -B -ntp

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S -g 1001 grab && adduser -S -u 1001 -G grab grab
COPY --from=builder --chown=grab:grab /workspace/service/authorization-server/target/authorization-server.jar /app/server.jar
USER grab
EXPOSE 9000
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -q -O - http://127.0.0.1:${AUTHORIZATION_PORT:-9000}/actuator/health/readiness >/dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/server.jar"]
