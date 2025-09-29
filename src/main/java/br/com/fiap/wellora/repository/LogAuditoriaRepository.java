package br.com.fiap.wellora.repository;

import br.com.fiap.wellora.model.LogAuditoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogAuditoriaRepository extends MongoRepository<LogAuditoria, String> {
    List<LogAuditoria> findByLevelOrderByTimestampDesc(String level);
    List<LogAuditoria> findByMessageContainingOrderByTimestampDesc(String message);
    List<LogAuditoria> findAllByOrderByTimestampDesc();
}
