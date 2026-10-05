package br.com.financas.shared.usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Ponto único que informa quem é o usuário da requisição: lê o id do "subject" do token JWT.
 * <p>
 * Os services só conhecem esta classe, então não sabem (nem precisam saber) que a identificação vem de um JWT.
 */
@Component
public class UsuarioLogado {

    public Long getId() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Nenhum usuário autenticado na requisição");
        }
        return Long.valueOf(jwt.getSubject());
    }
}
