package br.com.financas.relatorio.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * @param periodo    mês do relatório, no formato yyyy-MM
 * @param saldoDoMes receitas − despesas do mês (não é o saldo das contas)
 * @param contas     contas que entraram no relatório (todas com movimentação no mês, ou só a filtrada)
 */
public record RelatorioMensalResponse(
        String periodo,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldoDoMes,
        List<ResumoConta> contas,
        List<Item> despesasPorCategoria,
        List<Item> receitasPorCategoria
) {

    public record ResumoConta(Long id, String nome, BigDecimal receitas, BigDecimal despesas, BigDecimal saldoDoMes) {
    }

    /**
     * @param percentual participação no total do mesmo tipo (ex.: % das despesas do mês)
     * @param contas     de quais contas saiu (ou entrou) o valor da categoria
     */
    public record Item(Long categoriaId, String categoria, BigDecimal total, BigDecimal percentual,
                       List<ValorPorConta> contas) {
    }

    public record ValorPorConta(Long id, String nome, BigDecimal total) {
    }
}
