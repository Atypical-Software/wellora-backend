package br.com.fiap.wellora.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.wellora.model.AdminUser;

/**
 * Repository para gerenciar usuarios administradores
 */
@Repository
public interface AdminUserRepository extends MongoRepository<AdminUser, String> {

    /**
     * Busca admin por email
     */
    Optional<AdminUser> findByEmail(String email);

    /**
     * Busca admin por email e que esteja ativo
     */
    Optional<AdminUser> findByEmailAndIsActiveTrue(String email);

    /**
     * Verifica se existe admin com o email
     */
    boolean existsByEmail(String email);

    /**
     * Busca admins por empresa
     */
    java.util.List<AdminUser> findByEmpresaId(String empresaId);
}
