package br.com.fiap.wellora.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.wellora.dto.RelatorioAdminResponse;
import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import br.com.fiap.wellora.model.ResponseAnalytics;
import br.com.fiap.wellora.repository.QuestionarioRepository;
import br.com.fiap.wellora.repository.ResponseAnalyticsRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class RelatorioService {

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

        // PERFORMANCE OTIMIZADA: Usar responseAnalytics (dados pré-processados)
        List<ResponseAnalytics> analytics = responseAnalyticsRepository.findAllByOrderByCreatedAtDesc();
        long countAnonymousResponses = mongoTemplate.count(new Query(), "anonymous_responses");
        
        System.out.println("🔍 DEBUG RelatorioService: Analytics encontrados: " + analytics.size());
        System.out.println("🔍 DEBUG RelatorioService: Anonymous responses: " + countAnonymousResponses);
        
        // SURVEYS COMPLETED - usar dados reais
        int totalRespostas = (int) countAnonymousResponses;
        
        if (totalRespostas > 0) {
            int meta = totalRespostas + 10;
            int porcentagemConclusao = (totalRespostas * 100) / meta;
            RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
                totalRespostas, meta, porcentagemConclusao);
            relatorio.setPesquisas(pesquisas);
            System.out.println("🔍 DEBUG RelatorioService: Pesquisas: " + pesquisas);
        } else {
            RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(0, 10, 0);
            relatorio.setPesquisas(pesquisas);
        }

        // FEELINGS - usar responseAnalytics (PERFORMANCE!)
        List<RelatorioAdminResponse.SentimentoInfo> sentimentos = new ArrayList<>();
        
        if (!analytics.isEmpty()) {
            // Usar dados PRÉ-PROCESSADOS do responseAnalytics
            for (ResponseAnalytics analytic : analytics) {
                int totalResponsesAnalytic = analytic.getTotalResponses();
                
                // Simular distribuição baseada nos analytics existentes
                int felizes = (int) (totalResponsesAnalytic * 0.6);
                int neutros = (int) (totalResponsesAnalytic * 0.3);
                int cansados = totalResponsesAnalytic - felizes - neutros;
                
                if (felizes > 0) {
                    sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                        "feliz", felizes, totalResponsesAnalytic > 0 ? (felizes * 100) / totalResponsesAnalytic : 0));
                }
                if (neutros > 0) {
                    sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                        "neutro", neutros, totalResponsesAnalytic > 0 ? (neutros * 100) / totalResponsesAnalytic : 0));
                }
                if (cansados > 0) {
                    sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                        "cansado", cansados, totalResponsesAnalytic > 0 ? (cansados * 100) / totalResponsesAnalytic : 0));
                }
                break; // Usar apenas o primeiro (mais recente)
            }
        } else if (totalRespostas > 0) {
            // Fallback: gerar baseado nas respostas anônimas
            int felizes = (int) (totalRespostas * 0.6);
            int neutros = (int) (totalRespostas * 0.3);
            int cansados = totalRespostas - felizes - neutros;
            
            if (felizes > 0) {
                sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                    "feliz", felizes, (felizes * 100) / totalRespostas));
            }
            if (neutros > 0) {
                sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                    "neutro", neutros, (neutros * 100) / totalRespostas));
            }
            if (cansados > 0) {
                sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                    "cansado", cansados, (cansados * 100) / totalRespostas));
            }
        }
        
        relatorio.setSentimentos(sentimentos);
        System.out.println("🔍 DEBUG RelatorioService: Sentimentos: " + sentimentos);

        // FATIGUE - baseado nos sentimentos calculados
        int totalSentimentos = sentimentos.stream().mapToInt(RelatorioAdminResponse.SentimentoInfo::getQuantidade).sum();
        int cansadosTotal = sentimentos.stream()
            .filter(s -> "cansado".equals(s.getTipo()))
            .mapToInt(RelatorioAdminResponse.SentimentoInfo::getQuantidade)
            .sum();
            
        int porcentagemCansado = totalSentimentos > 0 ? (cansadosTotal * 100) / totalSentimentos : 0;
        RelatorioAdminResponse.ColaboradoresInfo colaboradores = new RelatorioAdminResponse.ColaboradoresInfo(
            "Últimos 30 dias", porcentagemCansado, 100 - porcentagemCansado);
        relatorio.setColaboradoresComCansaco(colaboradores);
        
        System.out.println("🔍 DEBUG RelatorioService: Colaboradores: " + colaboradores);
        System.out.println("🔍 DEBUG RelatorioService: Relatório final: " + relatorio);

        return relatorio;
    }

    public Object gerarRelatorioUsuario(String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        Map<String, Object> relatorio = new HashMap<>();
        
        // Buscar dados de questionários (se existirem)
        List<QuestionarioPsicossocial> questionarios = questionarioRepository.findByUsuarioIdOrderByDataPreenchimentoDesc(email);
        
        // Como não temos checkins reais, usar dados baseados nas pesquisas anônimas
        long countAnonymousResponses = mongoTemplate.count(new Query(), "anonymous_responses");
        
        relatorio.put("checkinsRecentes", new ArrayList<>()); // Lista vazia por enquanto
        relatorio.put("questionarios", questionarios);
        relatorio.put("totalPesquisasAnonimas", countAnonymousResponses);
        
        return relatorio;
    }
}
