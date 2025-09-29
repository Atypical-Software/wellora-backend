package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Modelo para sessões anônimas - substitui o sistema de usuários
 * Garante total anonimato dos colaboradores
 */
@Document(collection = "anonymous_sessions")
public class AnonymousSession {

    @Id
    private String sessionId;

    private String empresaId;
    private String deviceId;
    private String sessionToken;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime expiresAt;
    private boolean isActive;
    private Map<String, String> metadata;
    private List<ResponseEntry> responses;

    // Classe interna para respostas
    public static class ResponseEntry {
        private String questionId;
        private String answer;
        private LocalDateTime answeredAt;

        public ResponseEntry() {}

        public ResponseEntry(String questionId, String answer, LocalDateTime answeredAt) {
            this.questionId = questionId;
            this.answer = answer;
            this.answeredAt = answeredAt;
        }

        // Getters e Setters
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public LocalDateTime getAnsweredAt() { return answeredAt; }
        public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
    }

    // Construtores
    public AnonymousSession() {}

    public AnonymousSession(String sessionId, String empresaId, String deviceId) {
        this.sessionId = sessionId;
        this.empresaId = empresaId;
        this.deviceId = deviceId;
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMonths(3); // 3 meses
        this.isActive = true;
        this.responses = new ArrayList<>();
    }

    // Getters e Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public List<ResponseEntry> getResponses() { return responses; }
    public void setResponses(List<ResponseEntry> responses) { this.responses = responses; }

    // Métodos de utilidade
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isValid() { return isActive && !isExpired(); }
}
