package br.com.financas.transacao;

import br.com.financas.ApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransacaoFiltroTest extends ApiTest {

    private Long nubank;
    private Long carteira;
    private Long alimentacao;

    /*
     * Massa de dados:
     *  15/09  Nubank    Salário      RECEITA  5000  "Salário setembro"
     *  20/09  Carteira  Alimentação  DESPESA    40  "Padaria"
     *  05/10  Nubank    Alimentação  DESPESA   300  "Mercado do mês"
     *  10/10  Carteira  Alimentação  DESPESA    60  "Mercadinho"
     *  15/10  Nubank    Salário      RECEITA  5000  "Salário outubro"
     */
    @BeforeEach
    void preparar() throws Exception {
        nubank = criarConta("Nubank");
        carteira = criarConta("Carteira");
        Long salario = categoriaGlobal("Salário");
        alimentacao = categoriaGlobal("Alimentação");

        registrar(nubank, salario, "RECEITA", "5000", "2026-09-15", "Salário setembro");
        registrar(carteira, alimentacao, "DESPESA", "40", "2026-09-20", "Padaria");
        registrar(nubank, alimentacao, "DESPESA", "300", "2026-10-05", "Mercado do mês");
        registrar(carteira, alimentacao, "DESPESA", "60", "2026-10-10", "Mercadinho");
        registrar(nubank, salario, "RECEITA", "5000", "2026-10-15", "Salário outubro");
    }

    @Test
    void semFiltrosTrazTudoDaMaisRecenteParaAMaisAntiga() throws Exception {
        mockMvc.perform(get("/api/transacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content[0].descricao").value("Salário outubro"))
                .andExpect(jsonPath("$.content[4].descricao").value("Salário setembro"));
    }

    @Test
    void filtraPorPeriodoIncluindoAsDuasPontas() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("inicio", "2026-10-05").param("fim", "2026-10-15"))
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/transacoes").param("fim", "2026-09-30"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void filtraPorContaETipoCombinados() throws Exception {
        mockMvc.perform(get("/api/transacoes")
                        .param("contaId", nubank.toString())
                        .param("tipo", "DESPESA"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].descricao").value("Mercado do mês"));
    }

    @Test
    void filtraPorCategoriaEPeriodo() throws Exception {
        mockMvc.perform(get("/api/transacoes")
                        .param("categoriaId", alimentacao.toString())
                        .param("inicio", "2026-10-01")
                        .param("fim", "2026-10-31"))
                .andExpect(jsonPath("$.content[*].descricao").value(containsInAnyOrder("Mercado do mês", "Mercadinho")));
    }

    @Test
    void buscaPorTrechoDaDescricaoSemDiferenciarMaiusculas() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("descricao", "MERC"))
                .andExpect(jsonPath("$.content[*].descricao").value(containsInAnyOrder("Mercado do mês", "Mercadinho")));
    }

    @Test
    void curingaDoLikeDigitadoPeloUsuarioNaoTrazTudo() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("descricao", "%"))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void periodoInvertidoRetorna400() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("inicio", "2026-10-31").param("fim", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
    }

    @Test
    void contaDeOutroUsuarioNoFiltroNaoTrazNada() throws Exception {
        jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash) VALUES (99, 'Outro', 'outro@teste.com', 'x')");
        Long contaDoOutro = jdbcTemplate.queryForObject(
                "INSERT INTO conta (usuario_id, nome, tipo, saldo_inicial) VALUES (99, 'Do outro', 'CORRENTE', 0) RETURNING id",
                Long.class);

        mockMvc.perform(get("/api/transacoes").param("contaId", contaDoOutro.toString()))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
