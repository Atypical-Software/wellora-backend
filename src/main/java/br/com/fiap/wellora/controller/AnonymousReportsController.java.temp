package br.com.fiap.wellora.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.wellora.service.JwtService;

/**
 * Controller para relatórios de dados anônimos agregados
 * Fornece estatísticas sem comprometer a privacidade
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class AnonymousReportsController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtService jwtService;

    /**
     * Relatório de humor agregado por período
     */
    @GetMapping("/humor-summary")
    public ResponseEntity<Map<String, Object>> getHumorSummary(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "7") int days) {

        try {
            // Validar token anônimo
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            if (!jwtService.isValidAnonymousToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String empresaId = jwtService.getEmpresaIdFromToken(token);
            // Data de início
            LocalDate startDate = LocalDate.now().minusDays(days);
            String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Consulta agregada
            Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("empresaId").is(empresaId)
                    .and("date").gte(startDateStr)),
                Aggregation.group("nivelHumor").count().as("count"),
                Aggregation.project("count").and("nivelHumor").previousOperation()
            );

            @SuppressWarnings("rawtypes")
            AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, "anonymous_humor_checkins", Map.class);

            Map<String, Object> response = new HashMap<>();
            response.put("summary", results.getMappedResults());
            response.put("period", days + " days");
            response.put("empresa", empresaId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Relatório de participação nas pesquisas
     */
    @GetMapping("/participation")
    public ResponseEntity<Map<String, Object>> getParticipation(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "30") int days) {

        try {
            // Validar token anônimo
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            if (!jwtService.isValidAnonymousToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String empresaId = jwtService.getEmpresaIdFromToken(token);
            // Data de início
            LocalDate startDate = LocalDate.now().minusDays(days);
            String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Contar respostas por dia
            Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("empresaId").is(empresaId)
                    .and("date").gte(startDateStr)),
                Aggregation.group("date").count().as("responses"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.ASC, "date")
            );

            @SuppressWarnings("rawtypes")
            AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, "anonymous_responses", Map.class);

            // Contar sessões ativas
            Query sessionQuery = new Query(Criteria.where("empresaId").is(empresaId)
                .and("isActive").is(true));
            long activeSessions = mongoTemplate.count(sessionQuery, "anonymous_sessions");

            Map<String, Object> response = new HashMap<>();
            response.put("dailyResponses", results.getMappedResults());
            response.put("activeSessions", activeSessions);
            response.put("period", days + " days");
            response.put("empresa", empresaId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
