package br.com.fiap.wellora.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class AnonymousResponsesDebugController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/anonymous-responses")
    public ResponseEntity<Map<String, Object>> debugAnonymousResponses() {
        try {
            long count = mongoTemplate.count(new Query(), "anonymous_responses");
            List<Map> responses = mongoTemplate.findAll(Map.class, "anonymous_responses");
            
            Map<String, Object> result = new HashMap<>();
            result.put("collection", "anonymous_responses");
            result.put("count", count);
            result.put("exists", count > 0);
            result.put("data", responses);
            result.put("message", "Debug: anonymous_responses collection status");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("message", "Error accessing anonymous_responses collection");
            return ResponseEntity.ok(result);
        }
    }
}