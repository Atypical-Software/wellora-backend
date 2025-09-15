package br.com.fiap.wellora.service;

import br.com.fiap.wellora.model.AnonymousSession;
import br.com.fiap.wellora.repository.AnonymousSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AnonymousSessionService {

    @Autowired
    private AnonymousSessionRepository anonymousSessionRepository;

    public AnonymousSession createSession(String empresaId, String deviceId, Map<String, String> metadata) {
        String sessionId = "anon-sess-" + UUID.randomUUID().toString();

        AnonymousSession session = new AnonymousSession(sessionId, empresaId, deviceId);
        session.setMetadata(metadata);

        return anonymousSessionRepository.save(session);
    }

    public AnonymousSession findBySessionId(String sessionId) {
        return anonymousSessionRepository.findBySessionId(sessionId).orElse(null);
    }

    public AnonymousSession findActiveSessionByDevice(String deviceId) {
        return anonymousSessionRepository.findActiveSessionByDevice(deviceId, LocalDateTime.now()).orElse(null);
    }

    public AnonymousSession extendSession(String sessionId) {
        AnonymousSession session = findBySessionId(sessionId);
        if (session != null) {
            session.setExpiresAt(LocalDateTime.now().plusMonths(3));
            return anonymousSessionRepository.save(session);
        }
        return null;
    }

    public void deactivateSession(String sessionId) {
        AnonymousSession session = findBySessionId(sessionId);
        if (session != null) {
            session.setActive(false);
            anonymousSessionRepository.save(session);
        }
    }

    public long countActiveSessionsByEmpresa(String empresaId) {
        return anonymousSessionRepository.countActiveSessionsByEmpresa(empresaId);
    }
}
