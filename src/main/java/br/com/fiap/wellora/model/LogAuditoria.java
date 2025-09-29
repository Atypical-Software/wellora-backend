package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "logs")
public class LogAuditoria {

    @Id
    private String id;
    private String level;
    private String message;
    private Map<String, Object> context;
    private String timestamp;

    public LogAuditoria() {}

    public LogAuditoria(String level, String message, Map<String, Object> context) {
        this.level = level;
        this.message = message;
        this.context = context;
        this.timestamp = LocalDateTime.now().toString();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
