package br.com.fiap.wellora.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.model.ResponseAnalytics;
import br.com.fiap.wellora.repository.ResponseAnalyticsRepository;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class ResponseAnalyticsDebugController {

    @Autowired
    private ResponseAnalyticsRepository responseAnalyticsRepository;

    @GetMapping("/analytics")
    public ResponseEntity<Object> debugAnalytics() {
        try {
            List<ResponseAnalytics> analytics = responseAnalyticsRepository.findAll();
            long count = responseAnalyticsRepository.count();
            
            return ResponseEntity.ok(new Object() {
                public final long totalCount = count;
                public final int listSize = analytics.size();
                public final List<ResponseAnalytics> data = analytics;
                public final String message = "Debug: Analytics data from MongoDB";
            });
        } catch (Exception e) {
            return ResponseEntity.ok(new Object() {
                public final String error = e.getMessage();
                public final String message = "Error accessing ResponseAnalytics collection";
            });
        }
    }
}