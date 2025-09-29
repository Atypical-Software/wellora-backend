package br.com.fiap.wellora.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Modelo para analytics de respostas
 */
@Document(collection = "responseAnalytics")
public class ResponseAnalytics {

    @Id
    private String id;
    
    private LocalDateTime date;
    private String questionId;
    private int totalResponses;
    private Map<String, Integer> responseDistribution;
    private double averageScore;
    private LocalDateTime createdAt;

    // Construtores
    public ResponseAnalytics() {}

    public ResponseAnalytics(LocalDateTime date, String questionId, int totalResponses, 
                           Map<String, Integer> responseDistribution, double averageScore) {
        this.date = date;
        this.questionId = questionId;
        this.totalResponses = totalResponses;
        this.responseDistribution = responseDistribution;
        this.averageScore = averageScore;
        this.createdAt = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public int getTotalResponses() { return totalResponses; }
    public void setTotalResponses(int totalResponses) { this.totalResponses = totalResponses; }

    public Map<String, Integer> getResponseDistribution() { return responseDistribution; }
    public void setResponseDistribution(Map<String, Integer> responseDistribution) { 
        this.responseDistribution = responseDistribution; 
    }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}