package br.com.fiap.wellora.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.fiap.wellora.model.Question;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminSetupController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/setup-questions")
    public ResponseEntity<Map<String, Object>> setupStandardQuestions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Limpar perguntas existentes
            mongoTemplate.remove(new Query(), Question.class);
            
            // Opções padrão para todas as perguntas
            List<String> opcoesPadrao = Arrays.asList("Excelente", "Bom", "Razoável", "Ruim", "Péssimo");
            
            // Criar 5 perguntas padrão com as opções padronizadas
            Question[] perguntasPadrao = {
                new Question("q1", "Como você está se sentindo hoje?", "bem-estar", "multiple_choice", opcoesPadrao),
                new Question("q2", "Como está sua motivação no trabalho?", "trabalho", "multiple_choice", opcoesPadrao),
                new Question("q3", "Como está sua relação com os colegas?", "social", "multiple_choice", opcoesPadrao),
                new Question("q4", "Como está seu nível de estresse?", "saude", "multiple_choice", opcoesPadrao),
                new Question("q5", "Como você avalia seu bem-estar geral?", "geral", "multiple_choice", opcoesPadrao)
            };
            
            // Salvar as perguntas
            for (Question pergunta : perguntasPadrao) {
                mongoTemplate.save(pergunta);
            }
            
            response.put("status", "success");
            response.put("message", "5 perguntas padrão criadas com sucesso");
            response.put("perguntasCriadas", perguntasPadrao.length);
            response.put("opcoesPadrao", opcoesPadrao);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro ao criar perguntas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/verify-questions")
    public ResponseEntity<Map<String, Object>> verifyQuestions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Question> questions = mongoTemplate.findAll(Question.class);
            
            response.put("status", "success");
            response.put("totalQuestions", questions.size());
            response.put("questions", questions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro ao verificar perguntas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}