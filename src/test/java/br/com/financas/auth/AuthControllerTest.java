package br.com.financas.auth;

import br.com.financas.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo real de ponta a ponta: cadastro → login → token no header. Sem atalhos de autenticação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarSemDevolverASenha() throws Exception {
        cadastrar("Ana", "Ana@Email.com", "senha-forte")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRejeitarEmailRepetidoIgnorandoMaiusculas() throws Exception {
        cadastrar("Ana", "ana@email.com", "senha-forte").andExpect(status().isCreated());

        cadastrar("Outra Ana", "ANA@email.com", "outra-senha")
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveValidarCamposDoCadastro() throws Exception {
        cadastrar("", "nao-e-email", "curta")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").value("deve ser um e-mail válido"))
                .andExpect(jsonPath("$.campos.senha").value("deve ter entre 8 e 72 caracteres"));
    }

    @Test
    void deveFazerLoginEUsarOToken() throws Exception {
        cadastrar("Ana", "ana@email.com", "senha-forte");

        String token = login("ana@email.com", "senha-forte")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm").exists())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(token, "$.token");

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    void deveRecusarSenhaErradaSemRevelarSeOEmailExiste() throws Exception {
        cadastrar("Ana", "ana@email.com", "senha-forte");

        login("ana@email.com", "senha-errada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));

        login("ninguem@email.com", "qualquer-coisa")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    void deveExigirTokenNasRotasProtegidas() throws Exception {
        mockMvc.perform(get("/api/contas"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Não autenticado"));

        mockMvc.perform(get("/api/contas").header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveFazerLoginComOUsuarioDemo() throws Exception {
        login("demo@financas.local", "demo1234").andExpect(status().isOk());
    }

    @Test
    void usuariosNaoDevemVerOsDadosUmDoOutro() throws Exception {
        cadastrar("Ana", "ana@email.com", "senha-forte");
        cadastrar("Bruno", "bruno@email.com", "senha-forte");
        String tokenAna = tokenDe("ana@email.com");
        String tokenBruno = tokenDe("bruno@email.com");

        String contaDaAna = mockMvc.perform(post("/api/contas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Conta da Ana", "tipo": "CORRENTE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number idDaAna = JsonPath.read(contaDaAna, "$.id");

        mockMvc.perform(get("/api/contas").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenBruno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/contas/{id}", idDaAna).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenBruno))
                .andExpect(status().isNotFound());
    }

    private ResultActions cadastrar(String nome, String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "%s", "email": "%s", "senha": "%s"}
                        """.formatted(nome, email, senha)));
    }

    private ResultActions login(String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "senha": "%s"}
                        """.formatted(email, senha)));
    }

    private String tokenDe(String email) throws Exception {
        String corpo = login(email, "senha-forte").andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }
}
