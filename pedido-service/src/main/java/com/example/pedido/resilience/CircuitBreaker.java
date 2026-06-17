package com.example.pedido.resilience;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.Function;

public class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final String nome;
    private final int limiteFalhas;
    private final long tempoEsperaMs;
    
    private final AtomicReference<State> estado = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger contadorFalhas = new AtomicInteger(0);
    private final AtomicInteger contadorSucessos = new AtomicInteger(0);
    private volatile long tempoUltimaTransicao = System.currentTimeMillis();

    public CircuitBreaker(String nome, int limiteFalhas, long tempoEsperaMs) {
        this.nome = nome;
        this.limiteFalhas = limiteFalhas;
        this.tempoEsperaMs = tempoEsperaMs;
    }

    public <T> T executar(Supplier<T> fornecedor, Function<Throwable, T> fallback) {
        verificarTransicaoEstado();

        if (estado.get() == State.OPEN) {
            return fallback.apply(new RuntimeException("Circuit Breaker esta ABERTO para " + nome));
        }

        try {
            T resultado = fornecedor.get();
            aoObterSucesso();
            return resultado;
        } catch (Throwable t) {
            aoFalhar();
            return fallback.apply(t);
        }
    }

    private void verificarTransicaoEstado() {
        if (estado.get() == State.OPEN) {
            long decorrido = System.currentTimeMillis() - tempoUltimaTransicao;
            if (decorrido > tempoEsperaMs) {
                estado.set(State.HALF_OPEN);
                contadorSucessos.set(0);
                contadorFalhas.set(0);
                System.out.println("Circuit Breaker " + nome + " mudou para MEIO-ABERTO (HALF_OPEN)");
            }
        }
    }

    private void aoObterSucesso() {
        if (estado.get() == State.HALF_OPEN) {
            int sucessosAtuais = contadorSucessos.incrementAndGet();
            if (sucessosAtuais >= 2) {
                estado.set(State.CLOSED);
                contadorFalhas.set(0);
                System.out.println("Circuit Breaker " + nome + " mudou para FECHADO (CLOSED)");
            }
        } else if (estado.get() == State.CLOSED) {
            contadorFalhas.set(0);
        }
    }

    private void aoFalhar() {
        if (estado.get() == State.HALF_OPEN) {
            transitarParaAberto();
        } else if (estado.get() == State.CLOSED) {
            int falhas = contadorFalhas.incrementAndGet();
            if (falhas >= limiteFalhas) {
                transitarParaAberto();
            }
        }
    }

    private void transitarParaAberto() {
        estado.set(State.OPEN);
        tempoUltimaTransicao = System.currentTimeMillis();
        System.out.println("Circuit Breaker " + nome + " mudou para ABERTO (OPEN)");
    }

    public State getEstado() {
        return estado.get();
    }
}
