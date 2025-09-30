package br.com.fiap.wellora.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.config.ConnectivityChecker;

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
        response.put("message", "Wellora API is alive! 🚀");
        
        return ResponseEntity.ok(response);
    }

    /**
     * ENDPOINT IDEAL PARA ROBÔS DE KEEP-ALIVE
     * Endpoint super simples e rápido para manter a API ativa
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("pong", "alive");
        response.put("timestamp", java.time.Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint ainda mais simples - retorna só texto
     */
    @GetMapping("/alive")
    public ResponseEntity<String> alive() {
        return ResponseEntity.ok("ALIVE");
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