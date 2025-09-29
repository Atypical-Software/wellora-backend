package br.com.fiap.wellora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@Profile("prod")
public class ProdMongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        try {
            // Configuração SSL específica para resolver problema Render + MongoDB Atlas
            String modifiedUri = mongoUri;
            
            // Adicionar parâmetros SSL mais específicos
            if (!mongoUri.contains("ssl=") && !mongoUri.contains("tls=")) {
                String separator = mongoUri.contains("?") ? "&" : "?";
                modifiedUri = mongoUri + separator + 
                    "ssl=true&" +
                    "tls=true&" +
                    "tlsVersion=1.2&" +
                    "tlsAllowInvalidHostnames=true&" +
                    "tlsAllowInvalidCertificates=true&" +
                    "tlsDisableCertificateRevocationCheck=true&" +
                    "tlsDisableOCSPEndpointCheck=true&" +
                    "retryWrites=true&" +
                    "w=majority&" +
                    "connectTimeoutMS=120000&" +
                    "socketTimeoutMS=120000&" +
                    "serverSelectionTimeoutMS=120000&" +
                    "maxPoolSize=10&" +
                    "minPoolSize=1";
            }
            
            System.out.println("=== MONGODB CONFIG ===");
            System.out.println("URI Original (mascarada): " + 
                mongoUri.replaceAll("://[^@]+@", "://***:***@"));
            System.out.println("URI Modificada (mascarada): " + 
                modifiedUri.replaceAll("://[^@]+@", "://***:***@"));
            System.out.println("=======================");
            
            // Configurações SSL específicas do Java
            System.setProperty("com.mongodb.useJSSE", "false");
            System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
            
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(modifiedUri))
                    .applyToSslSettings(builder -> {
                        builder.enabled(true)
                               .invalidHostNameAllowed(true);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(120000, java.util.concurrent.TimeUnit.MILLISECONDS)
                               .readTimeout(120000, java.util.concurrent.TimeUnit.MILLISECONDS);
                    })
                    .build();

            return MongoClients.create(settings);
            
        } catch (Exception e) {
            System.err.println("Erro ao configurar MongoDB: " + e.getMessage());
            System.err.println("Tentando configuração básica...");
            
            // Fallback: tentar URI original
            return MongoClients.create(mongoUri);
        }
    }
}