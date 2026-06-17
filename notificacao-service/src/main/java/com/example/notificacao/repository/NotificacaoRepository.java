package com.example.notificacao.repository;

import com.example.notificacao.model.LogNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<LogNotificacao, Long> {
}
