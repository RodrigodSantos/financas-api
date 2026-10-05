package br.com.financas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 120, message = "deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "é obrigatório")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String email,

        @NotBlank(message = "é obrigatória")
        @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
        String senha
) {
}
