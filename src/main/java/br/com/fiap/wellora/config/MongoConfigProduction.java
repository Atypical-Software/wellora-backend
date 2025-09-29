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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Configuration
@Profile("prod")
public class MongoConfigProduction {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        try {
            System.out.println("🔧 Configurando MongoDB com driver 4.11.1 + SSL bypass para JDK 17...");
            
            // Configurar propriedades SSL do sistema
            System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
            System.setProperty("com.mongodb.ssl.enabled", "true");
            System.setProperty("com.mongodb.ssl.sslInvalidHostNameAllowed", "true");
            
            // Criar SSLContext que ignora validação de certificados
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() { 
                    return new X509Certificate[0]; 
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }}, new java.security.SecureRandom());
            
            // Configuração MongoDB com SSL customizado
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToSslSettings(builder -> {
                        builder.enabled(true)
                               .invalidHostNameAllowed(true)
                               .context(sslContext);
                    })
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(20)
                               .minSize(1)
                               .maxWaitTime(30, TimeUnit.SECONDS)
                               .maxConnectionIdleTime(180, TimeUnit.SECONDS)
                               .maxConnectionLifeTime(600, TimeUnit.SECONDS);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(30, TimeUnit.SECONDS)
                               .readTimeout(30, TimeUnit.SECONDS);
                    })
                    .applyToServerSettings(builder -> {
                        builder.heartbeatFrequency(30, TimeUnit.SECONDS)
                               .minHeartbeatFrequency(10, TimeUnit.SECONDS);
                    })
                    .build();

            MongoClient mongoClient = MongoClients.create(settings);
            
            // Teste de conexão otimizado
            try {
                MongoDatabase database = mongoClient.getDatabase("wellora");
                database.runCommand(new org.bson.Document("ping", 1));
                System.out.println("✅ MongoDB conectado com driver 4.11.1 + SSL bypass!");
            } catch (Exception e) {
                System.err.println("⚠️ Ping falhou mas cliente criado: " + e.getMessage());
            }
            
            return mongoClient;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao configurar MongoDB: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback simples
            try {
                System.out.println("🔄 Tentando fallback simples...");
                return MongoClients.create(mongoUri);
            } catch (Exception fallbackError) {
                throw new RuntimeException("Falha total na configuração MongoDB", e);
            }
        }
    }
}