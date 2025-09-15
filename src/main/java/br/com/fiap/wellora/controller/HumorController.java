package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.model.CheckinHumor;
import br.com.fiap.wellora.service.HumorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/humor")
@CrossOrigin(origins = "*")
public class HumorController {

    @Autowired
    private HumorService humorService;

    @PostMapping("/checkin")
    public ResponseEntity<CheckinHumor> registrarCheckin(
            @RequestBody CheckinHumor checkin,
            @RequestHeader("Authorization") String token) {
        try {
            CheckinHumor novoCheckin = humorService.registrarCheckin(checkin, token);
            return ResponseEntity.ok(novoCheckin);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/historico")
    public ResponseEntity<List<CheckinHumor>> obterHistorico(
            @RequestHeader("Authorization") String token) {
        try {
            List<CheckinHumor> historico = humorService.obterHistoricoUsuario(token);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
