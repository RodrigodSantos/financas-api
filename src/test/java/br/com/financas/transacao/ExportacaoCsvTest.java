package br.com.financas.transacao;

import br.com.financas.ApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExportacaoCsvTest extends ApiTest {

    @BeforeEach
    void preparar() throws Exception {
        Long nubank = criarConta("Nubank");
        registrar(nubank, categoriaGlobal("Salário"), "RECEITA", "5000", "2026-10-05", "Salário outubro");
        registrar(nubank, categoriaGlobal("Alimentação"), "DESPESA", "1234.5", "2026-10-01", "Mercado; feira e \\\"extras\\\"");
        registrar(nubank, categoriaGlobal("Moradia"), "DESPESA", "1500", "2026-09-10", "Aluguel setembro");
    }

    @Test
    void deveGerarCsvNoFormatoDoExcelEmPortugues() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/transacoes/exportar")
                        .param("inicio", "2026-10-01").param("fim", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"transacoes-2026-10-01_a_2026-10-31.csv\""))
                .andReturn();

        String csv = new String(resultado.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        String[] linhas = csv.split("\r\n");

        assertThat(csv).startsWith("﻿"); // BOM: faz o Excel reconhecer os acentos
        assertThat(linhas).hasSize(3);       // cabeçalho + 2 transações de outubro (setembro fica de fora)
        assertThat(linhas[0]).isEqualTo("﻿Data;Descrição;Tipo;Categoria;Conta;Valor");
        // Ordem cronológica; despesa negativa; vírgula decimal; campo com ";" e aspas entre aspas
        assertThat(linhas[1]).isEqualTo("01/10/2026;\"Mercado; feira e \"\"extras\"\"\";Despesa;Alimentação;Nubank;-1234,50");
        assertThat(linhas[2]).isEqualTo("05/10/2026;Salário outubro;Receita;Salário;Nubank;5000,00");
    }

    @Test
    void semFiltroExportaTudoComNomePadrao() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/transacoes/exportar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"transacoes.csv\""))
                .andReturn();

        String csv = resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv.split("\r\n")).hasSize(4);
    }

    @Test
    void periodoInvertidoRetorna400() throws Exception {
        mockMvc.perform(get("/api/transacoes/exportar").param("inicio", "2026-10-31").param("fim", "2026-10-01"))
                .andExpect(status().isBadRequest());
    }
}
