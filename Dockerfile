FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

COPY framework/pom.xml framework/
COPY category-domain/pom.xml category-domain/
COPY category-infrastructure/pom.xml category-infrastructure/
COPY product-domain/pom.xml product-domain/
COPY product-infrastructure/pom.xml product-infrastructure/
COPY store/pom.xml store/

RUN mvn dependency:go-offline -B

COPY framework/src framework/src
COPY category-domain/src category-domain/src
COPY category-infrastructure/src category-infrastructure/src
COPY product-domain/src product-domain/src
COPY product-infrastructure/src product-infrastructure/src
COPY store/src store/src

RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true -B

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

ENV SPRING_PROFILES_ACTIVE=default

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD nc -z localhost 8080 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]