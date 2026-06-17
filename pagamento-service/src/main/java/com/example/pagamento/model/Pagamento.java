package com.example.pagamento.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pagamentos")
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String pedidoId;
    private BigDecimal valor;
    private String status;

    public Pagamento() {}

    public Pagamento(String pedidoId, BigDecimal valor, String status) {
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
