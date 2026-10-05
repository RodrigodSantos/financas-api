package br.com.financas.transacao.dto;

import br.com.financas.categoria.TipoCategoria;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(
        @NotBlank(message = "é obrigatória")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String descricao,

        @NotNull(message = "é obrigatório")
        @Positive(message = "deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "deve ter no máximo 13 dígitos inteiros e 2 decimais")
        BigDecimal valor,

        @NotNull(message = "é obrigatório (RECEITA ou DESPESA)")
        TipoCategoria tipo,

        @NotNull(message = "é obrigatória")
        LocalDate data,

        @NotNull(message = "é obrigatória")
        Long contaId,

        @NotNull(message = "é obrigatória")
        Long categoriaId
) {
}
