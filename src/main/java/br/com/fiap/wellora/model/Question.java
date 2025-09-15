package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pool de perguntas para rotação diária
 */
@Document(collection = "questions_pool")
public class Question {

    @Id
    private String questionId;

    private String texto;
    private String categoria;
    private String tipo;
    private List<String> opcoes;
    private boolean ativa;
    private int peso;
    private LocalDateTime criadaEm;

    // Construtores
    public Question() {}

    public Question(String questionId, String texto, String categoria, String tipo, List<String> opcoes) {
        this.questionId = questionId;
        this.texto = texto;
        this.categoria = categoria;
        this.tipo = tipo;
        this.opcoes = opcoes;
        this.ativa = true;
        this.peso = 1;
        this.criadaEm = LocalDateTime.now();
    }

    // Getters e Setters
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public List<String> getOpcoes() { return opcoes; }
    public void setOpcoes(List<String> opcoes) { this.opcoes = opcoes; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public int getPeso() { return peso; }
    public void setPeso(int peso) { this.peso = peso; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
}
