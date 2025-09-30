package br.com.fiap.wellora.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import br.com.fiap.wellora.dto.RelatorioAdminResponse;
import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import br.com.fiap.wellora.repository.QuestionarioRepository;
import br.com.fiap.wellora.repository.ResponseAnalyticsRepository;

@Service
public class RelatorioService {
    
    @Autowired
    private ResponseAnalyticsRepository responseAnalyticsRepository;
    
    @Autowired
    private QuestionarioRepository questionarioRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    public RelatorioAdminResponse gerarRelatorioAdmin(String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);

        RelatorioAdminResponse relatorio = new RelatorioAdminResponse();
        relatorio.setTitulo("Relatório Administrativo - Wellora");
        
        long totalRespostas = mongoTemplate.count(new Query(), "anonymous_responses");

        RelatorioAdminResponse.PesquisasInfo pesquisas = new RelatorioAdminResponse.PesquisasInfo(
            (int) totalRespostas, (int) totalRespostas, 100);
        relatorio.setPesquisas(pesquisas);

        List<RelatorioAdminResponse.SentimentoInfo> sentimentos = new ArrayList<>();
        
        if (totalRespostas > 0) {
            Query query = new Query();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> responses = mongoTemplate.find(query, Map.class, "anonymous_responses")
                .stream()
                .map(response -> (Map<String, Object>) response)
                .collect(Collectors.toList());
            
            int felizes = 0;
            int neutros = 0; 
            int cansados = 0;
            
            for (Map<String, Object> response : responses) {
                Map<String, String> respostas = extrairRespostas(response);
                
                if (respostas != null && !respostas.isEmpty()) {
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
            
            if (felizes > 0) {
                sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                    "feliz", felizes, (felizes * 100) / (int) totalRespostas));
            }
            if (neutros > 0) {
                sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                    "neutro", neutros, (neutros * 100) / (int) totalRespostas));
            }
            if (cansados > 0) {
                sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                    "cansado", cansados, (cansados * 100) / (int) totalRespostas));
            }
        }
        
        relatorio.setSentimentos(sentimentos);

        int totalSentimentos = sentimentos.stream().mapToInt(RelatorioAdminResponse.SentimentoInfo::getQuantidade).sum();
        int cansadosTotal = sentimentos.stream()
            .filter(s -> "cansado".equals(s.getTipo()))
            .mapToInt(RelatorioAdminResponse.SentimentoInfo::getQuantidade)
            .sum();
            
        int porcentagemCansado = totalSentimentos > 0 ? (cansadosTotal * 100) / totalSentimentos : 0;
        RelatorioAdminResponse.ColaboradoresInfo colaboradores = new RelatorioAdminResponse.ColaboradoresInfo(
            "Últimos 30 dias", porcentagemCansado, 100 - porcentagemCansado);
        relatorio.setColaboradoresComCansaco(colaboradores);

        return relatorio;
    }
    
    private Map<String, String> extrairRespostas(Map<String, Object> response) {
        Map<String, String> respostas = null;
        
        Object responsesObj = response.get("responses");
        if (responsesObj != null) {
            respostas = convertToStringMap(responsesObj);
        } else {
            Map<String, Object> tempMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : response.entrySet()) {
                String key = entry.getKey();
                if (!key.equals("_id") && !key.equals("sessionId") && !key.equals("timestamp") && 
                    !key.equals("_class") && entry.getValue() instanceof String) {
                    tempMap.put(key, entry.getValue());
                }
            }
            if (!tempMap.isEmpty()) {
                respostas = convertToStringMap(tempMap);
            }
        }
        
        return respostas;
    }
    
    public Object gerarRelatorioUsuario(String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        Map<String, Object> relatorio = new HashMap<>();
        
        List<QuestionarioPsicossocial> questionarios = questionarioRepository.findByUsuarioIdOrderByDataPreenchimentoDesc(email);
        
        long countAnonymousResponses = mongoTemplate.count(new Query(), "anonymous_responses");
        
        relatorio.put("checkinsRecentes", new ArrayList<>());
        relatorio.put("questionarios", questionarios);
        relatorio.put("totalPesquisasAnonimas", countAnonymousResponses);
        
        return relatorio;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> convertToStringMap(Object responsesObj) {
        try {
            if (responsesObj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) responsesObj;
                Map<String, String> result = new HashMap<>();
                
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    
                    if (value != null) {
                        result.put(key, value.toString());
                    }
                }
                
                return result;
            }
        } catch (Exception e) {
            // Log error if needed
        }
        
        return null;
    }
    
    private int analisarSentimentoDasRespostas(Map<String, String> respostas) {
        int pontuacao = 0;
        
        for (String resposta : respostas.values()) {
            if (resposta == null) continue;
            
            String resp = resposta.toLowerCase().trim();
            
            if (resp.contains("muito bem") || resp.contains("ótimo") || resp.contains("excelente") || 
                resp.contains("satisfeito") || resp.contains("bem") || resp.contains("motivado") ||
                resp.contains("feliz") || resp.contains("positivo") || resp.contains("bom")) {
                pontuacao += 1;
            }
            
            else if (resp.contains("cansado") || resp.contains("estressado") || resp.contains("ruim") ||
                     resp.contains("mal") || resp.contains("insatisfeito") || resp.contains("péssimo") ||
                     resp.contains("desmotivado") || resp.contains("triste") || resp.contains("negativo")) {
                pontuacao -= 1;
            }
        }
        
        return pontuacao;
    }
}