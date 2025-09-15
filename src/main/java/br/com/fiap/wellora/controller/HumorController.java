package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.model.CheckinHumor;
import br.com.fiap.wellora.service.HumorService;
import br.com.fiap.wellora.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/humor")
@CrossOrigin(origins = "*")
public class HumorController {

    @Autowired
    private HumorService humorService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Check-in tradicional para usuários logados
     */
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

    /**
     * Check-in anônimo para usuários anônimos
     */
    @PostMapping("/anonymous-checkin")
    public ResponseEntity<Map<String, String>> registrarCheckinAnonimo(
            @RequestBody Map<String, Object> checkinData,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Validar token anônimo
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            if (!jwtService.isValidAnonymousToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String sessionId = jwtService.getSessionIdFromToken(token);
            String empresaId = jwtService.getEmpresaIdFromToken(token);

            // Criar documento de check-in anônimo
            Map<String, Object> checkinDocument = new HashMap<>();
            checkinDocument.put("sessionId", sessionId);
            checkinDocument.put("empresaId", empresaId);
            checkinDocument.put("nivelHumor", checkinData.get("nivelHumor"));
            checkinDocument.put("observacoes", checkinData.get("observacoes"));
            checkinDocument.put("timestamp", java.time.Instant.now());
            checkinDocument.put("date", java.time.LocalDate.now().toString());

            // Salvar no MongoDB
            mongoTemplate.save(checkinDocument, "anonymous_humor_checkins");

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Check-in registrado com sucesso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erro ao registrar check-in");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Histórico tradicional para usuários logados
     */
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
