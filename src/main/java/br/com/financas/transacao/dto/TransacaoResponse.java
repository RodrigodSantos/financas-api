package br.com.financas.transacao.dto;

import br.com.financas.categoria.TipoCategoria;
import br.com.financas.transacao.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoCategoria tipo,
        LocalDate data,
        Resumo conta,
        Resumo categoria
) {

    /** Só o necessário para exibir: evita devolver a conta e a categoria inteiras. */
    public record Resumo(Long id, String nome) {
    }

    public static TransacaoResponse de(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getTipo(),
                transacao.getData(),
                new Resumo(transacao.getConta().getId(), transacao.getConta().getNome()),
                new Resumo(transacao.getCategoria().getId(), transacao.getCategoria().getNome())
        );
    }
}
