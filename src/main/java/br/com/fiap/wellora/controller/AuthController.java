package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.dto.LoginRequest;
import br.com.fiap.wellora.dto.LoginResponse;
import br.com.fiap.wellora.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        if (authService.isValidToken(token)) {
            return ResponseEntity.ok("Token válido");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
