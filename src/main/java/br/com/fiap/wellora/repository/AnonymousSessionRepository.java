package br.com.fiap.wellora.repository;

import br.com.fiap.wellora.model.AnonymousSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AnonymousSessionRepository extends MongoRepository<AnonymousSession, String> {

    Optional<AnonymousSession> findBySessionId(String sessionId);

    @Query("{'deviceId': ?0, 'isActive': true, 'expiresAt': {$gt: ?1}}")
    Optional<AnonymousSession> findActiveSessionByDevice(String deviceId, LocalDateTime now);

    @Query("{'empresaId': ?0, 'isActive': true}")
    long countActiveSessionsByEmpresa(String empresaId);
}
