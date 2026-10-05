package br.com.financas.relatorio;

import br.com.financas.categoria.TipoCategoria;

import java.math.BigDecimal;

/** Linha do GROUP BY: quanto entrou ou saiu em cada categoria, em cada conta, no período. */
public record TotalPorCategoria(Long categoriaId, String categoria, TipoCategoria tipo,
                                Long contaId, String conta, BigDecimal total) {
}
