package br.com.fiap.wellora.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DataViewerController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/database-info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String dbName = mongoTemplate.getDb().getName();
            response.put("databaseName", dbName);
            
            // Listar todas as coleções
            List<String> collections = new ArrayList<>();
            mongoTemplate.getDb().listCollectionNames().forEach(collections::add);
            
            response.put("collections", collections);
            response.put("status", "success");
            
            // Para cada coleção, contar documentos
            Map<String, Long> collectionCounts = new HashMap<>();
            for (String collection : collections) {
                try {
                    long count = mongoTemplate.getCollection(collection).countDocuments();
                    collectionCounts.put(collection, count);
                } catch (Exception e) {
                    collectionCounts.put(collection, -1L);
                }
            }
            response.put("collectionCounts", collectionCounts);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view-collection/{collectionName}")
    public ResponseEntity<Map<String, Object>> viewCollection(@PathVariable String collectionName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
            List<Document> documents = new ArrayList<>();
            
            // Buscar até 20 documentos
            collection.find().limit(20).forEach(documents::add);
            
            response.put("status", "success");
            response.put("collection", collectionName);
            response.put("count", documents.size());
            response.put("documents", documents);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view-admin-users")
    public ResponseEntity<Map<String, Object>> viewAdminUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Tentar diferentes nomes de coleções possíveis
            String[] possibleCollections = {
                "admin_users", 
                "adminUsers", 
                "AdminUser", 
                "users", 
                "user",
                "User"
            };
            
            Map<String, Object> results = new HashMap<>();
            
            for (String collectionName : possibleCollections) {
                try {
                    if (mongoTemplate.collectionExists(collectionName)) {
                        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
                        List<Document> documents = new ArrayList<>();
                        collection.find().limit(10).forEach(documents::add);
                        
                        if (!documents.isEmpty()) {
                            results.put(collectionName, documents);
                        }
                    }
                } catch (Exception e) {
                    // Ignora erros de coleções específicas
                }
            }
            
            response.put("status", "success");
            response.put("foundCollections", results);
            response.put("searchedCollections", possibleCollections);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}