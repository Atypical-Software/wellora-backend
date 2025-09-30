package br.com.fiap.wellora.controller;

import java.time.LocalDate;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.model.Question;
import br.com.fiap.wellora.service.JwtService;

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
            System.out.println("🔍 DEBUG: Iniciando busca de perguntas diárias (SISTEMA ALEATÓRIO)");
            
            // Validar token anônimo
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ DEBUG: Header Authorization inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            if (!jwtService.isValidAnonymousToken(token)) {
                System.out.println("❌ DEBUG: Token anônimo inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // NOVO SISTEMA: Seed baseado na data para aleatoriedade consistente
            LocalDate today = LocalDate.now();
            long seed = today.toEpochDay(); // Sempre a mesma seed para o mesmo dia
            
            System.out.println("🔍 DEBUG: Data de hoje: " + today);
            System.out.println("🔍 DEBUG: Seed para aleatoriedade: " + seed);

            // Buscar TODAS as perguntas disponíveis (sem filtros)
            Query query = new Query();
            List<Question> allQuestions = mongoTemplate.find(query, Question.class);
            
            System.out.println("🔍 DEBUG: Total de perguntas no banco: " + allQuestions.size());

            if (allQuestions.isEmpty()) {
                System.out.println("❌ DEBUG: Nenhuma pergunta encontrada no banco!");
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("questions", new ArrayList<>());
                emptyResponse.put("message", "Nenhuma pergunta disponível");
                emptyResponse.put("date", today.toString());
                return ResponseEntity.ok(emptyResponse);
            }

            // Selecionar 5 perguntas usando aleatoriedade baseada na data
            List<Question> dailyQuestions = selectDailyQuestions(allQuestions, seed, 5);
            
            System.out.println("🔍 DEBUG: Perguntas selecionadas para hoje: " + dailyQuestions.size());
            for (Question q : dailyQuestions) {
                System.out.println("🔍 DEBUG: - " + q.getText());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("questions", dailyQuestions);
            response.put("date", today.toString());
            response.put("totalAvailable", allQuestions.size());
            response.put("selectedCount", dailyQuestions.size());
            response.put("seed", seed);

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
            System.out.println("🔍 DEBUG: Recebendo submissão de respostas...");
            System.out.println("🔍 DEBUG: AuthHeader: " + authHeader);
            System.out.println("🔍 DEBUG: Responses: " + responses);
            
            // Validar token anônimo
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ DEBUG: AuthHeader inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            System.out.println("🔍 DEBUG: Token extraído: " + token.substring(0, 20) + "...");
            
            if (!jwtService.isValidAnonymousToken(token)) {
                System.out.println("❌ DEBUG: Token anônimo inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String sessionId = jwtService.getSessionIdFromToken(token);
            String empresaId = jwtService.getEmpresaIdFromToken(token);
            
            System.out.println("🔍 DEBUG: SessionId: " + sessionId);
            System.out.println("🔍 DEBUG: EmpresaId: " + empresaId);

            // Criar documento de resposta anônima
            Map<String, Object> responseDocument = new HashMap<>();
            responseDocument.put("sessionId", sessionId);
            responseDocument.put("empresaId", empresaId);
            responseDocument.put("responses", responses);
            responseDocument.put("submittedAt", java.time.Instant.now());
            responseDocument.put("date", LocalDate.now().toString());

            System.out.println("🔍 DEBUG: Documento a salvar: " + responseDocument);

            // Salvar no MongoDB
            mongoTemplate.save(responseDocument, "anonymous_responses");
            
            System.out.println("✅ DEBUG: Documento salvo com sucesso em anonymous_responses!");

            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Respostas enviadas com sucesso");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ DEBUG: Erro ao salvar respostas: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Erro ao enviar respostas: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Seleciona perguntas diárias aleatórias usando uma seed para garantir consistência durante o dia
     */
    private List<Question> selectDailyQuestions(List<Question> allQuestions, long seed, int count) {
        if (allQuestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Cria uma cópia da lista para embaralhar
        List<Question> shuffledQuestions = new ArrayList<>(allQuestions);
        
        // Usa a seed baseada na data para garantir que as mesmas perguntas sejam selecionadas durante todo o dia
        Random random = new Random(seed);
        Collections.shuffle(shuffledQuestions, random);
        
        // Retorna apenas o número solicitado de perguntas ou todas se houver menos que o solicitado
        int actualCount = Math.min(count, shuffledQuestions.size());
        return shuffledQuestions.subList(0, actualCount);
    }
}
