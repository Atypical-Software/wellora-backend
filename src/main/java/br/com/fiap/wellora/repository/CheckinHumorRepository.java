package br.com.fiap.wellora.repository;

import br.com.fiap.wellora.model.CheckinHumor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckinHumorRepository extends MongoRepository<CheckinHumor, String> {
    List<CheckinHumor> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);
    List<CheckinHumor> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<CheckinHumor> findByUsuarioIdAndDataHoraBetween(String usuarioId, LocalDateTime inicio, LocalDateTime fim);
}
