# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

COPY framework/pom.xml framework/

COPY outbox-infrastructure/pom.xml outbox-infrastructure/

COPY catalog-domain/pom.xml catalog-domain/
COPY catalog-application/pom.xml catalog-application/
COPY catalog-adapter-persistence/pom.xml catalog-adapter-persistence/

COPY inventory-domain/pom.xml inventory-domain/
COPY inventory-application/pom.xml inventory-application/
COPY inventory-adapter-persistence/pom.xml inventory-adapter-persistence/

COPY identity-domain/pom.xml identity-domain/
COPY identity-application/pom.xml identity-application/
COPY identity-adapter-persistence/pom.xml identity-adapter-persistence/

COPY merchant-domain/pom.xml merchant-domain/
COPY merchant-application/pom.xml merchant-application/
COPY merchant-adapter-persistence/pom.xml merchant-adapter-persistence/

COPY pricing-domain/pom.xml pricing-domain/
COPY pricing-application/pom.xml pricing-application/
COPY pricing-adapter-persistence/pom.xml pricing-adapter-persistence/

COPY sales-channel-domain/pom.xml sales-channel-domain/
COPY sales-channel-application/pom.xml sales-channel-application/
COPY sales-channel-adapter-persistence/pom.xml sales-channel-adapter-persistence/

COPY customer-domain/pom.xml customer-domain/
COPY customer-application/pom.xml customer-application/
COPY customer-adapter-persistence/pom.xml customer-adapter-persistence/

COPY storefront-query-infrastructure/pom.xml storefront-query-infrastructure/

COPY cart-domain/pom.xml cart-domain/
COPY cart-application/pom.xml cart-application/
COPY cart-adapter-persistence/pom.xml cart-adapter-persistence/

COPY region-domain/pom.xml region-domain/
COPY region-application/pom.xml region-application/
COPY region-adapter-persistence/pom.xml region-adapter-persistence/

COPY workflow-infrastructure/pom.xml workflow-infrastructure/

COPY storage-adapter-s3/pom.xml storage-adapter-s3/

COPY logger-slf4j/pom.xml logger-slf4j/
COPY store/pom.xml store/

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl store -am dependency:go-offline -B -ntp

COPY framework/src framework/src
COPY outbox-infrastructure/src outbox-infrastructure/src

COPY catalog-domain/src catalog-domain/src
COPY catalog-application/src catalog-application/src
COPY catalog-adapter-persistence/src catalog-adapter-persistence/src

COPY inventory-domain/src inventory-domain/src
COPY inventory-application/src inventory-application/src
COPY inventory-adapter-persistence/src inventory-adapter-persistence/src

COPY identity-domain/src identity-domain/src
COPY identity-application/src identity-application/src
COPY identity-adapter-persistence/src identity-adapter-persistence/src

COPY merchant-domain/src merchant-domain/src
COPY merchant-application/src merchant-application/src
COPY merchant-adapter-persistence/src merchant-adapter-persistence/src

COPY pricing-domain/src pricing-domain/src
COPY pricing-application/src pricing-application/src
COPY pricing-adapter-persistence/src pricing-adapter-persistence/src

COPY sales-channel-domain/src sales-channel-domain/src
COPY sales-channel-application/src sales-channel-application/src
COPY sales-channel-adapter-persistence/src sales-channel-adapter-persistence/src

COPY customer-domain/src customer-domain/src
COPY customer-application/src customer-application/src
COPY customer-adapter-persistence/src customer-adapter-persistence/src

COPY storefront-query-infrastructure/src storefront-query-infrastructure/src

COPY cart-domain/src cart-domain/src
COPY cart-application/src cart-application/src
COPY cart-adapter-persistence/src cart-adapter-persistence/src

COPY region-domain/src region-domain/src
COPY region-application/src region-application/src
COPY region-adapter-persistence/src region-adapter-persistence/src

COPY workflow-infrastructure/src workflow-infrastructure/src

COPY storage-adapter-s3/src storage-adapter-s3/src

COPY logger-slf4j/src logger-slf4j/src
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
