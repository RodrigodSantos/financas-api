package br.com.financas.conta.dto;

import br.com.financas.conta.Conta;
import br.com.financas.conta.TipoConta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @param saldoAtual saldo inicial + receitas − despesas
 */
public record ContaResponse(Long id, String nome, TipoConta tipo, BigDecimal saldoInicial, BigDecimal saldoAtual,
                            LocalDateTime criadoEm) {

    public static ContaResponse de(Conta conta, BigDecimal movimentacao) {
        return new ContaResponse(
                conta.getId(),
                conta.getNome(),
                conta.getTipo(),
                conta.getSaldoInicial(),
                conta.getSaldoInicial().add(movimentacao),
                conta.getCriadoEm()
        );
    }
}
