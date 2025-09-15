package br.com.fiap.wellora.dto;

public class ValidationResponse {
    private boolean valid;
    private String role;

    // Construtores
    public ValidationResponse() {}

    public ValidationResponse(boolean valid, String role) {
        this.valid = valid;
        this.role = role;
    }

    // Getters e Setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
