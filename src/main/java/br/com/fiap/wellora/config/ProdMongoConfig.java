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
            String modifiedUri = mongoUri;
            
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
            return MongoClients.create(mongoUri);
        }
    }
}