package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.config.ConnectivityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private ConnectivityChecker connectivityChecker;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("service", "wellora-backend");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/connectivity-check")
    public ResponseEntity<Map<String, Object>> connectivityCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            connectivityChecker.checkConnectivity();
            response.put("status", "Connectivity check completed");
            response.put("message", "Check server logs for details");
        } catch (Exception e) {
            response.put("status", "Error");
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Wellora Backend API is running");
        response.put("version", "1.0.0");
        response.put("endpoints", "/api/health, /api/connectivity-check");
        
        return ResponseEntity.ok(response);
    }
}