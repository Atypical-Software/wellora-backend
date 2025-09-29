package br.com.fiap.wellora.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.model.AdminUser;
import br.com.fiap.wellora.repository.AdminUserRepository;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/admin-check")
    public ResponseEntity<Map<String, Object>> checkAdminUser() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<AdminUser> adminOpt = adminUserRepository.findByEmail("admin@wellora.com");
            
            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();
                response.put("status", "found");
                response.put("email", admin.getEmail());
                response.put("name", admin.getName());
                response.put("role", admin.getRole());
                response.put("active", admin.isActive());
                response.put("created", admin.getCreatedAt());
                response.put("passwordLength", admin.getPassword().length());
                response.put("message", "Admin user exists");
            } else {
                response.put("status", "not_found");
                response.put("message", "Admin user not found");
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error checking admin: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recreate-admin")
    public ResponseEntity<Map<String, Object>> recreateAdminUser() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Remove admin existente se houver
            adminUserRepository.findByEmail("admin@wellora.com")
                .ifPresent(admin -> adminUserRepository.delete(admin));
            
            // Cria novo admin
            AdminUser admin = new AdminUser();
            admin.setEmail("admin@wellora.com");
            admin.setName("Administrador");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            
            AdminUser savedAdmin = adminUserRepository.save(admin);
            
            response.put("status", "created");
            response.put("email", savedAdmin.getEmail());
            response.put("name", savedAdmin.getName());
            response.put("message", "Admin user recreated successfully");
            response.put("password", "admin123");
            
            System.out.println("✅ Admin user recreated: admin@wellora.com / admin123");
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error creating admin: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            Optional<AdminUser> adminOpt = adminUserRepository.findByEmail(email);
            
            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();
                boolean matches = passwordEncoder.matches(password, admin.getPassword());
                
                response.put("status", "tested");
                response.put("email", email);
                response.put("passwordMatches", matches);
                response.put("active", admin.isActive());
                response.put("message", matches ? "Password is correct" : "Password is incorrect");
            } else {
                response.put("status", "not_found");
                response.put("message", "User not found");
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error testing password: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}