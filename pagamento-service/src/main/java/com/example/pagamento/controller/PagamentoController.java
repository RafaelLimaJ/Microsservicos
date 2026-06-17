package com.example.pagamento.controller;

import com.example.pagamento.model.Pagamento;
import com.example.pagamento.repository.PagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @PostMapping
    public ResponseEntity<Pagamento> processarPagamento(@RequestBody Pagamento request) {
        String status = "SUCESSO";
        if (request.getValor() != null && request.getValor().compareTo(new BigDecimal("999")) == 0) {
            status = "FALHOU";
        }
        
        Pagamento pagamento = new Pagamento(request.getPedidoId(), request.getValor(), status);
        pagamentoRepository.save(pagamento);
        
        if ("FALHOU".equals(status)) {
            return ResponseEntity.badRequest().body(pagamento);
        }
        return ResponseEntity.ok(pagamento);
    }
}
