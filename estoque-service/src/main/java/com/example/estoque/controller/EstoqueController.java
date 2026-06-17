package com.example.estoque.controller;

import com.example.estoque.model.Produto;
import com.example.estoque.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/{codigoSku}")
    public ResponseEntity<Produto> obterEstoque(@PathVariable String codigoSku) {
        return produtoRepository.findByCodigoSku(codigoSku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reservar")
    public ResponseEntity<String> reservarEstoque(@RequestParam String codigoSku, @RequestParam Integer quantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findByCodigoSku(codigoSku);
        if (produtoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Produto nao encontrado");
        }
        
        Produto produto = produtoOpt.get();
        if (produto.getQuantidade() < quantidade) {
            return ResponseEntity.badRequest().body("Estoque insuficiente");
        }
        
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produtoRepository.save(produto);
        return ResponseEntity.ok("Estoque reservado com sucesso");
    }
}
