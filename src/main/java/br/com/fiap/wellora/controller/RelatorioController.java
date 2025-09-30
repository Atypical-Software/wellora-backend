package br.com.fiap.wellora.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.fiap.wellora.dto.RelatorioAdminResponse;
import br.com.fiap.wellora.service.RelatorioService;
import br.com.fiap.wellora.service.AuditoriaService;

@RestController
@RequestMapping("/api/relatorio")
@CrossOrigin(origins = "*")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;
    
    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping("/admin")
    public ResponseEntity<RelatorioAdminResponse> obterRelatorioAdmin(
            @RequestHeader("Authorization") String token) {
        try {
            RelatorioAdminResponse relatorio = relatorioService.gerarRelatorioAdmin(token);
            
            // LOG: Registrar acesso ao relatório administrativo
            auditoriaService.logarAcao(
                "admin",
                "RELATORIO_ADMIN_ACESSADO",
                "Relatório administrativo acessado com sucesso. Pesquisas: " + 
                (relatorio.getPesquisas() != null ? relatorio.getPesquisas().getConcluidas() : 0) + 
                ", Sentimentos analisados: " + 
                (relatorio.getSentimentos() != null ? relatorio.getSentimentos().size() : 0),
                "sistema"
            );
            
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            // LOG: Registrar erro no acesso ao relatório
            auditoriaService.logarAcao(
                "admin",
                "RELATORIO_ADMIN_ERRO",
                "Erro ao gerar relatório administrativo: " + e.getMessage() + " - Tipo: " + e.getClass().getSimpleName(),
                "sistema"
            );
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/usuario")
    public ResponseEntity<Object> obterRelatorioUsuario(
            @RequestHeader("Authorization") String token) {
        try {
            Object relatorio = relatorioService.gerarRelatorioUsuario(token);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
