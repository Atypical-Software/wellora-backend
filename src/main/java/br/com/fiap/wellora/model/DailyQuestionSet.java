package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para conjuntos diários de perguntas
 */
@Document(collection = "dailyQuestionSets")
public class DailyQuestionSet {

    @Id
    private String id;
    
    private LocalDateTime date;
    private List<String> questions; // Lista de IDs das perguntas
    private LocalDateTime createdAt;
    private boolean isActive;

    // Construtores
    public DailyQuestionSet() {}

    public DailyQuestionSet(LocalDateTime date, List<String> questions) {
        this.date = date;
        this.questions = questions;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public List<String> getQuestions() { return questions; }
    public void setQuestions(List<String> questions) { this.questions = questions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}