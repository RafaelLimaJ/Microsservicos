package com.example.notificacao.controller;

import com.example.notificacao.model.LogNotificacao;
import com.example.notificacao.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoRepository repository;

    @PostMapping
    public ResponseEntity<LogNotificacao> enviarNotificacao(@RequestBody LogNotificacao log) {
        LogNotificacao salvo = repository.save(new LogNotificacao(log.getDestinatario(), log.getMensagem()));
        return ResponseEntity.ok(salvo);
    }
}
