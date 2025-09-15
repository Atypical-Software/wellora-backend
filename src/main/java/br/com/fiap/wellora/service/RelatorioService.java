package br.com.fiap.wellora.service;

import br.com.fiap.wellora.dto.RelatorioAdminResponse;
import br.com.fiap.wellora.model.CheckinHumor;
import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import br.com.fiap.wellora.repository.CheckinHumorRepository;
import br.com.fiap.wellora.repository.QuestionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    @Autowired
    private CheckinHumorRepository checkinHumorRepository;

    @Autowired
    private QuestionarioRepository questionarioRepository;

    @Autowired
    private JwtService jwtService;

    public RelatorioAdminResponse gerarRelatorioAdmin(String token) throws Exception {
        RelatorioAdminResponse relatorio = new RelatorioAdminResponse();
        relatorio.setTitulo("Relatório de Bem-estar Organizacional");

        // Dados de pesquisas
        List<QuestionarioPsicossocial> questionarios = questionarioRepository.findAll();
        int totalQuestionarios = questionarios.size();
        RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
            totalQuestionarios, totalQuestionarios + 10, (totalQuestionarios * 100) / (totalQuestionarios + 10));
        relatorio.setPesquisas(pesquisas);

        // Dados de sentimentos
        List<CheckinHumor> checkins = checkinHumorRepository.findAll();
        Map<String, Long> contagemHumor = checkins.stream()
            .collect(Collectors.groupingBy(CheckinHumor::getHumor, Collectors.counting()));

        List<RelatorioAdminResponse.SentimentoInfo> sentimentos = new ArrayList<>();
        int totalCheckins = checkins.size();
        for (Map.Entry<String, Long> entry : contagemHumor.entrySet()) {
            int quantidade = entry.getValue().intValue();
            int porcentagem = totalCheckins > 0 ? (quantidade * 100) / totalCheckins : 0;
            sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                entry.getKey(), quantidade, porcentagem));
        }
        relatorio.setSentimentos(sentimentos);

        // Dados de colaboradores com cansaço
        long cansados = checkins.stream().filter(c -> "cansado".equals(c.getHumor())).count();
        int porcentagemCansado = totalCheckins > 0 ? (int) ((cansados * 100) / totalCheckins) : 0;
        RelatorioAdminResponse.ColaboradoresInfo colaboradores = new RelatorioAdminResponse.ColaboradoresInfo(
            "Últimos 30 dias", porcentagemCansado, 100 - porcentagemCansado);
        relatorio.setColaboradoresComCansaco(colaboradores);

        return relatorio;
    }

    public Object gerarRelatorioUsuario(String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        Map<String, Object> relatorio = new HashMap<>();
ECHO está ativado.
        List<CheckinHumor> checkins = checkinHumorRepository.findByUsuarioIdOrderByDataHoraDesc(email);
        List<QuestionarioPsicossocial> questionarios = questionarioRepository.findByUsuarioIdOrderByDataPreenchimentoDesc(email);
ECHO está ativado.
        relatorio.put("checkinsRecentes", checkins.stream().limit(10).collect(Collectors.toList()));
        relatorio.put("questionarios", questionarios);
ECHO está ativado.
        return relatorio;
    }
}
