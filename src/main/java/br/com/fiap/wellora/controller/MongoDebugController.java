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
}