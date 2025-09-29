package br.com.fiap.wellora.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
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
            System.out.println("🔧 Configurando MongoDB com padrão oficial MongoDB Atlas...");
            
            // Configuração baseada na documentação oficial do MongoDB Atlas
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .serverApi(serverApi)
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(20)
                               .minSize(0)
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
            
            // Teste de conexão conforme documentação oficial
            try {
                MongoDatabase database = mongoClient.getDatabase("wellora");
                // Send a ping to confirm a successful connection
                database.runCommand(new org.bson.Document("ping", 1));
                System.out.println("✅ Pinged your deployment. You successfully connected to MongoDB!");
            } catch (Exception e) {
                System.err.println("❌ Ping failed: " + e.getMessage());
                // Continua mesmo se o ping falhar - deixa o Spring Boot gerenciar
            }
            
            return mongoClient;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao configurar MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha na configuração MongoDB", e);
        }
    }
}