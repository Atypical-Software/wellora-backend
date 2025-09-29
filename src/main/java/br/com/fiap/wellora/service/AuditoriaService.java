package br.com.fiap.wellora.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.wellora.model.LogAuditoria;
import br.com.fiap.wellora.repository.LogAuditoriaRepository;

@Service
public class AuditoriaService {

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    public void logarAcao(String usuarioId, String acao, String detalhes, String ip) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", usuarioId);
        context.put("ip", ip);
        context.put("details", detalhes);
        
        LogAuditoria log = new LogAuditoria("INFO", acao, context);
        logAuditoriaRepository.save(log);
    }
}
