# Stage 0: build with maven (uses a full JDK image with maven installed)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# copy only what we need to download dependencies first (speeds cache)
COPY pom.xml .
# if you have .mvn wrapper, copy it too; otherwise build will use maven image
# COPY .mvn .mvn

# download dependencies (will be cached between builds)
RUN mvn -B dependency:go-offline

# copy source and build
COPY src ./src
RUN mvn -B package -DskipTests

# Stage 1: runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
# copy jar built in previous stage
COPY --from=build /workspace/target/*.jar app.jar

# optional: create a non-root user (recommended)
RUN addgroup --system appgroup && adduser --system appuser && chown appuser:appgroup /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
