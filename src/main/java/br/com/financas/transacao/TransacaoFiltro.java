package br.com.financas.transacao;

import br.com.financas.categoria.TipoCategoria;
import br.com.financas.shared.exception.RequisicaoInvalidaException;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Filtros opcionais da listagem e da exportação de transações. Todos podem ser combinados.
 */
public record TransacaoFiltro(
        @Parameter(description = "Data inicial (inclusive), formato yyyy-MM-dd")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate inicio,

        @Parameter(description = "Data final (inclusive), formato yyyy-MM-dd")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fim,

        Long contaId,

        Long categoriaId,

        TipoCategoria tipo,

        @Parameter(description = "Trecho da descrição, sem diferenciar maiúsculas")
        String descricao
) {

    public void validar() {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RequisicaoInvalidaException("A data de início (" + inicio + ") não pode ser depois da data de fim (" + fim + ")");
        }
    }
}
