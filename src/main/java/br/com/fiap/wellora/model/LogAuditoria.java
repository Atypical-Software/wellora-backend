package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "logs_auditoria")
public class LogAuditoria {

    @Id
    private String id;
    private String usuarioId;
    private String acao; // LOGIN, CHECKIN_HUMOR, QUESTIONARIO, etc.
    private String detalhes;
    private String ip;
    private LocalDateTime dataHora;

    public LogAuditoria() {}

    public LogAuditoria(String usuarioId, String acao, String detalhes, String ip) {
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.detalhes = detalhes;
        this.ip = ip;
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
