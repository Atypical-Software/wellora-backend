package br.com.fiap.wellora.controller;

import br.com.fiap.wellora.model.QuestionarioPsicossocial;
import br.com.fiap.wellora.service.QuestionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questionario")
@CrossOrigin(origins = "*")
public class QuestionarioController {

    @Autowired
    private QuestionarioService questionarioService;

    @PostMapping("/responder")
    public ResponseEntity<QuestionarioPsicossocial> responderQuestionario(
            @RequestBody QuestionarioPsicossocial questionario,
            @RequestHeader("Authorization") String token) {
        try {
            QuestionarioPsicossocial resultado = questionarioService.salvarQuestionario(questionario, token);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/historico")
    public ResponseEntity<List<QuestionarioPsicossocial>> obterHistorico(
            @RequestHeader("Authorization") String token) {
        try {
            List<QuestionarioPsicossocial> historico = questionarioService.obterHistoricoUsuario(token);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
