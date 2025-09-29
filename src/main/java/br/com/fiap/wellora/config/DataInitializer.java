package br.com.fiap.wellora.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.fiap.wellora.model.Usuario;
import br.com.fiap.wellora.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ConnectivityChecker connectivityChecker;

    @Override
    public void run(String... args) throws Exception {
        connectivityChecker.checkConnectivity();
        
        try {
            // Aguardar um pouco antes de tentar inicializar dados
            Thread.sleep(5000);
            initializeDefaultData();
            System.out.println("✅ Dados inicializados com sucesso!");
        } catch (Exception e) {
            System.err.println("❌ Erro ao inicializar dados: " + e.getMessage());
            // Não propagar a exceção para não quebrar a inicialização da aplicação
        }
    }
    
    private void initializeDefaultData() {
        if (!usuarioRepository.existsByEmail("admin@wellora.com")) {
            Usuario admin = new Usuario();
            admin.setEmail("admin@wellora.com");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setNome("Administrador");
            admin.setRoles(Set.of("ADMIN"));
            admin.setAtivo(true);
            usuarioRepository.save(admin);
            System.out.println("Usuário admin criado: admin@wellora.com / admin123");
        }

        if (!usuarioRepository.existsByEmail("user@wellora.com")) {
            Usuario user = new Usuario();
            user.setEmail("user@wellora.com");
            user.setSenha(passwordEncoder.encode("user123"));
            user.setNome("Usuário Teste");
            user.setRoles(Set.of("USER"));
            user.setAtivo(true);
            usuarioRepository.save(user);
            System.out.println("Usuário comum criado: user@wellora.com / user123");
        }
    }
}
