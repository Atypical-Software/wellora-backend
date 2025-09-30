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
import br.com.fiap.wellora.service.AuditoriaService;

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
    
    @Autowired
    private AuditoriaService auditoriaService;

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

                // LOG: Registrar login de admin bem-sucedido
                auditoriaService.logarAcao(
                    email,
                    "ADMIN_LOGIN_REALIZADO",
                    "Login de administrador realizado com sucesso. Admin: " + admin.getName() + ", Empresa: " + admin.getEmpresaId(),
                    "sistema"
                );

                return ResponseEntity.ok(response);
            } else {
                // LOG: Registrar tentativa de login falhada
                auditoriaService.logarAcao(
                    email != null ? email : "email_nao_informado",
                    "ADMIN_LOGIN_FALHOU",
                    "Tentativa de login de administrador com credenciais inválidas",
                    "sistema"
                );
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

        } catch (Exception e) {
            // LOG: Registrar erro no sistema de login
            auditoriaService.logarAcao(
                "sistema",
                "ADMIN_LOGIN_ERRO",
                "Erro interno no sistema de login administrativo: " + e.getMessage() + " - Tipo: " + e.getClass().getSimpleName(),
                "sistema"
            );
            
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
