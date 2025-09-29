package br.com.fiap.wellora.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class ApiCallLogController {

    /**
     * Endpoint para verificar se o Android está fazendo chamadas
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Android conectou com sucesso!");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("endpoint", "/api/debug/ping");
        
        System.out.println("🔔 PING recebido do Android em: " + LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint extremamente simples para teste
     */
    @GetMapping("/simple-test")
    public ResponseEntity<String> simpleTest() {
        System.out.println("🔔 SIMPLE TEST chamado em: " + LocalDateTime.now());
        return ResponseEntity.ok("TESTE_OK");
    }
    
    /**
     * Endpoint que retorna dados muito simples
     */
    @GetMapping("/minimal-report")
    public ResponseEntity<Map<String, Object>> minimalReport() {
        Map<String, Object> response = new HashMap<>();
        response.put("titulo", "Relatório Teste");
        response.put("status", "ok");
        
        System.out.println("🔔 MINIMAL REPORT chamado em: " + LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}