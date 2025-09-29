package br.com.fiap.wellora.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Pool de perguntas para rotação diária
 */
@Document(collection = "questions")
public class Question {

    @Id
    private String id;

    private String text;
    private String category;
    private String type;
    private List<String> options;
    private boolean isActive;
    private int priority;
    private LocalDateTime createdAt;

    // Construtores
    public Question() {}

    public Question(String id, String text, String category, String type, List<String> options) {
        this.id = id;
        this.text = text;
        this.category = category;
        this.type = type;
        this.options = options;
        this.isActive = true;
        this.priority = 1;
        this.createdAt = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
