# Multi-stage build for optimization
FROM eclipse-temurin:17-jdk-alpine as build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable and check
RUN chmod +x ./mvnw && ls -la ./mvnw

# Download dependencies with verbose output
RUN echo "=== Downloading dependencies ===" && \
    ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Show what we have before build
RUN echo "=== Files before build ===" && \
    ls -la && \
    echo "=== POM content ===" && \
    cat pom.xml

# Build the application with verbose output
RUN echo "=== Starting Maven build ===" && \
    ./mvnw clean package -DskipTests && \
    echo "=== Build completed ===" && \
    ls -la target/

# Verify the JAR is Spring Boot executable
RUN echo "=== Verifying JAR ===" && \
    ls -la target/ && \
    if [ ! -f target/wellora-backend-1.0.0.jar ]; then \
        echo "ERROR: JAR not found!"; \
        ls -la target/; \
        exit 1; \
    fi && \
    echo "JAR found, checking if it's Spring Boot..." && \
    jar tf target/wellora-backend-1.0.0.jar | head -20 && \
    if jar tf target/wellora-backend-1.0.0.jar | grep -q "BOOT-INF"; then \
        echo "✅ Spring Boot JAR confirmed"; \
    else \
        echo "❌ Not a Spring Boot JAR!"; \
        jar tf target/wellora-backend-1.0.0.jar | grep -E "(MANIFEST|META-INF)"; \
        exit 1; \
    fi

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/wellora-backend-1.0.0.jar app.jar

# Verify JAR was copied and test it
RUN echo "=== Production stage ===" && \
    ls -la app.jar && \
    java -jar app.jar --version 2>&1 | head -5 || echo "JAR test failed but continuing..."

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring && \
    chown spring:spring app.jar

USER spring:spring

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
CMD ["java", "-jar", "app.jar"]