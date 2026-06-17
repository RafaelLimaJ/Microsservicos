package com.example.estoque;

import com.example.estoque.model.Produto;
import com.example.estoque.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EstoqueApplication {
    public static void main(String[] args) {
        SpringApplication.run(EstoqueApplication.class, args);
    }

    @Bean
    public CommandLineRunner carregarDados(ProdutoRepository repository) {
        return args -> {
            repository.save(new Produto("ITEM-001", "Notebook", 10));
            repository.save(new Produto("ITEM-002", "Smartphone", 5));
            repository.save(new Produto("ITEM-003", "Fone de Ouvido", 0));
        };
    }
}
