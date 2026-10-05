package br.com.financas.transacao;

import br.com.financas.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
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
@Import(TestcontainersConfig.class)
@Transactional
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long contaId;
    private Long salarioId;
    private Long alimentacaoId;

    @BeforeEach
    void preparar() throws Exception {
        contaId = criarConta("Nubank", 1000);
        salarioId = categoriaGlobal("Salário");
        alimentacaoId = categoriaGlobal("Alimentação");
    }

    @Test
    void deveRegistrarReceitaComResumoDaContaEDaCategoria() throws Exception {
        registrar("Salário outubro", "5000.00", "RECEITA", salarioId)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.valor").value(5000.0))
                .andExpect(jsonPath("$.conta.nome").value("Nubank"))
                .andExpect(jsonPath("$.categoria.nome").value("Salário"));
    }

    @Test
    void deveRejeitarTipoDiferenteDoDaCategoria() throws Exception {
        registrar("Mercado", "200.00", "DESPESA", salarioId)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Uma DESPESA não pode usar a categoria 'Salário', que é de RECEITA"));
    }

    @Test
    void deveRetornarErroDeValidacaoPorCampo() throws Exception {
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao": "", "valor": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descricao").exists())
                .andExpect(jsonPath("$.campos.valor").value("deve ser maior que zero"))
                .andExpect(jsonPath("$.campos.tipo").exists())
                .andExpect(jsonPath("$.campos.data").exists())
                .andExpect(jsonPath("$.campos.contaId").exists())
                .andExpect(jsonPath("$.campos.categoriaId").exists());
    }

    @Test
    void naoDeveUsarContaDeOutroUsuario() throws Exception {
        jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash) VALUES (99, 'Outro', 'outro@teste.com', 'x')");
        Long contaDoOutro = jdbcTemplate.queryForObject(
                "INSERT INTO conta (usuario_id, nome, tipo, saldo_inicial) VALUES (99, 'Do outro', 'CORRENTE', 0) RETURNING id",
                Long.class);

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Invasão", "10.00", "RECEITA", contaDoOutro, salarioId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCalcularSaldoAtualDaConta() throws Exception {
        registrar("Salário", "500.00", "RECEITA", salarioId);
        registrar("Mercado", "200.00", "DESPESA", alimentacaoId);
        registrar("Padaria", "50.50", "DESPESA", alimentacaoId);

        // 1000 + 500 - 200 - 50,50
        mockMvc.perform(get("/api/contas/{id}", contaId))
                .andExpect(jsonPath("$.saldoInicial").value(1000.0))
                .andExpect(jsonPath("$.saldoAtual").value(1249.5));

        mockMvc.perform(get("/api/contas"))
                .andExpect(jsonPath("$.content[0].saldoAtual").value(1249.5));
    }

    @Test
    void deveListarDaMaisRecenteParaAMaisAntiga() throws Exception {
        registrar("Antiga", "10.00", "DESPESA", alimentacaoId, "2026-09-01");
        registrar("Recente", "10.00", "DESPESA", alimentacaoId, "2026-10-01");

        mockMvc.perform(get("/api/transacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].descricao").value("Recente"))
                .andExpect(jsonPath("$.content[1].descricao").value("Antiga"));
    }

    @Test
    void deveAtualizarEExcluirTransacao() throws Exception {
        Long id = idDe(registrar("Mercado", "100.00", "DESPESA", alimentacaoId));

        mockMvc.perform(put("/api/transacoes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Mercado do mês", "350.00", "DESPESA", contaId, alimentacaoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Mercado do mês"))
                .andExpect(jsonPath("$.valor").value(350.0));

        mockMvc.perform(delete("/api/transacoes/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/transacoes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void naoDeveExcluirContaComTransacoes() throws Exception {
        registrar("Mercado", "100.00", "DESPESA", alimentacaoId);

        mockMvc.perform(delete("/api/contas/{id}", contaId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("A conta 'Nubank' possui transações e não pode ser excluída"));
    }

    @Test
    void naoDeveExcluirNemMudarTipoDeCategoriaEmUso() throws Exception {
        registrar("Mercado", "100.00", "DESPESA", alimentacaoId);

        mockMvc.perform(delete("/api/categorias/{id}", alimentacaoId))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(put("/api/categorias/{id}", alimentacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Alimentação", "tipo": "RECEITA"}
                                """))
                .andExpect(status().isUnprocessableEntity());

        // Renomear sem mudar o tipo continua permitido
        mockMvc.perform(put("/api/categorias/{id}", alimentacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Mercado e restaurantes", "tipo": "DESPESA"}
                                """))
                .andExpect(status().isOk());
    }

    private ResultActions registrar(String descricao, String valor, String tipo, Long categoriaId) throws Exception {
        return registrar(descricao, valor, tipo, categoriaId, "2026-10-05");
    }

    private ResultActions registrar(String descricao, String valor, String tipo, Long categoriaId, String data) throws Exception {
        return mockMvc.perform(post("/api/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(descricao, valor, tipo, contaId, categoriaId, data)));
    }

    private String json(String descricao, String valor, String tipo, Long contaId, Long categoriaId) {
        return json(descricao, valor, tipo, contaId, categoriaId, "2026-10-05");
    }

    private String json(String descricao, String valor, String tipo, Long contaId, Long categoriaId, String data) {
        return """
                {"descricao": "%s", "valor": %s, "tipo": "%s", "data": "%s", "contaId": %d, "categoriaId": %d}
                """.formatted(descricao, valor, tipo, data, contaId, categoriaId);
    }

    private Long criarConta(String nome, int saldoInicial) throws Exception {
        return idDe(mockMvc.perform(post("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "%s", "tipo": "CORRENTE", "saldoInicial": %d}
                        """.formatted(nome, saldoInicial))));
    }

    private Long categoriaGlobal(String nome) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM categoria WHERE nome = ? AND usuario_id IS NULL", Long.class, nome);
    }

    private Long idDe(ResultActions resultado) throws Exception {
        Number id = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
