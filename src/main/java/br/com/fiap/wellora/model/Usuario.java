package br.com.fiap.wellora.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Set;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String email;
    private String senha;
    private String nome;
    private Set<String> roles;
    private LocalDateTime dataCriacao;
    private boolean ativo;

    // Construtores
    public Usuario() {}

    public Usuario(String email, String senha, String nome, Set<String> roles) {
        this.email = email;
        this.senha = senha;
        this.nome = nome;
        this.roles = roles;
        this.dataCriacao = LocalDateTime.now();
        this.ativo = true;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
