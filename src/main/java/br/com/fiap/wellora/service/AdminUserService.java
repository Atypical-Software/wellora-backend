package br.com.fiap.wellora.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fiap.wellora.model.AdminUser;
import br.com.fiap.wellora.repository.AdminUserRepository;

/**
 * Service para gerenciar usuarios administradores
 */
@Service
public class AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Autentica admin por email e senha
     */
    public Optional<AdminUser> authenticate(String email, String password) {
        System.out.println("🔍 DEBUG AdminUserService: Tentando autenticar email: " + email);
        
        Optional<AdminUser> adminOpt = adminUserRepository.findByEmailAndIsActiveTrue(email);
        
        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            System.out.println("🔍 DEBUG AdminUserService: Admin encontrado: " + admin.getName());
            System.out.println("🔍 DEBUG AdminUserService: Admin ativo: " + admin.isActive());
            
            if (passwordEncoder.matches(password, admin.getPassword())) {
                System.out.println("✅ DEBUG AdminUserService: Senha correta! Login bem-sucedido.");
                // Atualizar ultimo login
                admin.setLastLogin(LocalDateTime.now());
                adminUserRepository.save(admin);
                return Optional.of(admin);
            } else {
                System.out.println("❌ DEBUG AdminUserService: Senha incorreta!");
            }
        } else {
            System.out.println("❌ DEBUG AdminUserService: Admin não encontrado ou inativo!");
        }
        return Optional.empty();
    }

    /**
     * Cria novo admin
     */
    public AdminUser createAdmin(String email, String password, String name, String empresaId) {
        if (adminUserRepository.existsByEmail(email)) {
            throw new RuntimeException("Email ja existe");
        }
        String hashedPassword = passwordEncoder.encode(password);
        AdminUser admin = new AdminUser(email, hashedPassword, name, empresaId);
        return adminUserRepository.save(admin);
    }

    /**
     * Busca admin por ID
     */
    public Optional<AdminUser> findById(String id) {
        return adminUserRepository.findById(id);
    }

    /**
     * Busca admin por email
     */
    public Optional<AdminUser> findByEmail(String email) {
        return adminUserRepository.findByEmail(email);
    }

    /**
     * Atualiza admin
     */
    public AdminUser updateAdmin(AdminUser admin) {
        return adminUserRepository.save(admin);
    }

    /**
     * Desativa admin
     */
    public void deactivateAdmin(String id) {
        Optional<AdminUser> adminOpt = adminUserRepository.findById(id);
        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            admin.setActive(false);
            adminUserRepository.save(admin);
        }
    }
}