package br.com.financas.shared.usuario;

import org.springframework.stereotype.Component;

/**
 * Ponto único que informa quem é o usuário da requisição.
 * <p>
 * Enquanto a autenticação não existe, devolve sempre o usuário de demonstração (migration V3).
 * Quando o JWT entrar, só esta classe muda: passa a ler o id do token.
 */
@Component
public class UsuarioLogado {

    public static final Long USUARIO_DEMO_ID = 1L;

    public Long getId() {
        return USUARIO_DEMO_ID;
    }
}
