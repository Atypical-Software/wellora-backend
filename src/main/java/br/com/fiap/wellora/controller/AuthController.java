package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.dto.LoginRequest;
import br.com.fiap.wellora.dto.LoginResponse;
import br.com.fiap.wellora.service.AuthService;
import br.com.fiap.wellora.service.AdminUserService;
import br.com.fiap.wellora.service.JwtService;
import br.com.fiap.wellora.model.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

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

            Optional<AdminUser> adminOpt = adminUserService.authenticate(email, password);

            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();

                // Gerar token JWT para admin
                String token = jwtService.generateToken(admin.getEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("role", "ADMIN");
                response.put("name", admin.getName());
                response.put("empresaId", admin.getEmpresaId());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

        } catch (Exception e) {
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
