package br.com.fiap.wellora.repository;

import br.com.fiap.wellora.model.LogAuditoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogAuditoriaRepository extends MongoRepository<LogAuditoria, String> {
    List<LogAuditoria> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);
    List<LogAuditoria> findByAcaoOrderByDataHoraDesc(String acao);
    List<LogAuditoria> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}
