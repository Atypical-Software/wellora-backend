package br.com.fiap.wellora.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String nome;

    public LoginResponse() {}

    public LoginResponse(String token, String role, String nome) {
        this.token = token;
        this.role = role;
        this.nome = nome;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
