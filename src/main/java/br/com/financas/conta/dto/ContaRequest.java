package br.com.financas.conta.dto;

import br.com.financas.conta.TipoConta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ContaRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 80, message = "deve ter no máximo 80 caracteres")
        String nome,

        @NotNull(message = "é obrigatório (CORRENTE, POUPANCA, CARTEIRA ou INVESTIMENTO)")
        TipoConta tipo,

        // Opcional: quando não informado, a conta começa com saldo zero
        @PositiveOrZero(message = "não pode ser negativo")
        @Digits(integer = 13, fraction = 2, message = "deve ter no máximo 13 dígitos inteiros e 2 decimais")
        BigDecimal saldoInicial
) {

    public BigDecimal saldoInicialOuZero() {
        return saldoInicial == null ? BigDecimal.ZERO : saldoInicial;
    }
}
