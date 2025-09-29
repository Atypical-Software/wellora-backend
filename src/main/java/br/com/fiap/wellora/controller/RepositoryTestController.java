package br.com.fiap.wellora.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.model.AdminUser;
import br.com.fiap.wellora.repository.AdminUserRepository;
import br.com.fiap.wellora.service.AdminUserService;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class RepositoryTestController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminUserService adminUserService;

    /**
     * Testa as diferentes formas de buscar o admin
     */
    @PostMapping("/test-repository")
    public ResponseEntity<Map<String, Object>> testRepository(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            // 1. findByEmail
            Optional<AdminUser> byEmail = adminUserRepository.findByEmail(email);
            response.put("findByEmail", byEmail.isPresent());
            if (byEmail.isPresent()) {
                response.put("findByEmail_active", byEmail.get().isActive());
            }
            
            // 2. findByEmailAndActiveTrue
            Optional<AdminUser> byEmailActive = adminUserRepository.findByEmailAndActiveTrue(email);
            response.put("findByEmailAndActiveTrue", byEmailActive.isPresent());
            
            // 3. AdminUserService.authenticate
            try {
                Optional<AdminUser> serviceAuth = adminUserService.authenticate(email, password);
                response.put("adminUserService_authenticate", serviceAuth.isPresent());
                
                if (serviceAuth.isPresent()) {
                    response.put("service_user_name", serviceAuth.get().getName());
                    response.put("service_user_role", serviceAuth.get().getRole());
                }
            } catch (Exception e) {
                response.put("adminUserService_authenticate", false);
                response.put("adminUserService_error", e.getMessage());
                e.printStackTrace();
            }
            
            response.put("status", "success");
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }
}