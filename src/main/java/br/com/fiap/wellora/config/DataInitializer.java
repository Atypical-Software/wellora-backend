package br.com.fiap.wellora.config;

import br.com.fiap.wellora.model.Usuario;
import br.com.fiap.wellora.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

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
        // Fazer verificação de conectividade primeiro
        connectivityChecker.checkConnectivity();
        
        try {
            // Tentar conectar e inicializar dados
            initializeDefaultData();
        } catch (Exception e) {
            System.err.println("=== ERRO AO INICIALIZAR DADOS ===");
            System.err.println("Erro: " + e.getMessage());
            System.err.println("Tipo: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }
            System.err.println("A aplicação continuará sem os dados iniciais.");
            System.err.println("===================================");
            // Não propagar a exceção para não falhar o startup da aplicação
        }
    }
    
    private void initializeDefaultData() {
        // Criar usuário admin padrão se não existir
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

        // Criar usuário comum para teste
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
