package br.com.fiap.wellora.service;

import br.com.fiap.wellora.model.LogAuditoria;
import br.com.fiap.wellora.repository.LogAuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    public void logarAcao(String usuarioId, String acao, String detalhes, String ip) {
        LogAuditoria log = new LogAuditoria(usuarioId, acao, detalhes, ip);
        logAuditoriaRepository.save(log);
    }
}
