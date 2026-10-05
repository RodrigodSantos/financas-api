package br.com.financas.transacao;

import java.math.BigDecimal;

/**
 * Soma das transações de uma conta: receitas menos despesas.
 */
public record MovimentacaoPorConta(Long contaId, BigDecimal total) {
}
