package br.com.fiap.wellora.controller;

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
public class SimpleReportController {

    /**
     * Endpoint simples para testar se o app recebe dados
     */
    @GetMapping("/simple-report")
    public ResponseEntity<Map<String, Object>> getSimpleReport() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("titulo", "Relatório de Bem-estar Organizacional");
            
            // Dados de pesquisas com valores fixos para teste
            Map<String, Object> pesquisas = new HashMap<>();
            pesquisas.put("concluidas", 5);
            pesquisas.put("total", 20);
            pesquisas.put("porcentagem", 25);
            response.put("pesquisas", pesquisas);
            
            // Dados vazios para o resto
            response.put("sentimentos", new Object[0]);
            
            Map<String, Object> colaboradores = new HashMap<>();
            colaboradores.put("periodo", "Últimos 30 dias");
            colaboradores.put("porcentagemCansado", 10);
            colaboradores.put("porcentagemOk", 90);
            response.put("colaboradoresComCansaco", colaboradores);
            
            System.out.println("✅ DEBUG: Retornando dados simples de teste");
            System.out.println("✅ DEBUG: pesquisas = " + pesquisas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}