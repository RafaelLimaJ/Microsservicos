package com.example.pedido.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigoSku;
    private Integer quantidade;
    private BigDecimal preco;
    private String status;
    private String statusPagamento;
    private String statusNotificacao;
    private LocalDateTime dataCriacao;

    public Pedido() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Pedido(String codigoSku, Integer quantidade, BigDecimal preco, String status) {
        this();
        this.codigoSku = codigoSku;
        this.quantidade = quantidade;
        this.preco = preco;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoSku() { return codigoSku; }
    public void setCodigoSku(String codigoSku) { this.codigoSku = codigoSku; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }

    public String getStatusNotificacao() { return statusNotificacao; }
    public void setStatusNotificacao(String statusNotificacao) { this.statusNotificacao = statusNotificacao; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
