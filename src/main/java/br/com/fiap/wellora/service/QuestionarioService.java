package br.com.fiap.wellora.service;

import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import br.com.fiap.wellora.repository.QuestionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuestionarioService {

    @Autowired
    private QuestionarioRepository questionarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditoriaService auditoriaService;

    public QuestionarioPsicossocial salvarQuestionario(QuestionarioPsicossocial questionario, String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        questionario.setUsuarioId(email);
ECHO está ativado.
        QuestionarioPsicossocial resultado = questionarioRepository.save(questionario);
ECHO está ativado.
        // Log de auditoria
        auditoriaService.logarAcao(email, "QUESTIONARIO_RESPONDIDO", 
            "Questionário concluído. Nível de risco: " + resultado.getNivelRisco(), "127.0.0.1");
ECHO está ativado.
        return resultado;
    }

    public List<QuestionarioPsicossocial> obterHistoricoUsuario(String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        return questionarioRepository.findByUsuarioIdOrderByDataPreenchimentoDesc(email);
    }
}
