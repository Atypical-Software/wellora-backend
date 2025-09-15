package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.model.Question;
import br.com.fiap.wellora.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller para gerenciamento de perguntas diárias
 * Sistema de rotação de perguntas para coleta anônima
 */
@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtService jwtService;

    /**
     * Obtém as perguntas do dia para um usuário anônimo
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyQuestions(
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

            String empresaId = jwtService.getEmpresaIdFromToken(token);

            // Calcular offset baseado na data atual
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate today = LocalDate.now();
            long daysSinceStart = ChronoUnit.DAYS.between(startDate, today);
            int offset = (int) (daysSinceStart % 100); // Ciclo de 100 dias

            // Buscar perguntas para a empresa
            Query query = new Query(Criteria.where("empresaId").is(empresaId));
            query.skip(offset).limit(5); // 5 perguntas por dia

            List<Question> questions = mongoTemplate.find(query, Question.class);

            if (questions.isEmpty()) {
                // Se não há perguntas específicas, usar perguntas padrão
                Query defaultQuery = new Query(Criteria.where("empresaId").exists(false));
                defaultQuery.skip(offset).limit(5);
                questions = mongoTemplate.find(defaultQuery, Question.class);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("questions", questions);
            response.put("date", today.toString());
            response.put("cycleDay", offset + 1);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Submete respostas anônimas
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitResponses(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> responses) {

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

            // Criar documento de resposta anônima
            Map<String, Object> responseDocument = new HashMap<>();
            responseDocument.put("sessionId", sessionId);
            responseDocument.put("empresaId", empresaId);
            responseDocument.put("responses", responses);
            responseDocument.put("submittedAt", java.time.Instant.now());
            responseDocument.put("date", LocalDate.now().toString());

            // Salvar no MongoDB
            mongoTemplate.save(responseDocument, "anonymous_responses");

            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Respostas enviadas com sucesso");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Erro ao enviar respostas");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
