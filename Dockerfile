# Multi-stage build for optimization
FROM openjdk:17-jdk-slim as build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with explicit repackage goal
RUN ./mvnw clean package spring-boot:repackage -DskipTests

# Verify the JAR is executable
RUN ls -la target/ && \
    if [ ! -f target/wellora-backend-1.0.0.jar ]; then echo "JAR not found!"; exit 1; fi && \
    jar tf target/wellora-backend-1.0.0.jar | grep -q "BOOT-INF" || (echo "Not a Spring Boot JAR!"; exit 1)

# Production stage
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/wellora-backend-1.0.0.jar app.jar

# Verify JAR was copied
RUN ls -la app.jar

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
CMD ["java", "-jar", "app.jar"]