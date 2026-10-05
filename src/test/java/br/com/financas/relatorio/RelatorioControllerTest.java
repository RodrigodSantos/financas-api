package br.com.financas.relatorio;

import br.com.financas.ApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RelatorioControllerTest extends ApiTest {

    private Long nubank;
    private Long carteira;
    private Long salario;
    private Long moradia;
    private Long alimentacao;

    /*
     * Outubro/2026:
     *  Receitas: Salário 5000 + Freelance 800 = 5800
     *  Despesas: Moradia 1500 + Alimentação (300 + 80) = 1880
     *  Saldo do mês: 3920
     * Setembro tem uma despesa que NÃO pode entrar no relatório de outubro.
     */
    @BeforeEach
    void preparar() throws Exception {
        nubank = criarConta("Nubank");
        carteira = criarConta("Carteira");
        salario = categoriaGlobal("Salário");
        moradia = categoriaGlobal("Moradia");
        alimentacao = categoriaGlobal("Alimentação");
        Long freelance = categoriaGlobal("Freelance");

        registrar(nubank, salario, "RECEITA", "5000", "2026-10-05", "Salário");
        registrar(nubank, freelance, "RECEITA", "800", "2026-10-20", "Freela");
        registrar(nubank, moradia, "DESPESA", "1500", "2026-10-10", "Aluguel");
        registrar(nubank, alimentacao, "DESPESA", "300", "2026-10-01", "Mercado");
        registrar(carteira, alimentacao, "DESPESA", "80", "2026-10-31", "Feira");
        registrar(nubank, moradia, "DESPESA", "999", "2026-09-30", "Mês anterior");
    }

    @Test
    void deveSomarReceitasDespesasESaldoDoMes() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026").param("mes", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("2026-10"))
                .andExpect(jsonPath("$.totalReceitas").value(5800.0))
                .andExpect(jsonPath("$.totalDespesas").value(1880.0))
                .andExpect(jsonPath("$.saldoDoMes").value(3920.0));
    }

    @Test
    void deveAgruparPorCategoriaDoMaiorParaOMenorComPercentual() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026").param("mes", "10"))
                // Despesas: 1500 / 1880 = 79,79% e 380 / 1880 = 20,21%
                .andExpect(jsonPath("$.despesasPorCategoria.length()").value(2))
                .andExpect(jsonPath("$.despesasPorCategoria[0].categoria").value("Moradia"))
                .andExpect(jsonPath("$.despesasPorCategoria[0].total").value(1500.0))
                .andExpect(jsonPath("$.despesasPorCategoria[0].percentual").value(79.79))
                .andExpect(jsonPath("$.despesasPorCategoria[1].categoria").value("Alimentação"))
                .andExpect(jsonPath("$.despesasPorCategoria[1].total").value(380.0))
                .andExpect(jsonPath("$.despesasPorCategoria[1].percentual").value(20.21))
                // Receitas: 5000 / 5800 = 86,21% e 800 / 5800 = 13,79%
                .andExpect(jsonPath("$.receitasPorCategoria[0].categoria").value("Salário"))
                .andExpect(jsonPath("$.receitasPorCategoria[0].percentual").value(86.21))
                .andExpect(jsonPath("$.receitasPorCategoria[1].percentual").value(13.79));
    }

    @Test
    void semFiltroDeContaMostraOResumoDeCadaConta() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026").param("mes", "10"))
                // Em ordem alfabética: Carteira (só a feira de 80) e Nubank (5800 de receita, 1500 + 300 de despesa)
                .andExpect(jsonPath("$.contas.length()").value(2))
                .andExpect(jsonPath("$.contas[0].id").value(carteira))
                .andExpect(jsonPath("$.contas[0].nome").value("Carteira"))
                .andExpect(jsonPath("$.contas[0].receitas").value(0))
                .andExpect(jsonPath("$.contas[0].despesas").value(80.0))
                .andExpect(jsonPath("$.contas[0].saldoDoMes").value(-80.0))
                .andExpect(jsonPath("$.contas[1].nome").value("Nubank"))
                .andExpect(jsonPath("$.contas[1].receitas").value(5800.0))
                .andExpect(jsonPath("$.contas[1].despesas").value(1800.0))
                .andExpect(jsonPath("$.contas[1].saldoDoMes").value(4000.0));
    }

    @Test
    void cadaCategoriaMostraDeQualContaSaiuOValor() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026").param("mes", "10"))
                // Alimentação = 300 no Nubank + 80 na Carteira, do maior para o menor
                .andExpect(jsonPath("$.despesasPorCategoria[1].categoria").value("Alimentação"))
                .andExpect(jsonPath("$.despesasPorCategoria[1].contas.length()").value(2))
                .andExpect(jsonPath("$.despesasPorCategoria[1].contas[0].nome").value("Nubank"))
                .andExpect(jsonPath("$.despesasPorCategoria[1].contas[0].total").value(300.0))
                .andExpect(jsonPath("$.despesasPorCategoria[1].contas[1].nome").value("Carteira"))
                .andExpect(jsonPath("$.despesasPorCategoria[1].contas[1].total").value(80.0))
                // Moradia só teve lançamento no Nubank
                .andExpect(jsonPath("$.despesasPorCategoria[0].contas.length()").value(1))
                .andExpect(jsonPath("$.despesasPorCategoria[0].contas[0].nome").value("Nubank"));
    }

    @Test
    void deveFiltrarPorConta() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal")
                        .param("ano", "2026").param("mes", "10").param("contaId", carteira.toString()))
                .andExpect(jsonPath("$.contas.length()").value(1))
                .andExpect(jsonPath("$.contas[0].id").value(carteira))
                .andExpect(jsonPath("$.contas[0].nome").value("Carteira"))
                .andExpect(jsonPath("$.totalReceitas").value(0))
                .andExpect(jsonPath("$.totalDespesas").value(80.0))
                .andExpect(jsonPath("$.despesasPorCategoria[0].percentual").value(100.0))
                .andExpect(jsonPath("$.despesasPorCategoria[0].contas[0].nome").value("Carteira"));
    }

    @Test
    void mesSemTransacoesVemZerado() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026").param("mes", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(0))
                .andExpect(jsonPath("$.totalDespesas").value(0))
                .andExpect(jsonPath("$.saldoDoMes").value(0))
                .andExpect(jsonPath("$.contas.length()").value(0))
                .andExpect(jsonPath("$.despesasPorCategoria.length()").value(0));
    }

    @Test
    void semAnoEMesUsaOMesAtual() throws Exception {
        registrar(nubank, salario, "RECEITA", "123.45", LocalDate.now().toString(), "Hoje");

        mockMvc.perform(get("/api/relatorios/mensal"))
                .andExpect(jsonPath("$.periodo").value(YearMonth.now().toString()))
                .andExpect(jsonPath("$.receitasPorCategoria[?(@.categoria == 'Salário')]").exists());
    }

    @Test
    void parametrosInvalidos() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026").param("mes", "13"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/relatorios/mensal").param("ano", "2026").param("mes", "10").param("contaId", "999999"))
                .andExpect(status().isNotFound());
    }
}
