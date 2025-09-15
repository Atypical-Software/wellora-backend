package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "checkins_humor")
public class CheckinHumor {

    @Id
    private String id;
    private String usuarioId;
    private String humor; // triste, alegre, ansioso, medo, raiva, cansado
    private String descricao;
    private LocalDateTime dataHora;

    public CheckinHumor() {}

    public CheckinHumor(String usuarioId, String humor, String descricao) {
        this.usuarioId = usuarioId;
        this.humor = humor;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getHumor() { return humor; }
    public void setHumor(String humor) { this.humor = humor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
