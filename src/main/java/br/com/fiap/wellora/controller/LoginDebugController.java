package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.dto.LoginRequest;
import br.com.fiap.wellora.dto.LoginResponse;
import br.com.fiap.wellora.model.AdminUser;
import br.com.fiap.wellora.repository.AdminUserRepository;
import br.com.fiap.wellora.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class LoginDebugController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/test-login")
    public ResponseEntity<Map<String, Object>> testLogin(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            System.out.println("🔍 DEBUG LOGIN - Email: " + email + ", Password: " + password);
            
            // Buscar usuário
            Optional<AdminUser> userOpt = adminUserRepository.findByEmail(email);
            
            if (!userOpt.isPresent()) {
                response.put("status", "user_not_found");
                response.put("message", "Usuário não encontrado");
                response.put("email", email);
                System.out.println("❌ Usuário não encontrado: " + email);
                return ResponseEntity.ok(response);
            }
            
            AdminUser user = userOpt.get();
            
            // Verificar se usuário está ativo
            if (!user.isActive()) {
                response.put("status", "user_inactive");
                response.put("message", "Usuário inativo");
                response.put("email", email);
                System.out.println("❌ Usuário inativo: " + email);
                return ResponseEntity.ok(response);
            }
            
            // Testar senha
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            
            if (!passwordMatches) {
                response.put("status", "wrong_password");
                response.put("message", "Senha incorreta");
                response.put("email", email);
                response.put("passwordHash", user.getPassword().substring(0, 20) + "...");
                System.out.println("❌ Senha incorreta para: " + email);
                return ResponseEntity.ok(response);
            }
            
            // Gerar token JWT
            String token = jwtService.generateToken(user.getEmail());
            
            // Sucesso!
            response.put("status", "success");
            response.put("message", "Login realizado com sucesso");
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole());
            response.put("token", token);
            response.put("isActive", user.isActive());
            
            System.out.println("✅ Login bem-sucedido para: " + email);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro interno: " + e.getMessage());
            System.err.println("❌ Erro no login debug: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String newPassword = request.getOrDefault("password", "admin123");
            
            Optional<AdminUser> userOpt = adminUserRepository.findByEmail(email);
            
            if (!userOpt.isPresent()) {
                response.put("status", "user_not_found");
                response.put("message", "Usuário não encontrado");
                return ResponseEntity.ok(response);
            }
            
            AdminUser user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            adminUserRepository.save(user);
            
            response.put("status", "success");
            response.put("message", "Senha resetada com sucesso");
            response.put("email", email);
            response.put("newPassword", newPassword);
            
            System.out.println("✅ Senha resetada para: " + email + " / " + newPassword);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro ao resetar senha: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-users")
    public ResponseEntity<Map<String, Object>> listUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var users = adminUserRepository.findAll();
            response.put("status", "success");
            response.put("count", users.size());
            response.put("users", users.stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("email", user.getEmail());
                userInfo.put("name", user.getName());
                userInfo.put("role", user.getRole());
                userInfo.put("active", user.isActive());
                userInfo.put("created", user.getCreatedAt());
                return userInfo;
            }).toList());
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro ao listar usuários: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}