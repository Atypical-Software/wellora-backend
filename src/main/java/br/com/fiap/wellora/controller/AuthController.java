package br.com.fiap.wellora.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.dto.LoginRequest;
import br.com.fiap.wellora.dto.LoginResponse;
import br.com.fiap.wellora.model.AdminUser;
import br.com.fiap.wellora.service.AdminUserService;
import br.com.fiap.wellora.service.AuthService;
import br.com.fiap.wellora.service.JwtService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private JwtService jwtService;

    /**
     * Login tradicional para usuarios comuns (manter compatibilidade)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Login especifico para administradores
     */
    @PostMapping("/admin-login")
    public ResponseEntity<Map<String, Object>> adminLogin(
            @RequestBody Map<String, String> request) {

        try {
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("🔍 DEBUG AuthController: ========== ADMIN LOGIN ==========");
            System.out.println("🔍 DEBUG AuthController: Email recebido: " + email);
            System.out.println("🔍 DEBUG AuthController: Password length: " + (password != null ? password.length() : "null"));

            Optional<AdminUser> adminOpt = adminUserService.authenticate(email, password);

            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();

                System.out.println("✅ DEBUG AuthController: Login bem-sucedido para: " + admin.getName());

                // Gerar token JWT para admin
                String token = jwtService.generateToken(admin.getEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("role", "ADMIN");
                response.put("name", admin.getName());
                response.put("empresaId", admin.getEmpresaId());

                System.out.println("✅ DEBUG AuthController: Token gerado: " + token.substring(0, 20) + "...");
                System.out.println("🔍 DEBUG AuthController: ========== FIM ADMIN LOGIN ==========");

                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ DEBUG AuthController: Login falhou - credenciais inválidas");
                System.out.println("🔍 DEBUG AuthController: ========== FIM ADMIN LOGIN ==========");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

        } catch (Exception e) {
            System.err.println("❌ DEBUG AuthController: Erro no login admin: " + e.getMessage());
            e.printStackTrace();
            System.out.println("🔍 DEBUG AuthController: ========== FIM ADMIN LOGIN ==========");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validacao de token tradicional
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        if (authService.isValidToken(token)) {
            return ResponseEntity.ok("Token valido");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Validacao de token admin
     */
    @GetMapping("/validate-admin")
    public ResponseEntity<Map<String, String>> validateAdminToken(
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);

            if (jwtService.isValidToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                Optional<AdminUser> adminOpt = adminUserService.findByEmail(email);

                if (adminOpt.isPresent() && adminOpt.get().isActive()) {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "valid");
                    response.put("role", "ADMIN");
                    return ResponseEntity.ok(response);
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
