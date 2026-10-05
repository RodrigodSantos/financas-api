package br.com.financas;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base dos testes de integração autenticados como o usuário demo, com atalhos para montar os dados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfig.class, AutenticadoComoDemo.class})
@Transactional
public abstract class ApiTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected Long criarConta(String nome) throws Exception {
        return idDe(mockMvc.perform(post("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "%s", "tipo": "CORRENTE"}
                        """.formatted(nome))));
    }

    protected Long registrar(Long contaId, Long categoriaId, String tipo, String valor, String data, String descricao)
            throws Exception {
        return idDe(mockMvc.perform(post("/api/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"descricao": "%s", "valor": %s, "tipo": "%s", "data": "%s", "contaId": %d, "categoriaId": %d}
                        """.formatted(descricao, valor, tipo, data, contaId, categoriaId))));
    }

    protected Long categoriaGlobal(String nome) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM categoria WHERE nome = ? AND usuario_id IS NULL", Long.class, nome);
    }

    protected Long idDe(ResultActions resultado) throws Exception {
        Number id = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
