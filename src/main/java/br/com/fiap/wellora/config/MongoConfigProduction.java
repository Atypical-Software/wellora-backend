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
            System.out.println("🔧 Configurando MongoDB com SSL BYPASS TOTAL para produção...");
            
            // Configurar propriedades do sistema ANTES de qualquer conexão
            System.setProperty("com.mongodb.useJSSE", "false");
            System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
            System.setProperty("javax.net.ssl.trustStore", "");
            System.setProperty("javax.net.ssl.trustStorePassword", "");
            System.setProperty("javax.net.ssl.keyStore", "");
            System.setProperty("javax.net.ssl.keyStorePassword", "");
            
            // Desabilitar completamente validação SSL
            System.setProperty("com.mongodb.ssl.sslInvalidHostNameAllowed", "true");
            System.setProperty("com.mongodb.ssl.enabled", "false");
            
            // Criar SSLContext completamente permissivo
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }}, new java.security.SecureRandom());
            
            // Definir como contexto SSL padrão
            SSLContext.setDefault(sslContext);

            // URI limpa sem parâmetros SSL
            String cleanUri = mongoUri.split("\\?")[0] + "?retryWrites=true&w=majority";
            System.out.println("🌐 URI limpa: " + cleanUri.replaceAll(":[^:]*@", ":***@"));

            // Configurar cliente MongoDB SEM SSL
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(cleanUri))
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(5)
                               .minSize(0)
                               .maxWaitTime(60, TimeUnit.SECONDS)
                               .maxConnectionIdleTime(300, TimeUnit.SECONDS);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(60, TimeUnit.SECONDS)
                               .readTimeout(60, TimeUnit.SECONDS);
                    })
                    .applyToServerSettings(builder -> {
                        builder.heartbeatFrequency(30, TimeUnit.SECONDS)
                               .minHeartbeatFrequency(10, TimeUnit.SECONDS);
                    })
                    .build();

            MongoClient client = MongoClients.create(settings);
            System.out.println("✅ MongoDB cliente configurado SEM SSL!");
            return client;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao configurar MongoDB: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: tentar com URI original
            try {
                System.out.println("🔄 Tentando fallback com URI original...");
                MongoClient fallbackClient = MongoClients.create(mongoUri);
                System.out.println("✅ Fallback funcionou!");
                return fallbackClient;
            } catch (Exception fallbackError) {
                System.err.println("❌ Fallback também falhou: " + fallbackError.getMessage());
                throw new RuntimeException("Falha total na configuração MongoDB", e);
            }
        }
    }
}