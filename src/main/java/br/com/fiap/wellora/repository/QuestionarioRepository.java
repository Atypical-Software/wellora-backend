package br.com.fiap.wellora.repository;

import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionarioRepository extends MongoRepository<QuestionarioPsicossocial, String> {
    List<QuestionarioPsicossocial> findByUsuarioIdOrderByDataPreenchimentoDesc(String usuarioId);
    List<QuestionarioPsicossocial> findByNivelRisco(String nivelRisco);
    List<QuestionarioPsicossocial> findByDataPreenchimentoBetween(LocalDateTime inicio, LocalDateTime fim);
}
