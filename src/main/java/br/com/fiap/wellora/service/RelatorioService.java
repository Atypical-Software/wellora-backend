package br.com.fiap.wellora.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // FEELINGS - analisar respostas REAIS do anonymous_responses
        List<RelatorioAdminResponse.SentimentoInfo> sentimentos = new ArrayList<>();
        
        if (totalRespostas > 0) {
            // Buscar TODAS as respostas reais para análise
            Query query = new Query();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> responses = mongoTemplate.find(query, Map.class, "anonymous_responses")
                .stream()
                .map(response -> (Map<String, Object>) response)
                .collect(Collectors.toList());
            
            System.out.println("🔍 DEBUG RelatorioService: Analisando " + responses.size() + " respostas reais");
            
            int felizes = 0;
            int neutros = 0; 
            int cansados = 0;
            
            // Analisar cada resposta REAL
            for (Map<String, Object> response : responses) {
                @SuppressWarnings("unchecked")
                Map<String, String> respostas = (Map<String, String>) response.get("responses");
                
                if (respostas != null) {
                    System.out.println("🔍 DEBUG: Analisando respostas: " + respostas);
                    
                    // Analisar todas as respostas da pessoa
                    int sentimentoPessoa = analisarSentimentoDasRespostas(respostas);
                    
                    if (sentimentoPessoa > 0) {
                        felizes++;
                    } else if (sentimentoPessoa < 0) {
                        cansados++;
                    } else {
                        neutros++;
                    }
                }
            }
            
            System.out.println("🔍 DEBUG: Contagem final - Felizes: " + felizes + ", Neutros: " + neutros + ", Cansados: " + cansados);
            
            // Criar sentimentos baseados na análise REAL
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
    
    /**
     * Analisa o sentimento das respostas de uma pessoa
     * @param respostas Map com as respostas da pessoa
     * @return > 0 para feliz, < 0 para cansado, 0 para neutro
     */
    private int analisarSentimentoDasRespostas(Map<String, String> respostas) {
        int pontuacao = 0;
        
        for (String resposta : respostas.values()) {
            if (resposta == null) continue;
            
            String resp = resposta.toLowerCase().trim();
            
            // RESPOSTAS POSITIVAS/FELIZES (+1)
            if (resp.contains("muito bem") || resp.contains("ótimo") || resp.contains("excelente") || 
                resp.contains("satisfeito") || resp.contains("bem") || resp.contains("motivado") ||
                resp.contains("feliz") || resp.contains("positivo") || resp.contains("bom")) {
                pontuacao += 1;
            }
            
            // RESPOSTAS NEGATIVAS/CANSADAS (-1)
            else if (resp.contains("cansado") || resp.contains("estressado") || resp.contains("ruim") ||
                     resp.contains("mal") || resp.contains("insatisfeito") || resp.contains("péssimo") ||
                     resp.contains("desmotivado") || resp.contains("triste") || resp.contains("negativo")) {
                pontuacao -= 1;
            }
            
            // RESPOSTAS NEUTRAS (0) - "ok", "normal", "regular", etc.
            // Não alteram a pontuação
        }
        
        System.out.println("🔍 DEBUG: Pontuação do sentimento: " + pontuacao + " para respostas: " + respostas);
        return pontuacao;
    }
}
