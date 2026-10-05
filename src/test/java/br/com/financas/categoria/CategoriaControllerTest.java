package br.com.financas.categoria;

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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCriarCategoriaPropriaQueAparecePertoDasGlobais() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Assinaturas", "tipo": "DESPESA"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.global").value(false));

        mockMvc.perform(get("/api/categorias").param("tipo", "DESPESA").param("size", "50"))
                .andExpect(jsonPath("$.content[*].nome").value(hasItem("Assinaturas")))
                .andExpect(jsonPath("$.content[*].nome").value(hasItem("Moradia")));
    }

    @Test
    void naoDeveAlterarNemExcluirCategoriaGlobal() throws Exception {
        Long moradia = jdbcTemplate.queryForObject(
                "SELECT id FROM categoria WHERE nome = 'Moradia' AND usuario_id IS NULL", Long.class);

        mockMvc.perform(put("/api/categorias/{id}", moradia)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Casa", "tipo": "DESPESA"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));

        mockMvc.perform(delete("/api/categorias/{id}", moradia))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDeveExporCategoriaDeOutroUsuario() throws Exception {
        jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash) VALUES (99, 'Outro', 'outro@teste.com', 'x')");
        Long daOutraPessoa = jdbcTemplate.queryForObject(
                "INSERT INTO categoria (usuario_id, nome, tipo) VALUES (99, 'Segredo', 'DESPESA') RETURNING id", Long.class);

        mockMvc.perform(get("/api/categorias/{id}", daOutraPessoa)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/categorias/{id}", daOutraPessoa)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/categorias").param("size", "50"))
                .andExpect(jsonPath("$.content[*].nome").value(not(hasItem("Segredo"))));

        // A outra pessoa ter "Segredo" não impede o usuário de criar uma com o mesmo nome
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Segredo", "tipo": "DESPESA"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void deveListarCategoriasPadraoFiltrandoPorTipo() throws Exception {
        mockMvc.perform(get("/api/categorias").param("tipo", "RECEITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.content[0].tipo").value("RECEITA"))
                .andExpect(jsonPath("$.content[0].global").value(true));
    }

    @Test
    void deveCriarCategoria() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Pets", "tipo": "DESPESA"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Pets"));
    }

    @Test
    void deveRejeitarCategoriaDuplicada() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "salário", "tipo": "RECEITA"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void deveRetornarErroDeValidacaoPorCampo() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.tipo").exists());
    }

    @Test
    void deveRetornar404ParaCategoriaInexistente() throws Exception {
        mockMvc.perform(get("/api/categorias/{id}", 999_999))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/categorias/{id}", 999_999))
                .andExpect(status().isNotFound());
    }
}
