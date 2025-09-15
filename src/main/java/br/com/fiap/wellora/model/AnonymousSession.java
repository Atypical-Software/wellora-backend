package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

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
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isActive;
    private Map<String, String> metadata;

    // Construtores
    public AnonymousSession() {}

    public AnonymousSession(String sessionId, String empresaId, String deviceId) {
        this.sessionId = sessionId;
        this.empresaId = empresaId;
        this.deviceId = deviceId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMonths(3); // 3 meses
        this.isActive = true;
    }

    // Getters e Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    // Métodos de utilidade
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isValid() { return isActive && !isExpired(); }
}
