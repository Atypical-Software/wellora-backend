package br.com.fiap.wellora.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.fiap.wellora.model.CheckinHumor;
import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import br.com.fiap.wellora.model.Usuario;
import br.com.fiap.wellora.repository.CheckinHumorRepository;
import br.com.fiap.wellora.repository.QuestionarioRepository;
import br.com.fiap.wellora.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CheckinHumorRepository checkinHumorRepository;

    @Autowired
    private QuestionarioRepository questionarioRepository;

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
            initializeSampleData();
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

    private void initializeSampleData() {
        // Criar dados de exemplo apenas se não existirem
        long checkinCount = checkinHumorRepository.count();
        long questionarioCount = questionarioRepository.count();

        if (checkinCount == 0) {
            createSampleCheckins();
            System.out.println("✅ Check-ins de exemplo criados");
        }

        if (questionarioCount == 0) {
            createSampleQuestionarios();
            System.out.println("✅ Questionários de exemplo criados");
        }
    }

    private void createSampleCheckins() {
        Random random = new Random();
        String[] humores = {"alegre", "cansado", "estressado", "neutro", "triste"};
        String[] usuarios = {"user@wellora.com", "funcionario1@empresa.com", "funcionario2@empresa.com", 
                           "funcionario3@empresa.com", "funcionario4@empresa.com"};

        for (int i = 0; i < 50; i++) {
            CheckinHumor checkin = new CheckinHumor();
            checkin.setUsuarioId(usuarios[random.nextInt(usuarios.length)]);
            checkin.setHumor(humores[random.nextInt(humores.length)]);
            checkin.setDataHora(LocalDateTime.now().minusDays(random.nextInt(30)));
            checkin.setObservacoes("Check-in automático de exemplo");
            checkinHumorRepository.save(checkin);
        }
    }

    private void createSampleQuestionarios() {
        Random random = new Random();
        String[] usuarios = {"user@wellora.com", "funcionario1@empresa.com", "funcionario2@empresa.com", 
                           "funcionario3@empresa.com", "funcionario4@empresa.com"};

        for (int i = 0; i < 15; i++) {
            QuestionarioPsicossocial questionario = new QuestionarioPsicossocial();
            questionario.setUsuarioId(usuarios[random.nextInt(usuarios.length)]);
            questionario.setDataPreenchimento(LocalDateTime.now().minusDays(random.nextInt(30)));
            
            // Respostas de exemplo
            List<String> respostas = Arrays.asList(
                "Resposta exemplo 1", "Resposta exemplo 2", "Resposta exemplo 3",
                "Resposta exemplo 4", "Resposta exemplo 5"
            );
            questionario.setRespostas(respostas);
            questionario.setPontuacaoTotal(random.nextInt(100) + 1);
            questionarioRepository.save(questionario);
        }
    }
}
