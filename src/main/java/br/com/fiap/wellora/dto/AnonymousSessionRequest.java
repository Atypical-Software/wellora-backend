package br.com.fiap.wellora.dto;

import java.util.Map;

public class AnonymousSessionRequest {
    private String empresaId;
    private String deviceId;
    private Map<String, String> metadata;

    // Construtores
    public AnonymousSessionRequest() {}

    public AnonymousSessionRequest(String empresaId, String deviceId) {
        this.empresaId = empresaId;
        this.deviceId = deviceId;
    }

    // Getters e Setters
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
