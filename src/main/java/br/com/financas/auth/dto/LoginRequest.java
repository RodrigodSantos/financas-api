package br.com.financas.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "é obrigatório")
        String email,

        @NotBlank(message = "é obrigatória")
        String senha
) {
}
