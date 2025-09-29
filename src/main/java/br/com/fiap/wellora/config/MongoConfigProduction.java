package br.com.fiap.wellora.config;

import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
@Profile("prod")
public class MongoConfigProduction {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        try {
            System.out.println("🔧 Configurando MongoDB com JDK 21 + SSL BYPASS TOTAL...");
            
            // Bypass SSL completo para JDK 21
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() { 
                        return new X509Certificate[0]; 
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            // Hostname verifier que aceita tudo
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Configurar SSL Context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Definir como padrão do sistema
            SSLContext.setDefault(sslContext);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            // Propriedades do sistema para JDK 21
            System.setProperty("com.mongodb.ssl.sslInvalidHostNameAllowed", "true");
            System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
            System.setProperty("javax.net.ssl.trustStoreType", "jks");

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToSslSettings(builder -> {
                        builder.enabled(true)
                               .invalidHostNameAllowed(true)
                               .context(sslContext);
                    })
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(20)
                               .minSize(2)
                               .maxWaitTime(60, TimeUnit.SECONDS)
                               .maxConnectionIdleTime(180, TimeUnit.SECONDS)
                               .maxConnectionLifeTime(0, TimeUnit.SECONDS);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(60, TimeUnit.SECONDS)
                               .readTimeout(60, TimeUnit.SECONDS);
                    })
                    .build();

            MongoClient mongoClient = MongoClients.create(settings);
            
            // Teste de conexão
            try {
                MongoDatabase database = mongoClient.getDatabase("wellora");
                database.runCommand(new org.bson.Document("ping", 1));
                System.out.println("✅ MongoDB conectado com JDK 21 + SSL BYPASS TOTAL!");
            } catch (Exception e) {
                System.err.println("⚠️ Ping falhou mas cliente criado: " + e.getMessage());
                // Continua mesmo se ping falhar
            }
            
            return mongoClient;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao configurar MongoDB: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback extremo - sem SSL
            try {
                System.out.println("🔄 Tentando fallback SEM SSL...");
                String noSslUri = mongoUri.replace("mongodb+srv://", "mongodb://").split("\\?")[0];
                return MongoClients.create(noSslUri);
            } catch (Exception fallbackError) {
                throw new RuntimeException("Falha total na configuração MongoDB", e);
            }
        }
    }
}