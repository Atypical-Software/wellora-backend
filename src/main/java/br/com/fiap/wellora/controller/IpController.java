package br.com.fiap.wellora.controller;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ip")
@CrossOrigin(origins = "*")
public class IpController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getIpInfo(HttpServletRequest request) {
        Map<String, Object> ipInfo = new HashMap<>();
        
        try {
            // IP público do servidor
            String publicIp = getPublicIp();
            
            // IP local do servidor
            InetAddress localHost = InetAddress.getLocalHost();
            String localIp = localHost.getHostAddress();
            String hostname = localHost.getHostName();
            
            // Headers do request
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            String xRealIp = request.getHeader("X-Real-IP");
            String remoteAddr = request.getRemoteAddr();
            
            ipInfo.put("publicIp", publicIp);
            ipInfo.put("localIp", localIp);
            ipInfo.put("hostname", hostname);
            ipInfo.put("xForwardedFor", xForwardedFor);
            ipInfo.put("xRealIp", xRealIp);
            ipInfo.put("remoteAddr", remoteAddr);
            ipInfo.put("status", "success");
            
            System.out.println("🌐 IP INFO - Public: " + publicIp + ", Local: " + localIp);
            
        } catch (Exception e) {
            ipInfo.put("error", e.getMessage());
            ipInfo.put("status", "error");
        }
        
        return ResponseEntity.ok(ipInfo);
    }
    
    private String getPublicIp() {
        try {
            java.net.URI uri = java.net.URI.create("https://api.ipify.org");
            java.net.URL url = uri.toURL();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(url.openStream())
            );
            String publicIp = reader.readLine();
            reader.close();
            return publicIp;
        } catch (Exception e) {
            return "Não foi possível obter IP público: " + e.getMessage();
        }
    }
}