package br.com.fiap.wellora.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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
            System.out.println("🔧 Configurando MongoDB com SSL customizado para produção...");
            
            // Criar SSLContext que aceita todos os certificados (apenas para produção com MongoDB Atlas)
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }}, new java.security.SecureRandom());

            // Configurar cliente MongoDB com SSL customizado
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToSslSettings(builder -> {
                        builder.enabled(true)
                               .invalidHostNameAllowed(true)
                               .context(sslContext);
                    })
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(10)
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

            MongoClient client = MongoClients.create(settings);
            System.out.println("✅ MongoDB cliente configurado com SSL customizado!");
            return client;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao configurar MongoDB SSL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha na configuração MongoDB SSL", e);
        }
    }
}