package br.com.financas.conta;

import br.com.financas.AutenticadoComoDemo;
import br.com.financas.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfig.class, AutenticadoComoDemo.class})
@Transactional
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCriarContaComSaldoInicial() throws Exception {
        criarConta("""
                {"nome": "Nubank", "tipo": "CORRENTE", "saldoInicial": 1500.50}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Nubank"))
                .andExpect(jsonPath("$.saldoInicial").value(1500.50))
                .andExpect(jsonPath("$.criadoEm").exists());
    }

    @Test
    void deveAssumirSaldoZeroQuandoNaoInformado() throws Exception {
        criarConta("""
                {"nome": "Carteira", "tipo": "CARTEIRA"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoInicial").value(0));
    }

    @Test
    void deveRejeitarNomeDuplicadoIgnorandoMaiusculas() throws Exception {
        criarConta("""
                {"nome": "Itaú", "tipo": "CORRENTE"}
                """).andExpect(status().isCreated());

        criarConta("""
                {"nome": "ITAÚ", "tipo": "POUPANCA"}
                """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void deveRetornarErroDeValidacaoPorCampo() throws Exception {
        criarConta("""
                {"nome": "", "saldoInicial": -10}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.tipo").exists())
                .andExpect(jsonPath("$.campos.saldoInicial").value("não pode ser negativo"));
    }

    @Test
    void deveFiltrarPorTipo() throws Exception {
        criarConta("""
                {"nome": "Banco A", "tipo": "CORRENTE"}
                """);
        criarConta("""
                {"nome": "Tesouro", "tipo": "INVESTIMENTO"}
                """);

        mockMvc.perform(get("/api/contas").param("tipo", "INVESTIMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Tesouro"));
    }

    @Test
    void deveAtualizarConta() throws Exception {
        Long id = idDe(criarConta("""
                {"nome": "Poupança", "tipo": "POUPANCA", "saldoInicial": 100}
                """));

        mockMvc.perform(put("/api/contas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Poupança Caixa", "tipo": "POUPANCA", "saldoInicial": 250}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Poupança Caixa"))
                .andExpect(jsonPath("$.saldoInicial").value(250));
    }

    @Test
    void deveExcluirConta() throws Exception {
        Long id = idDe(criarConta("""
                {"nome": "Temporária", "tipo": "CARTEIRA"}
                """));

        mockMvc.perform(delete("/api/contas/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/contas/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void naoDeveExporContaDeOutroUsuario() throws Exception {
        jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash) VALUES (99, 'Outro', 'outro@teste.com', 'x')");
        Long idDoOutro = jdbcTemplate.queryForObject(
                "INSERT INTO conta (usuario_id, nome, tipo, saldo_inicial) VALUES (99, 'Conta do outro', 'CORRENTE', 0) RETURNING id",
                Long.class);

        mockMvc.perform(get("/api/contas/{id}", idDoOutro)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/contas/{id}", idDoOutro)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    private ResultActions criarConta(String json) throws Exception {
        return mockMvc.perform(post("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private Long idDe(ResultActions resultado) throws Exception {
        String corpo = resultado.andReturn().getResponse().getContentAsString();
        return Long.valueOf(corpo.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }
}
