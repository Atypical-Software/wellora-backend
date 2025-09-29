package br.com.fiap.wellora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.lang.NonNull;

import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String connectionString;

    @Override
    @NonNull
    protected String getDatabaseName() {
        return "wellora";
    }

    @Bean
    @Override
    @NonNull
    public MongoClient mongoClient() {
        try {
            // Configurações SSL mais permissivas para resolver problemas de conexão
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .applyToSslSettings(builder -> {
                        builder.enabled(true)
                               .invalidHostNameAllowed(true);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(30000, java.util.concurrent.TimeUnit.MILLISECONDS)
                               .readTimeout(30000, java.util.concurrent.TimeUnit.MILLISECONDS);
                    })
                    .build();

            return MongoClients.create(settings);
            
        } catch (Exception e) {
            // Se falhar, tenta com configuração padrão
            System.err.println("Falha ao configurar MongoDB customizado, usando configuração padrão: " + e.getMessage());
            return MongoClients.create(connectionString);
        }
    }
}