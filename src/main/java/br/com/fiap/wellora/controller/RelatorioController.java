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

@RestController
@RequestMapping("/api/relatorio")
@CrossOrigin(origins = "*")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/admin")
    public ResponseEntity<RelatorioAdminResponse> obterRelatorioAdmin(
            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("🔍 DEBUG RelatorioController: ========== INICIO REQUISIÇÃO ==========");
            System.out.println("🔍 DEBUG RelatorioController: Recebendo requisição admin report");
            System.out.println("🔍 DEBUG RelatorioController: Token: " + token.substring(0, 20) + "...");
            System.out.println("🔍 DEBUG RelatorioController: Timestamp: " + java.time.LocalDateTime.now());
            
            RelatorioAdminResponse relatorio = relatorioService.gerarRelatorioAdmin(token);
            
            // 📋 LOGS DETALHADOS PARA DEBUG DO ANDROID
            System.out.println("📋 JSON RESPONSE COMPLETO:");
            System.out.println("📋 titulo: " + relatorio.getTitulo());
            
            if (relatorio.getPesquisas() != null) {
                System.out.println("📋 pesquisas.concluidas: " + relatorio.getPesquisas().getConcluidas());
                System.out.println("📋 pesquisas.total: " + relatorio.getPesquisas().getTotal());
                System.out.println("📋 pesquisas.porcentagem: " + relatorio.getPesquisas().getPorcentagem());
            }
            
            if (relatorio.getSentimentos() != null) {
                System.out.println("📋 sentimentos.size: " + relatorio.getSentimentos().size());
                for (int i = 0; i < relatorio.getSentimentos().size(); i++) {
                    var sentimento = relatorio.getSentimentos().get(i);
                    System.out.println("📋 sentimentos[" + i + "].tipo: " + sentimento.getTipo());
                    System.out.println("📋 sentimentos[" + i + "].quantidade: " + sentimento.getQuantidade());
                    System.out.println("📋 sentimentos[" + i + "].porcentagem: " + sentimento.getPorcentagem());
                }
            }
            
            if (relatorio.getColaboradoresComCansaco() != null) {
                System.out.println("📋 colaboradores.periodo: " + relatorio.getColaboradoresComCansaco().getPeriodo());
                System.out.println("📋 colaboradores.porcentagemCansado: " + relatorio.getColaboradoresComCansaco().getPorcentagemCansado());
                System.out.println("📋 colaboradores.porcentagemOk: " + relatorio.getColaboradoresComCansaco().getPorcentagemOk());
            }
            
            System.out.println("🔍 DEBUG RelatorioController: Relatório gerado: " + relatorio);
            System.out.println("🔍 DEBUG RelatorioController: Pesquisas: " + relatorio.getPesquisas());
            System.out.println("🔍 DEBUG RelatorioController: Sentimentos: " + relatorio.getSentimentos());
            System.out.println("🔍 DEBUG RelatorioController: Colaboradores: " + relatorio.getColaboradoresComCansaco());
            System.out.println("🔍 DEBUG RelatorioController: ========== FIM REQUISIÇÃO ==========");
            
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            System.err.println("❌ DEBUG RelatorioController: Erro: " + e.getMessage());
            e.printStackTrace();
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
