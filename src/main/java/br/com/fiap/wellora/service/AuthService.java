package br.com.fiap.wellora.service;

import br.com.fiap.wellora.dto.LoginRequest;
import br.com.fiap.wellora.dto.LoginResponse;
import br.com.fiap.wellora.model.Usuario;
import br.com.fiap.wellora.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public LoginResponse authenticate(LoginRequest request) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new Exception("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new Exception("Senha inválida");
        }

        String token = jwtService.generateToken(usuario.getEmail());
        String role = usuario.getRoles().iterator().next(); // Pega primeira role

        return new LoginResponse(token, role, usuario.getNome());
    }

    public boolean isValidToken(String token) {
        return jwtService.isValidToken(token);
    }
}
