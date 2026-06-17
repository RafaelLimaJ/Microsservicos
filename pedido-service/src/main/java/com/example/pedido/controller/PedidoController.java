package com.example.pedido.controller;

import com.example.pedido.model.Pedido;
import com.example.pedido.repository.PedidoRepository;
import com.example.pedido.resilience.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.estoque.url}")
    private String estoqueUrl;

    @Value("${services.pagamento.url}")
    private String pagamentoUrl;

    @Value("${services.notificacao.url}")
    private String notificacaoUrl;

    private final CircuitBreaker circuitoEstoque = new CircuitBreaker("estoque-service", 3, 10000);
    private final CircuitBreaker circuitoPagamento = new CircuitBreaker("pagamento-service", 3, 10000);
    private final CircuitBreaker circuitoNotificacao = new CircuitBreaker("notificacao-service", 3, 10000);

    @GetMapping
    public List<Pedido> obterTodosPedidos() {
        return pedidoRepository.findAll();
    }

    @GetMapping("/status-circuitos")
    public Map<String, String> obterStatusCircuitos() {
        Map<String, String> status = new HashMap<>();
        status.put("estoque-service", circuitoEstoque.getEstado().name());
        status.put("pagamento-service", circuitoPagamento.getEstado().name());
        status.put("notificacao-service", circuitoNotificacao.getEstado().name());
        return status;
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedidoReq) {
        Pedido pedido = new Pedido(pedidoReq.getCodigoSku(), pedidoReq.getQuantidade(), pedidoReq.getPreco(), "CRIADO");
        pedidoRepository.save(pedido);

        boolean estoqueReservado = circuitoEstoque.executar(
            () -> {
                String url = estoqueUrl + "/reservar?codigoSku=" + pedido.getCodigoSku() + "&quantidade=" + pedido.getQuantidade();
                restTemplate.put(url, null);
                return true;
            },
            (throwable) -> {
                return false;
            }
        );

        if (!estoqueReservado) {
            pedido.setStatus("FALHOU_ESTOQUE_INDISPONIVEL");
            pedidoRepository.save(pedido);
            return ResponseEntity.badRequest().body(pedido);
        }

        String resultadoPagamento = circuitoPagamento.executar(
            () -> {
                Map<String, Object> reqPagamento = new HashMap<>();
                reqPagamento.put("pedidoId", pedido.getId().toString());
                reqPagamento.put("valor", pedido.getPreco().multiply(new BigDecimal(pedido.getQuantidade())));
                
                ResponseEntity<Map> response = restTemplate.postForEntity(pagamentoUrl, reqPagamento, Map.class);
                return (String) response.getBody().get("status");
            },
            (throwable) -> {
                return "PAGAMENTO_PENDENTE_FALLBACK";
            }
        );

        pedido.setStatusPagamento(resultadoPagamento);
        if ("SUCESSO".equals(resultadoPagamento)) {
            pedido.setStatus("CONCLUIDO");
        } else if ("PAGAMENTO_PENDENTE_FALLBACK".equals(resultadoPagamento)) {
            pedido.setStatus("PENDENTE_PAGAMENTO_FALLBACK");
        } else {
            pedido.setStatus("FALHOU_PAGAMENTO");
            pedidoRepository.save(pedido);
            return ResponseEntity.badRequest().body(pedido);
        }

        String resultadoNotificacao = circuitoNotificacao.executar(
            () -> {
                Map<String, String> reqNotificacao = new HashMap<>();
                reqNotificacao.put("destinatario", "cliente@exemplo.com");
                reqNotificacao.put("mensagem", "Seu pedido #" + pedido.getId() + " mudou de status para: " + pedido.getStatus());
                
                restTemplate.postForEntity(notificacaoUrl, reqNotificacao, Map.class);
                return "ENVIADA";
            },
            (throwable) -> {
                return "NOTIFICACAO_FILA_FALLBACK";
            }
        );

        pedido.setStatusNotificacao(resultadoNotificacao);
        pedidoRepository.save(pedido);

        return ResponseEntity.ok(pedido);
    }
}
