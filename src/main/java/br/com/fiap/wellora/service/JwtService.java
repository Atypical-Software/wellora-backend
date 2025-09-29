package br.com.fiap.wellora.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Serviço para geração e validação de tokens JWT
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400}") // 24 horas em segundos
    private long jwtExpiration;

    @Value("${jwt.anonymous.secret}")
    private String anonymousSecretKey;

    @Value("${jwt.anonymous.expiration:3600}") // 1 hora em segundos para tokens anônimos
    private long anonymousExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private SecretKey getAnonymousSigningKey() {
        return Keys.hmacShaKeyFor(anonymousSecretKey.getBytes());
    }

    /**
     * Gera token JWT para usuário administrador
     */
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ADMIN");
        claims.put("email", email);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (jwtExpiration * 1000)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Gera token JWT para sessão anônima
     */
    public String generateAnonymousToken(String sessionId, String empresaId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ANONYMOUS");
        claims.put("sessionId", sessionId);
        claims.put("empresaId", empresaId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(sessionId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (anonymousExpiration * 1000)))
                .signWith(getAnonymousSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida token JWT de admin
     */
    public boolean isValidToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return "ADMIN".equals(claims.get("type")) && 
                   claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida token JWT anônimo
     */
    public boolean isValidAnonymousToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getAnonymousSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return "ANONYMOUS".equals(claims.get("type")) && 
                   claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai email do token de admin
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrai sessionId do token anônimo
     */
    public String getSessionIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getAnonymousSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.get("sessionId", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrai empresaId do token anônimo
     */
    public String getEmpresaIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getAnonymousSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.get("empresaId", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica se o token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}