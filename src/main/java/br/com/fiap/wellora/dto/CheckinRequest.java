package br.com.fiap.wellora.dto;

public class CheckinRequest {
    private String humor;
    private String descricao;

    public CheckinRequest() {}

    public CheckinRequest(String humor, String descricao) {
        this.humor = humor;
        this.descricao = descricao;
    }

    public String getHumor() { return humor; }
    public void setHumor(String humor) { this.humor = humor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
