package br.com.financas.relatorio;

import br.com.financas.relatorio.dto.RelatorioMensalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/mensal")
    @Operation(summary = "Receitas, despesas e totais por categoria de um mês (sem ano e mês = mês atual)")
    public RelatorioMensalResponse mensal(
            @Parameter(example = "2026") @RequestParam(required = false) Integer ano,
            @Parameter(example = "10") @RequestParam(required = false) Integer mes,
            @Parameter(description = "Opcional: relatório de uma conta só") @RequestParam(required = false) Long contaId) {
        return service.mensal(ano, mes, contaId);
    }
}
