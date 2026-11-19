FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Maven reads the full reactor before selecting workflow-service and its dependencies.
COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl :workflow-service -am package -DskipTests -Dmaven.javadoc.skip=true -B -ntp

FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL application="workflow-service"
LABEL description="Grab workflow headless domain service"

WORKDIR /app

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

COPY --from=builder --chown=appuser:appgroup /app/service/workflow/workflow-service/target/workflow-service.jar workflow-service.jar

USER appuser

EXPOSE 1103

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD if [ "${GRAB_RPC_ENABLED:-false}" = "true" ]; then \
            host="${GRAB_RPC_BIND_ADDRESS:-127.0.0.1}"; \
            case "$host" in 0.0.0.0) host=127.0.0.1 ;; ::) host=::1 ;; esac; \
            nc -z "$host" "${GRAB_RPC_REGISTRY_PORT:-1103}"; \
        else \
            kill -0 1; \
        fi

ENTRYPOINT ["java", "-jar", "workflow-service.jar"]
