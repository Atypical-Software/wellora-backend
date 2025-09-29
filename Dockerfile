FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/wellora-backend-1.0.0.jar app.jar

RUN apk add --no-cache ca-certificates
RUN addgroup -S spring && adduser -S spring -G spring && chown spring:spring app.jar
USER spring:spring

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

CMD ["java", \
     "-Dcom.mongodb.useJSSE=false", \
     "-Djava.security.useSystemProxies=true", \
     "-Djavax.net.ssl.trustStore=/opt/java/openjdk/lib/security/cacerts", \
     "-Djavax.net.ssl.trustStorePassword=changeit", \
     "-Djavax.net.ssl.keyStore=/opt/java/openjdk/lib/security/cacerts", \
     "-Djavax.net.ssl.keyStorePassword=changeit", \
     "-Djdk.tls.useExtendedMasterSecret=false", \
     "-Djdk.tls.client.protocols=TLSv1.2", \
     "-Djavax.net.ssl.trustStoreType=JKS", \
     "-Djdk.tls.namedGroups=secp256r1,secp384r1,secp521r1,sect283k1,sect283r1,sect409k1,sect409r1,sect571k1,sect571r1,secp256k1", \
     "-Djavax.net.ssl.sessionCacheSize=0", \
     "-Dhttps.protocols=TLSv1.2", \
     "-jar", "app.jar"]