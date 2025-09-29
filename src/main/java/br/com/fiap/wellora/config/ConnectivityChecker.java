package br.com.fiap.wellora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Component
public class ConnectivityChecker {

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    public void checkConnectivity() {
        System.out.println("=== VERIFICAÇÃO DE CONECTIVIDADE ===");
        testDnsResolution();
        testTcpConnectivity();
        showEnvironmentInfo();
    }

    private void testDnsResolution() {
        try {
            System.out.println("Testando resolução DNS para MongoDB Atlas...");
            String[] hosts = {
                "ac-go8uywq-shard-00-00.dfxd3lp.mongodb.net",
                "ac-go8uywq-shard-00-01.dfxd3lp.mongodb.net", 
                "ac-go8uywq-shard-00-02.dfxd3lp.mongodb.net"
            };
            
            for (String host : hosts) {
                try {
                    java.net.InetAddress addr = java.net.InetAddress.getByName(host);
                    System.out.println("✓ " + host + " resolvido para: " + addr.getHostAddress());
                } catch (Exception e) {
                    System.out.println("✗ Falha ao resolver " + host + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro na verificação DNS: " + e.getMessage());
        }
    }

    private void testTcpConnectivity() {
        try {
            System.out.println("Testando conectividade TCP...");
            String[] hosts = {
                "ac-go8uywq-shard-00-00.dfxd3lp.mongodb.net",
                "ac-go8uywq-shard-00-01.dfxd3lp.mongodb.net",
                "ac-go8uywq-shard-00-02.dfxd3lp.mongodb.net"
            };
            
            for (String host : hosts) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, 27017), 10000);
                    System.out.println("✓ TCP conectado com sucesso: " + host + ":27017");
                } catch (SocketTimeoutException e) {
                    System.out.println("✗ Timeout ao conectar: " + host + ":27017");
                } catch (Exception e) {
                    System.out.println("✗ Falha TCP " + host + ":27017 - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro na verificação TCP: " + e.getMessage());
        }
    }

    private void showEnvironmentInfo() {
        System.out.println("=== INFORMAÇÕES DO AMBIENTE ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("MongoDB URI configurada: " + (mongoUri != null && !mongoUri.isEmpty() ? "Sim" : "Não"));
        System.out.println("TLS Protocols: " + System.getProperty("https.protocols", "default"));
        System.out.println("Trust Store: " + System.getProperty("javax.net.ssl.trustStore", "default"));
        System.out.println("=====================================");
    }
}