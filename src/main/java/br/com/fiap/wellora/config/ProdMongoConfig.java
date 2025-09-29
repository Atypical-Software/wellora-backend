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
            // Para resolver o problema SSL específico do Render + MongoDB Atlas
            // Vamos modificar a URI para incluir parâmetros SSL explícitos
            String modifiedUri = mongoUri;
            
            // Se a URI não tem parâmetros SSL, adicionar
            if (!mongoUri.contains("ssl=") && !mongoUri.contains("tls=")) {
                String separator = mongoUri.contains("?") ? "&" : "?";
                modifiedUri = mongoUri + separator + 
                    "ssl=true&" +
                    "tlsAllowInvalidHostnames=true&" +
                    "tlsAllowInvalidCertificates=true&" +
                    "retryWrites=true&" +
                    "w=majority&" +
                    "connectTimeoutMS=60000&" +
                    "socketTimeoutMS=60000&" +
                    "serverSelectionTimeoutMS=60000";
            }
            
            System.out.println("MongoDB URI (sem credenciais): " + 
                modifiedUri.replaceAll("://[^@]+@", "://***:***@"));
            
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(modifiedUri))
                    .build();

            return MongoClients.create(settings);
            
        } catch (Exception e) {
            System.err.println("Erro ao configurar MongoDB: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: tentar URI original
            return MongoClients.create(mongoUri);
        }
    }
}