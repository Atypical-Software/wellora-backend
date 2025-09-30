package br.com.fiap.wellora.controller;

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
@RequestMapping("/api/admin-reset")
@CrossOrigin(origins = "*")
public class AdminResetController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String newPassword = request.getOrDefault("password", "admin123");
            
            Optional<AdminUser> adminOpt = adminUserRepository.findByEmail(email);
            
            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();
                admin.setPassword(passwordEncoder.encode(newPassword));
                adminUserRepository.save(admin);
                
                response.put("status", "success");
                response.put("email", email);
                response.put("newPassword", newPassword);
                response.put("message", "Password reset successfully");
                
                
            } else {
                response.put("status", "not_found");
                response.put("message", "User not found: " + email);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error resetting password: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-admins")
    public ResponseEntity<Map<String, Object>> listAdmins() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var admins = adminUserRepository.findAll();
            response.put("status", "success");
            response.put("count", admins.size());
            response.put("admins", admins.stream().map(admin -> {
                Map<String, Object> adminData = new HashMap<>();
                adminData.put("email", admin.getEmail());
                adminData.put("name", admin.getName());
                adminData.put("role", admin.getRole());
                adminData.put("active", admin.isActive());
                adminData.put("created", admin.getCreatedAt());
                return adminData;
            }).toList());
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error listing admins: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}