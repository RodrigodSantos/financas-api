package br.com.financas.auth.dto;

import br.com.financas.usuario.Usuario;

/** Nunca inclui a senha nem o hash. */
public record UsuarioResponse(Long id, String nome, String email) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
