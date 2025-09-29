package br.com.fiap.wellora.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.model.AdminUser;
import br.com.fiap.wellora.repository.AdminUserRepository;
import br.com.fiap.wellora.service.AdminUserService;
import br.com.fiap.wellora.service.JwtService;

@RestController
@RequestMapping("/api/admin-debug")
@CrossOrigin(origins = "*")
public class AdminLoginDebugController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Debug para o admin-login com logs detalhados
     */
    @PostMapping("/login-debug")
    public ResponseEntity<Map<String, Object>> loginDebug(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            System.out.println("🔍 LOGIN DEBUG - Email: " + email);
            
            // 1. Verificar se usuário existe
            Optional<AdminUser> userByEmail = adminUserRepository.findByEmail(email);
            if (!userByEmail.isPresent()) {
                response.put("status", "user_not_found");
                response.put("message", "Usuário não encontrado");
                System.out.println("❌ Usuário não encontrado");
                return ResponseEntity.ok(response);
            }
            
            AdminUser user = userByEmail.get();
            System.out.println("✅ Usuário encontrado: " + user.getEmail());
            System.out.println("📝 Nome: " + user.getName());
            System.out.println("🔐 Ativo: " + user.isActive());
            System.out.println("🏢 Empresa: " + user.getEmpresaId());
            System.out.println("👤 Role: " + user.getRole());
            
            // 2. Verificar se está ativo
            if (!user.isActive()) {
                response.put("status", "user_inactive");
                response.put("message", "Usuário inativo");
                System.out.println("❌ Usuário inativo");
                return ResponseEntity.ok(response);
            }
            
            // 3. Verificar senha
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("🔑 Senha confere: " + passwordMatches);
            
            if (!passwordMatches) {
                response.put("status", "wrong_password");
                response.put("message", "Senha incorreta");
                System.out.println("❌ Senha incorreta");
                return ResponseEntity.ok(response);
            }
            
            // 4. Testar AdminUserService.authenticate
            try {
                Optional<AdminUser> serviceAuth = adminUserService.authenticate(email, password);
                System.out.println("🔧 AdminUserService.authenticate: " + serviceAuth.isPresent());
                
                if (!serviceAuth.isPresent()) {
                    response.put("status", "service_auth_failed");
                    response.put("message", "AdminUserService.authenticate falhou");
                    System.out.println("❌ AdminUserService.authenticate falhou");
                    return ResponseEntity.ok(response);
                }
            } catch (Exception serviceEx) {
                response.put("status", "service_exception");
                response.put("message", "Exceção no AdminUserService: " + serviceEx.getMessage());
                System.out.println("💥 Exceção no AdminUserService: " + serviceEx.getMessage());
                serviceEx.printStackTrace();
                return ResponseEntity.ok(response);
            }
            
            // 5. Gerar token JWT
            try {
                String token = jwtService.generateToken(user.getEmail());
                System.out.println("🎫 Token gerado: " + token.substring(0, 20) + "...");
                
                response.put("status", "success");
                response.put("message", "Login realizado com sucesso");
                response.put("token", token);
                response.put("role", user.getRole());
                response.put("name", user.getName());
                response.put("empresaId", user.getEmpresaId());
                
                System.out.println("✅ Login bem-sucedido!");
                
            } catch (Exception jwtEx) {
                response.put("status", "jwt_exception");
                response.put("message", "Erro ao gerar JWT: " + jwtEx.getMessage());
                System.out.println("💥 Erro ao gerar JWT: " + jwtEx.getMessage());
                jwtEx.printStackTrace();
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("status", "general_exception");
            response.put("message", "Erro geral: " + e.getMessage());
            System.out.println("💥 Erro geral: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }
}