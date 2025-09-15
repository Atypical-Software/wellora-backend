package br.com.fiap.wellora.service;

import br.com.fiap.wellora.model.CheckinHumor;
import br.com.fiap.wellora.model.LogAuditoria;
import br.com.fiap.wellora.repository.CheckinHumorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HumorService {

    @Autowired
    private CheckinHumorRepository checkinHumorRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditoriaService auditoriaService;

    public CheckinHumor registrarCheckin(CheckinHumor checkin, String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        checkin.setUsuarioId(email);
        CheckinHumor novoCheckin = checkinHumorRepository.save(checkin);
        // Log de auditoria
        auditoriaService.logarAcao(email, "CHECKIN_HUMOR", 
            "Humor registrado: " + checkin.getHumor(), "127.0.0.1");
        return novoCheckin;
    }

    public List<CheckinHumor> obterHistoricoUsuario(String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        return checkinHumorRepository.findByUsuarioIdOrderByDataHoraDesc(email);
    }
}
