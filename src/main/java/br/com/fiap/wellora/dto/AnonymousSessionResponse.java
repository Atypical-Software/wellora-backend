package br.com.fiap.wellora.dto;

import java.time.LocalDateTime;

public class AnonymousSessionResponse {
    private String sessionId;
    private String token;
    private LocalDateTime expiresAt;

    // Construtores
    public AnonymousSessionResponse() {}

    public AnonymousSessionResponse(String sessionId, String token, LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // Getters e Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
