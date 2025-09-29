FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/wellora-backend-1.0.0.jar app.jar

# Instalar certificados e ferramentas de rede
RUN apk add --no-cache ca-certificates curl openssl
RUN update-ca-certificates

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring && chown spring:spring app.jar
USER spring:spring

EXPOSE ${PORT:-8080}
ENV SPRING_PROFILES_ACTIVE=prod

# Verificar conectividade SSL antes de iniciar
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/health || exit 1

CMD ["sh", "-c", "java \
     -Dserver.port=${PORT:-8080} \
     -Xmx512m -Xms256m \
     -Dcom.mongodb.useJSSE=true \
     -Djavax.net.ssl.trustStore=/opt/java/openjdk/lib/security/cacerts \
     -Djavax.net.ssl.trustStorePassword=changeit \
     -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 \
     -Djavax.net.ssl.trustStoreType=JKS \
     -Dhttps.protocols=TLSv1.2,TLSv1.3 \
     -jar app.jar"]