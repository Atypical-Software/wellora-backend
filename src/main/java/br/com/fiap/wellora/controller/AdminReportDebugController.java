package br.com.fiap.wellora.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class AdminReportDebugController {

    /**
     * Endpoint simplificado que retorna dados mock para teste
     */
    @GetMapping("/admin-report-mock")
    public ResponseEntity<Map<String, Object>> getAdminReportMock() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("titulo", "Relatório de Bem-estar Organizacional");
            
            // Dados de pesquisas
            Map<String, Object> pesquisas = new HashMap<>();
            pesquisas.put("concluidas", 5);
            pesquisas.put("total", 15);
            pesquisas.put("porcentagem", 33);
            response.put("pesquisas", pesquisas);
            
            // Dados de sentimentos
            List<Map<String, Object>> sentimentos = new ArrayList<>();
            
            Map<String, Object> sentimento1 = new HashMap<>();
            sentimento1.put("tipo", "feliz");
            sentimento1.put("quantidade", 8);
            sentimento1.put("porcentagem", 40);
            sentimentos.add(sentimento1);
            
            Map<String, Object> sentimento2 = new HashMap<>();
            sentimento2.put("tipo", "neutro");
            sentimento2.put("quantidade", 7);
            sentimento2.put("porcentagem", 35);
            sentimentos.add(sentimento2);
            
            Map<String, Object> sentimento3 = new HashMap<>();
            sentimento3.put("tipo", "cansado");
            sentimento3.put("quantidade", 5);
            sentimento3.put("porcentagem", 25);
            sentimentos.add(sentimento3);
            
            response.put("sentimentos", sentimentos);
            
            // Dados de colaboradores com cansaço
            Map<String, Object> colaboradores = new HashMap<>();
            colaboradores.put("periodo", "Últimos 30 dias");
            colaboradores.put("porcentagemCansado", 25);
            colaboradores.put("porcentagemOk", 75);
            response.put("colaboradoresComCansaco", colaboradores);
            
            response.put("status", "success");
            response.put("message", "Dados mock para teste");
            
            System.out.println("✅ Retornando dados mock do relatório admin");
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro ao gerar dados mock: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint que força um delay para testar loading
     */
    @GetMapping("/admin-report-slow")
    public ResponseEntity<Map<String, Object>> getAdminReportSlow() {
        try {
            // Simular processamento lento
            Thread.sleep(3000);
            return getAdminReportMock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Interrompido");
            return ResponseEntity.ok(response);
        }
    }
}