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
}