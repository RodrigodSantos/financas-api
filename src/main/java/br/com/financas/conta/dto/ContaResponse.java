package br.com.financas.conta.dto;

import br.com.financas.conta.Conta;
import br.com.financas.conta.TipoConta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContaResponse(Long id, String nome, TipoConta tipo, BigDecimal saldoInicial, LocalDateTime criadoEm) {

    public static ContaResponse de(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNome(),
                conta.getTipo(),
                conta.getSaldoInicial(),
                conta.getCriadoEm()
        );
    }
}
