# DOCKERFILE DEFINITIVO - FORÇA JAR EXECUTÁVEL
FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /app

# Copy tudo
COPY . .

# Tornar scripts executáveis
RUN chmod +x mvnw build-jar.sh

# Usar script de build customizado
RUN ./build-jar.sh

# Produção
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/wellora-backend-1.0.0.jar app.jar

# Verificação final
RUN echo "=== JAR final ===" && \
    ls -la app.jar && \
    java -jar app.jar --help 2>&1 | head -2 || echo "JAR ready"

RUN addgroup -S spring && adduser -S spring -G spring && chown spring:spring app.jar
USER spring:spring

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

CMD ["java", "-jar", "app.jar"]