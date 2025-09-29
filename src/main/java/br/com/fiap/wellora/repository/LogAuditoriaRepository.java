package br.com.fiap.wellora.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.wellora.model.LogAuditoria;

@Repository
public interface LogAuditoriaRepository extends MongoRepository<LogAuditoria, String> {
    List<LogAuditoria> findByLevelOrderByTimestampDesc(String level);
    List<LogAuditoria> findByMessageContainingOrderByTimestampDesc(String message);
    List<LogAuditoria> findAllByOrderByTimestampDesc();
}
