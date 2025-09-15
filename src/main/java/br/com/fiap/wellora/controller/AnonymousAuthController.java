package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.model.AnonymousSession;
import br.com.fiap.wellora.dto.AnonymousSessionRequest;
import br.com.fiap.wellora.dto.AnonymousSessionResponse;
import br.com.fiap.wellora.dto.ValidationResponse;
import br.com.fiap.wellora.service.AnonymousSessionService;
import br.com.fiap.wellora.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticação anônima
 * Substitui o sistema de login tradicional por sessões temporárias
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AnonymousAuthController {

    @Autowired
    private AnonymousSessionService anonymousSessionService;

    @Autowired
    private JwtService jwtService;

    /**
     * Cria uma nova sessão anônima para o dispositivo
     */
    @PostMapping("/anonymous-session")
    public ResponseEntity<AnonymousSessionResponse> createAnonymousSession(
            @RequestBody AnonymousSessionRequest request) {

        try {
            // Verificar se dispositivo já tem sessão ativa
            AnonymousSession existingSession = anonymousSessionService
                .findActiveSessionByDevice(request.getDeviceId());

            if (existingSession != null && existingSession.isValid()) {
                // Reutilizar sessão existente
                String token = jwtService.generateAnonymousToken(
                    existingSession.getSessionId(),
                    existingSession.getEmpresaId()
                );

                return ResponseEntity.ok(new AnonymousSessionResponse(
                    existingSession.getSessionId(),
                    token,
                    existingSession.getExpiresAt()
                ));
            }

            // Criar nova sessão
            AnonymousSession session = anonymousSessionService.createSession(
                request.getEmpresaId(),
                request.getDeviceId(),
                request.getMetadata()
            );

            String token = jwtService.generateAnonymousToken(
                session.getSessionId(),
                session.getEmpresaId()
            );

            return ResponseEntity.ok(new AnonymousSessionResponse(
                session.getSessionId(),
                token,
                session.getExpiresAt()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AnonymousSessionResponse(null, null, null));
        }
    }

    /**
     * Valida uma sessão anônima
     */
    @PostMapping("/validate-session")
    public ResponseEntity<ValidationResponse> validateSession(
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidationResponse(false, null));
            }

            String token = authHeader.substring(7);

            if (jwtService.isValidAnonymousToken(token)) {
                String sessionId = jwtService.getSessionIdFromToken(token);
                AnonymousSession session = anonymousSessionService.findBySessionId(sessionId);

                if (session != null && session.isValid()) {
                    return ResponseEntity.ok(new ValidationResponse(true, "admin"));
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ValidationResponse(false, null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ValidationResponse(false, null));
        }
    }
}
