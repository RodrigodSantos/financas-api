package br.com.financas.transacao;

import br.com.financas.categoria.TipoCategoria;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Gera o CSV no formato que o Excel em português abre direto:
 * separador ";", vírgula decimal, data dd/MM/yyyy e UTF-8 com BOM (para os acentos).
 */
final class TransacaoCsv {

    private static final String BOM = "﻿";
    private static final String SEPARADOR = ";";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TransacaoCsv() {
    }

    static byte[] gerar(List<Transacao> transacoes) {
        StringBuilder csv = new StringBuilder(BOM)
                .append(String.join(SEPARADOR, "Data", "Descrição", "Tipo", "Categoria", "Conta", "Valor"))
                .append("\r\n");

        for (Transacao t : transacoes) {
            csv.append(String.join(SEPARADOR,
                            t.getData().format(DATA),
                            campo(t.getDescricao()),
                            t.getTipo() == TipoCategoria.RECEITA ? "Receita" : "Despesa",
                            campo(t.getCategoria().getNome()),
                            campo(t.getConta().getNome()),
                            valorComSinal(t)))
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Despesas saem negativas, para a coluna poder ser somada direto na planilha. */
    private static String valorComSinal(Transacao t) {
        BigDecimal valor = t.getTipo() == TipoCategoria.DESPESA ? t.getValor().negate() : t.getValor();
        return String.format(PT_BR, "%.2f", valor);
    }

    /** Coloca entre aspas quando o texto tem separador, aspas ou quebra de linha (RFC 4180). */
    private static String campo(String texto) {
        if (texto.contains(SEPARADOR) || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}
