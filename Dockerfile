# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

COPY framework/pom.xml framework/
COPY outbox-infrastructure/pom.xml outbox-infrastructure/
COPY catalog-domain/pom.xml catalog-domain/
COPY catalog-infrastructure/pom.xml catalog-infrastructure/
COPY inventory-domain/pom.xml inventory-domain/
COPY inventory-infrastructure/pom.xml inventory-infrastructure/
COPY store/pom.xml store/

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl store -am dependency:go-offline -B -ntp

COPY framework/src framework/src
COPY outbox-infrastructure/src outbox-infrastructure/src
COPY catalog-domain/src catalog-domain/src
COPY catalog-infrastructure/src catalog-infrastructure/src
COPY inventory-domain/src inventory-domain/src
COPY inventory-infrastructure/src inventory-infrastructure/src
COPY store/src store/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl store -am package -DskipTests -Dmaven.javadoc.skip=true -B -ntp

FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="Khine Myae Zin <hello@khinemyaezin.com>"
LABEL application="grab-store"
LABEL description="Grab E-Commerce Store Application"

WORKDIR /app

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

COPY --from=builder /app/store/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD nc -z localhost 8080 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
