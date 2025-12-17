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

COPY libs/ libs/

ARG LIB_GROUP_ID=com.coolstuff.core
ARG LIB_LIBRARY_ARTIFACT_ID=library
ARG LIB_LIBRARY_VERSION=0.0.1
ARG LIB_APP_ARTIFACT_ID=app
ARG LIB_APP_VERSION=0.0.1

RUN mvn install:install-file -Dfile=libs/${LIB_LIBRARY_ARTIFACT_ID}-${LIB_LIBRARY_VERSION}.jar \
    -DgroupId=${LIB_GROUP_ID} -DartifactId=${LIB_LIBRARY_ARTIFACT_ID} -Dversion=${LIB_LIBRARY_VERSION} \
    -Dpackaging=jar -DgeneratePom=true -B && \
    mvn install:install-file -Dfile=libs/${LIB_APP_ARTIFACT_ID}-${LIB_APP_VERSION}.jar \
    -DgroupId=${LIB_GROUP_ID} -DartifactId=${LIB_APP_ARTIFACT_ID} -Dversion=${LIB_APP_VERSION} \
    -Dpackaging=jar -DgeneratePom=true -B

RUN mvn dependency:go-offline -B

# Copy source code for all modules
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

RUN apk add --no-cache netcat-openbsd && \
    addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

COPY --from=builder /app/store/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom"

# Default Spring profile (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=default

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD nc -z localhost 8080 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]