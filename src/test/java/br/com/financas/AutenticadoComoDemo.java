package br.com.financas;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Faz toda requisição do MockMvc chegar autenticada como o usuário demo (id 1).
 * <p>
 * Os testes de regra de negócio focam na regra; o fluxo real de token (login, 401, token inválido)
 * é testado de ponta a ponta no AuthControllerTest.
 */
@TestConfiguration(proxyBeanMethods = false)
public class AutenticadoComoDemo {

    @Bean
    MockMvcBuilderCustomizer autenticarComoDemo() {
        return builder -> builder.defaultRequest(get("/").with(jwt().jwt(token -> token.subject("1"))));
    }
}
