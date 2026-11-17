FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Maven reads the full reactor before selecting seller-bff and its dependencies.
COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl :seller-bff -am package -DskipTests -Dmaven.javadoc.skip=true -B -ntp

FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL application="seller-bff"
LABEL description="Grab Seller Platform BFF"

WORKDIR /app

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

COPY --from=builder --chown=appuser:appgroup /app/service/seller-bff/target/seller-bff.jar seller-bff.jar

USER appuser

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q -O - http://127.0.0.1:${SERVER_PORT:-8080}/actuator/health/liveness >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "seller-bff.jar"]
