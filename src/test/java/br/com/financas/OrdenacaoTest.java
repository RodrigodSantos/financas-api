package br.com.financas;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O parâmetro "sort" vem do cliente: um campo inexistente (inclusive o "string" que o Swagger
 * preenche como exemplo) precisa responder 400 com mensagem clara, e não 500.
 */
class OrdenacaoTest extends ApiTest {

    @ParameterizedTest
    @ValueSource(strings = {"/api/categorias", "/api/contas", "/api/transacoes"})
    void campoInexistenteRetorna400(String rota) throws Exception {
        mockMvc.perform(get(rota).param("sort", "string"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ordenação inválida"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/categorias", "/api/contas", "/api/transacoes"})
    void direcaoInvalidaRetorna400(String rota) throws Exception {
        // "nome,xyz": como "xyz" não é asc/desc, o Spring tenta ordenar por um campo chamado "xyz"
        mockMvc.perform(get(rota).param("sort", "nome,xyz"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/categorias?sort=nome,desc", "/api/contas?sort=saldoInicial,desc",
            "/api/transacoes?sort=valor,asc", "/api/transacoes?sort=conta.nome"})
    void camposValidosContinuamFuncionando(String rotaComOrdenacao) throws Exception {
        mockMvc.perform(get(rotaComOrdenacao)).andExpect(status().isOk());
    }
}
