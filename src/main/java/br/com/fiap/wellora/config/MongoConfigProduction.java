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
            System.out.println("🔧 Configurando MongoDB com JDK 21 LTS - SSL nativo otimizado!");
            
            // JDK 21 tem suporte SSL/TLS nativo excelente - configuração clean
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(20)
                               .minSize(2)
                               .maxWaitTime(30, TimeUnit.SECONDS)
                               .maxConnectionIdleTime(120, TimeUnit.SECONDS)
                               .maxConnectionLifeTime(0, TimeUnit.SECONDS);
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
                System.out.println("✅ MongoDB conectado com JDK 21 - SSL nativo funcionando!");
            } catch (Exception e) {
                System.err.println("⚠️ Ping falhou mas cliente criado: " + e.getMessage());
            }
            
            return mongoClient;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao configurar MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha na configuração MongoDB", e);
        }
    }
}