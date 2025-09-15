package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.dto.RelatorioAdminResponse;
import br.com.fiap.wellora.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            RelatorioAdminResponse relatorio = relatorioService.gerarRelatorioAdmin(token);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
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
