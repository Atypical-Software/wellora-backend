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
        // Validar token (sem usar email por enquanto)
        jwtService.getEmailFromToken(token);

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
            
            // Contadores para as 5 opções padronizadas
            Map<String, Integer> contagemRespostas = new HashMap<>();
            contagemRespostas.put("Excelente", 0);
            contagemRespostas.put("Bom", 0);
            contagemRespostas.put("Razoável", 0);
            contagemRespostas.put("Ruim", 0);
            contagemRespostas.put("Péssimo", 0);
            
            int totalRespostasAnalisadas = 0;
            
            for (Map<String, Object> response : responses) {
                Map<String, String> respostas = extrairRespostas(response);
                
                if (respostas != null && !respostas.isEmpty()) {
                    for (String resposta : respostas.values()) {
                        if (resposta != null) {
                            String respostaPadronizada = padronizarResposta(resposta);
                            if (respostaPadronizada != null) {
                                contagemRespostas.put(respostaPadronizada, 
                                    contagemRespostas.get(respostaPadronizada) + 1);
                                totalRespostasAnalisadas++;
                            }
                        }
                    }
                }
            }
            
            // Gerar estatísticas para cada tipo de resposta
            if (totalRespostasAnalisadas > 0) {
                for (Map.Entry<String, Integer> entry : contagemRespostas.entrySet()) {
                    if (entry.getValue() > 0) {
                        int porcentagem = (entry.getValue() * 100) / totalRespostasAnalisadas;
                        String emoji = getEmojiParaResposta(entry.getKey());
                        sentimentos.add(new RelatorioAdminResponse.SentimentoInfo(
                            emoji + " " + entry.getKey(), 
                            entry.getValue(), 
                            porcentagem
                        ));
                    }
                }
            }
        }
        
        relatorio.setSentimentos(sentimentos);

        // Calcular colaboradores com problemas (Ruim + Péssimo)
        int totalSentimentos = sentimentos.stream().mapToInt(RelatorioAdminResponse.SentimentoInfo::getQuantidade).sum();
        int problemasTotal = sentimentos.stream()
            .filter(s -> s.getTipo().contains("Ruim") || s.getTipo().contains("Péssimo"))
            .mapToInt(RelatorioAdminResponse.SentimentoInfo::getQuantidade)
            .sum();
            
        int porcentagemProblemas = totalSentimentos > 0 ? (problemasTotal * 100) / totalSentimentos : 0;
        RelatorioAdminResponse.ColaboradoresInfo colaboradores = new RelatorioAdminResponse.ColaboradoresInfo(
            "Últimos 30 dias", porcentagemProblemas, 100 - porcentagemProblemas);
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
    
    /**
     * Padroniza as respostas para as 5 opções padrão
     */
    private String padronizarResposta(String resposta) {
        if (resposta == null) return null;
        
        String resp = resposta.toLowerCase().trim();
        
        // Excelente
        if (resp.contains("excelente") || resp.contains("ótimo") || resp.contains("muito bom") || 
            resp.contains("perfeito") || resp.contains("maravilhoso")) {
            return "Excelente";
        }
        
        // Bom
        if (resp.contains("bom") || resp.contains("bem") || resp.contains("satisfeito") || 
            resp.contains("positivo") || resp.contains("legal") || resp.contains("ok")) {
            return "Bom";
        }
        
        // Razoável  
        if (resp.contains("razoável") || resp.contains("razavel") || resp.contains("regular") ||
            resp.contains("médio") || resp.contains("medio") || resp.contains("normal") ||
            resp.contains("neutro") || resp.contains("mais ou menos")) {
            return "Razoável";
        }
        
        // Ruim
        if (resp.contains("ruim") || resp.contains("mal") || resp.contains("insatisfeito") ||
            resp.contains("negativo") || resp.contains("chato") || resp.contains("difícil") ||
            resp.contains("dificil") || resp.contains("complicado")) {
            return "Ruim";
        }
        
        // Péssimo
        if (resp.contains("péssimo") || resp.contains("pessimo") || resp.contains("terrível") ||
            resp.contains("terrivel") || resp.contains("horrível") || resp.contains("horrivel") ||
            resp.contains("muito ruim") || resp.contains("muito mal")) {
            return "Péssimo";
        }
        
        // Se não encontrou correspondência, tenta inferir pela análise de sentimento
        return inferirRespostaPorSentimento(resp);
    }
    
    /**
     * Infere a resposta padrão baseada em análise de sentimento
     */
    private String inferirRespostaPorSentimento(String resposta) {
        String resp = resposta.toLowerCase().trim();
        
        // Palavras muito positivas
        if (resp.contains("feliz") || resp.contains("motivado") || resp.contains("animado") ||
            resp.contains("excited") || resp.contains("happy")) {
            return "Excelente";
        }
        
        // Palavras positivas
        if (resp.contains("satisfied") || resp.contains("tranquilo") || resp.contains("calmo")) {
            return "Bom";
        }
        
        // Palavras negativas
        if (resp.contains("cansado") || resp.contains("tired") || resp.contains("estressado") ||
            resp.contains("stressed") || resp.contains("worried") || resp.contains("preocupado")) {
            return "Ruim";
        }
        
        // Palavras muito negativas
        if (resp.contains("sad") || resp.contains("triste") || resp.contains("angry") ||
            resp.contains("raiva") || resp.contains("fear") || resp.contains("medo")) {
            return "Péssimo";
        }
        
        // Padrão para respostas não classificadas
        return "Razoável";
    }
    
    /**
     * Retorna o emoji correspondente a cada tipo de resposta
     */
    private String getEmojiParaResposta(String tipoResposta) {
        switch (tipoResposta) {
            case "Excelente": return "😍";
            case "Bom": return "😊";
            case "Razoável": return "😐";
            case "Ruim": return "😞";
            case "Péssimo": return "😭";
            default: return "📊";
        }
    }
}