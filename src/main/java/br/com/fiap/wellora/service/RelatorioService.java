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
import br.com.fiap.wellora.model.ResponseAnalytics;
import br.com.fiap.wellora.repository.CheckinHumorRepository;
import br.com.fiap.wellora.repository.QuestionarioRepository;
import br.com.fiap.wellora.repository.ResponseAnalyticsRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class RelatorioService {

    @Autowired
    private CheckinHumorRepository checkinHumorRepository;

    @Autowired
    private QuestionarioRepository questionarioRepository;

    @Autowired
    private ResponseAnalyticsRepository responseAnalyticsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtService jwtService;

    public RelatorioAdminResponse gerarRelatorioAdmin(String token) throws Exception {
        RelatorioAdminResponse relatorio = new RelatorioAdminResponse();
        relatorio.setTitulo("Relatório de Bem-estar Organizacional");

        // Buscar dados REAIS das coleções disponíveis
        List<ResponseAnalytics> analytics = responseAnalyticsRepository.findAllByOrderByCreatedAtDesc();
        long countAnonymousResponses = mongoTemplate.count(new Query(), "anonymous_responses");
        
        // Dados de pesquisas baseados nos dados REAIS do MongoDB
        int totalRespostas = 0;
        int totalQuestionarios = 0;
        
        // Priorizar responseAnalytics se tiver dados (como você mostrou na imagem)
        if (!analytics.isEmpty()) {
            totalRespostas = analytics.stream()
                    .mapToInt(ResponseAnalytics::getTotalResponses)
                    .sum();
            totalQuestionarios = analytics.size();
            
            // Usar dados reais de responseAnalytics
            int meta = totalRespostas + 15; // Meta um pouco maior
            int porcentagemConclusao = totalRespostas > 0 ? (totalRespostas * 100) / meta : 0;
            RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
                totalQuestionarios, meta, porcentagemConclusao);
            relatorio.setPesquisas(pesquisas);
        } else if (countAnonymousResponses > 0) {
            // Usar dados de anonymous_responses se existirem
            totalRespostas = (int) countAnonymousResponses;
            int meta = totalRespostas + 10;
            int porcentagemConclusao = totalRespostas > 0 ? (totalRespostas * 100) / meta : 0;
            RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
                totalRespostas, meta, porcentagemConclusao);
            relatorio.setPesquisas(pesquisas);
        } else {
            // Fallback para dados antigos de questionarios
            List<QuestionarioPsicossocial> questionarios = questionarioRepository.findAll();
            int totalQuestionariosAntigos = questionarios.size();
            RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
                totalQuestionariosAntigos, totalQuestionariosAntigos + 10, 
                totalQuestionariosAntigos > 0 ? (totalQuestionariosAntigos * 100) / (totalQuestionariosAntigos + 10) : 0);
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
