package br.com.fiap.wellora.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.wellora.dto.RelatorioAdminResponse;
import br.com.fiap.wellora.model.CheckinHumor;
import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import br.com.fiap.wellora.repository.CheckinHumorRepository;
import br.com.fiap.wellora.repository.QuestionarioRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class RelatorioService {

    @Autowired
    private CheckinHumorRepository checkinHumorRepository;

    @Autowired
    private QuestionarioRepository questionarioRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtService jwtService;

    public RelatorioAdminResponse gerarRelatorioAdmin(String token) throws Exception {
        RelatorioAdminResponse relatorio = new RelatorioAdminResponse();
        relatorio.setTitulo("Relatório de Bem-estar Organizacional");

        // Buscar dados REAIS da collection anonymous_responses onde estão sendo salvos
        long countAnonymousResponses = mongoTemplate.count(new Query(), "anonymous_responses");
        
        // Dados de pesquisas baseados nos dados REAIS do anonymous_responses
        int totalRespostas = (int) countAnonymousResponses;
        int totalQuestionarios = totalRespostas; // Cada resposta é um questionário respondido
        
        if (totalRespostas > 0) {
            // Usar dados reais de anonymous_responses
            int meta = totalRespostas + 10; // Meta um pouco maior
            int porcentagemConclusao = (totalRespostas * 100) / meta;
            RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
                totalQuestionarios, meta, porcentagemConclusao);
            relatorio.setPesquisas(pesquisas);
        } else {
            // Fallback se não houver dados
            RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
                0, 10, 0);
            relatorio.setPesquisas(pesquisas);
        }

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
        List<CheckinHumor> checkins = checkinHumorRepository.findByUsuarioIdOrderByDataHoraDesc(email);
        List<QuestionarioPsicossocial> questionarios = questionarioRepository.findByUsuarioIdOrderByDataPreenchimentoDesc(email);
        relatorio.put("checkinsRecentes", checkins.stream().limit(10).collect(Collectors.toList()));
        relatorio.put("questionarios", questionarios);
        return relatorio;
    }
}
