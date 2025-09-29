package br.com.fiap.wellora.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Configuration
@Profile("prod")
public class MongoConfigProduction {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        try {
            System.out.println("🔧 Configurando MongoDB com JDK 11 - sem problemas SSL!");
            
            // JDK 11 não tem problemas com MongoDB Atlas SSL - configuração simples
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(20)
                               .minSize(2)
                               .maxWaitTime(30, TimeUnit.SECONDS)
                               .maxConnectionIdleTime(120, TimeUnit.SECONDS)
                               .maxConnectionLifeTime(600, TimeUnit.SECONDS);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(30, TimeUnit.SECONDS)
                               .readTimeout(30, TimeUnit.SECONDS);
                    })
                    .build();

            MongoClient mongoClient = MongoClients.create(settings);
            
            // Teste de conexão
            try {
                MongoDatabase database = mongoClient.getDatabase("wellora");
                database.runCommand(new org.bson.Document("ping", 1));
                System.out.println("✅ MongoDB conectado com sucesso usando JDK 11!");
            } catch (Exception e) {
                System.err.println("⚠️ Ping falhou mas cliente criado: " + e.getMessage());
            }
            
            return mongoClient;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao configurar MongoDB: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback para configuração mais simples ainda
            try {
                System.out.println("🔄 Tentando configuração básica...");
                return MongoClients.create(mongoUri);
            } catch (Exception fallbackError) {
                throw new RuntimeException("Falha total na configuração MongoDB", e);
            }
        }
    }
}