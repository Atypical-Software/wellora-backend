package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "questionarios")
public class QuestionarioPsicossocial {

    @Id
    private String id;
    private String usuarioId;
    private Map<String, String> respostas; // pergunta -> resposta
    private int pontuacao;
    private String nivelRisco; // baixo, medio, alto
    private LocalDateTime dataPreenchimento;

    public QuestionarioPsicossocial() {}

    public QuestionarioPsicossocial(String usuarioId, Map<String, String> respostas) {
        this.usuarioId = usuarioId;
        this.respostas = respostas;
        this.dataPreenchimento = LocalDateTime.now();
        calcularPontuacao();
    }

    private void calcularPontuacao() {
        // Lógica para calcular pontuação baseada nas respostas
        this.pontuacao = respostas.size() * 2; // Exemplo simples
        if (pontuacao < 30) this.nivelRisco = "baixo";
        else if (pontuacao < 60) this.nivelRisco = "medio";
        else this.nivelRisco = "alto";
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public Map<String, String> getRespostas() { return respostas; }
    public void setRespostas(Map<String, String> respostas) { this.respostas = respostas; }

    public int getPontuacao() { return pontuacao; }
    public void setPontuacao(int pontuacao) { this.pontuacao = pontuacao; }

    public String getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(String nivelRisco) { this.nivelRisco = nivelRisco; }

    public LocalDateTime getDataPreenchimento() { return dataPreenchimento; }
    public void setDataPreenchimento(LocalDateTime dataPreenchimento) { this.dataPreenchimento = dataPreenchimento; }
}
