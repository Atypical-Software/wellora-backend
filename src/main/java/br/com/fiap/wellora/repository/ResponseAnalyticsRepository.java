package br.com.fiap.wellora.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.wellora.model.ResponseAnalytics;

@Repository
public interface ResponseAnalyticsRepository extends MongoRepository<ResponseAnalytics, String> {
    
    /**
     * Busca todas as analytics ordenadas por data de criação
     */
    List<ResponseAnalytics> findAllByOrderByCreatedAtDesc();
    
    /**
     * Busca analytics por questionId
     */
    List<ResponseAnalytics> findByQuestionId(String questionId);
    
    /**
     * Conta total de respostas de todas as analytics
     */
    default int getTotalResponses() {
        return findAll().stream()
                .mapToInt(ResponseAnalytics::getTotalResponses)
                .sum();
    }
}