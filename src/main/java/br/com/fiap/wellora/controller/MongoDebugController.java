package br.com.fiap.wellora.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class MongoDebugController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/mongo-info")
    public ResponseEntity<Map<String, Object>> getMongoInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar conexão
            String dbName = mongoTemplate.getDb().getName();
            response.put("databaseName", dbName);
            response.put("status", "connected");
            
            // Listar todas as coleções
            Set<String> collections = mongoTemplate.getDb().listCollectionNames().into(new java.util.HashSet<>());
            response.put("collections", collections);
            response.put("collectionsCount", collections.size());
            
            // Verificar coleção admin_users especificamente
            boolean adminUsersExists = mongoTemplate.collectionExists("admin_users");
            response.put("adminUsersCollectionExists", adminUsersExists);
            
            if (adminUsersExists) {
                long adminUsersCount = mongoTemplate.getCollection("admin_users").countDocuments();
                response.put("adminUsersCount", adminUsersCount);
            }
            
            // Verificar outras coleções relacionadas
            for (String collection : collections) {
                if (collection.toLowerCase().contains("admin") || collection.toLowerCase().contains("user")) {
                    long count = mongoTemplate.getCollection(collection).countDocuments();
                    response.put("collection_" + collection + "_count", count);
                }
            }
            
            System.out.println("🔍 MongoDB Info - Database: " + dbName + ", Collections: " + collections.size());
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error getting MongoDB info: " + e.getMessage());
            System.err.println("❌ MongoDB Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/raw-admin-users")
    public ResponseEntity<Map<String, Object>> getRawAdminUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Buscar dados brutos diretamente do MongoDB
            var cursor = mongoTemplate.getCollection("admin_users").find();
            var documents = new java.util.ArrayList<String>();
            
            for (var doc : cursor) {
                documents.add(doc.toJson());
            }
            
            response.put("status", "success");
            response.put("count", documents.size());
            response.put("rawDocuments", documents);
            
            System.out.println("🔍 Raw admin users count: " + documents.size());
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error getting raw admin users: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getLogs() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long count = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "logs");
            response.put("count", count);
            response.put("exists", count > 0);
            response.put("collection", "logs");
            
            if (count > 0) {
                org.springframework.data.mongodb.core.query.Query query = 
                    new org.springframework.data.mongodb.core.query.Query()
                        .with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"))
                        .limit(10);
                        
                java.util.List<Map> logs = mongoTemplate.find(query, Map.class, "logs");
                response.put("data", logs);
                response.put("message", "Debug: logs collection status");
            } else {
                response.put("data", new java.util.ArrayList<>());
                response.put("message", "No logs found in collection");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Error accessing logs collection");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/responses")
    public ResponseEntity<Map<String, Object>> getResponses() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = mongoTemplate.collectionExists("anonymous_responses");
            response.put("exists", exists);
            response.put("collection", "anonymous_responses");
            
            if (exists) {
                long count = mongoTemplate.getCollection("anonymous_responses").countDocuments();
                response.put("count", count);
                
                if (count > 0) {
                    // Buscar as últimas respostas
                    org.springframework.data.mongodb.core.query.Query query = 
                        new org.springframework.data.mongodb.core.query.Query()
                            .with(org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "_id"));
                    query.limit(5);
                    
                    java.util.List<Object> responses = mongoTemplate.find(query, Object.class, "anonymous_responses");
                    response.put("data", responses);
                } else {
                    response.put("data", new java.util.ArrayList<>());
                }
            } else {
                response.put("data", new java.util.ArrayList<>());
            }
            
            response.put("message", "Debug: anonymous_responses collection status");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("exists", false);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup-standard-questions")
    public ResponseEntity<Map<String, Object>> setupStandardQuestions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Limpar perguntas existentes
            mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(), "questions");
            
            // Opções padrão para todas as perguntas
            java.util.List<String> opcoesPadrao = java.util.Arrays.asList("Excelente", "Bom", "Razoável", "Ruim", "Péssimo");
            
            // Criar perguntas diretamente como Map para inserir no MongoDB
            java.util.List<Map<String, Object>> perguntas = new java.util.ArrayList<>();
            
            // Pergunta 1
            Map<String, Object> p1 = new HashMap<>();
            p1.put("id", "q1");
            p1.put("text", "Como você está se sentindo hoje?");
            p1.put("category", "bem-estar");
            p1.put("type", "multiple_choice");
            p1.put("options", opcoesPadrao);
            p1.put("isActive", true);
            p1.put("priority", 1);
            p1.put("createdAt", java.time.LocalDateTime.now());
            perguntas.add(p1);
            
            // Pergunta 2
            Map<String, Object> p2 = new HashMap<>();
            p2.put("id", "q2");
            p2.put("text", "Como está sua motivação no trabalho?");
            p2.put("category", "trabalho");
            p2.put("type", "multiple_choice");
            p2.put("options", opcoesPadrao);
            p2.put("isActive", true);
            p2.put("priority", 1);
            p2.put("createdAt", java.time.LocalDateTime.now());
            perguntas.add(p2);
            
            // Pergunta 3
            Map<String, Object> p3 = new HashMap<>();
            p3.put("id", "q3");
            p3.put("text", "Como está sua relação com os colegas?");
            p3.put("category", "social");
            p3.put("type", "multiple_choice");
            p3.put("options", opcoesPadrao);
            p3.put("isActive", true);
            p3.put("priority", 1);
            p3.put("createdAt", java.time.LocalDateTime.now());
            perguntas.add(p3);
            
            // Pergunta 4
            Map<String, Object> p4 = new HashMap<>();
            p4.put("id", "q4");
            p4.put("text", "Como está seu nível de estresse?");
            p4.put("category", "saude");
            p4.put("type", "multiple_choice");
            p4.put("options", opcoesPadrao);
            p4.put("isActive", true);
            p4.put("priority", 1);
            p4.put("createdAt", java.time.LocalDateTime.now());
            perguntas.add(p4);
            
            // Pergunta 5
            Map<String, Object> p5 = new HashMap<>();
            p5.put("id", "q5");
            p5.put("text", "Como você avalia seu bem-estar geral?");
            p5.put("category", "geral");
            p5.put("type", "multiple_choice");
            p5.put("options", opcoesPadrao);
            p5.put("isActive", true);
            p5.put("priority", 1);
            p5.put("createdAt", java.time.LocalDateTime.now());
            perguntas.add(p5);
            
            // Salvar todas as perguntas
            for (Map<String, Object> pergunta : perguntas) {
                mongoTemplate.save(pergunta, "questions");
            }
            
            response.put("status", "success");
            response.put("message", "5 perguntas padrão criadas com sucesso");
            response.put("perguntasCriadas", perguntas.size());
            response.put("opcoesPadrao", opcoesPadrao);
            response.put("perguntas", perguntas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro ao criar perguntas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}