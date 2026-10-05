package br.com.financas.transacao;

import br.com.financas.transacao.dto.TransacaoRequest;
import br.com.financas.transacao.dto.TransacaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/transacoes")
@Tag(name = "Transações")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as transações do usuário, das mais recentes para as mais antigas, com filtros opcionais")
    public Page<TransacaoResponse> listar(
            @ParameterObject TransacaoFiltro filtro,
            @ParameterObject @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(filtro, pageable);
    }

    @GetMapping("/exportar")
    @Operation(summary = "Exporta as transações em CSV (formato do Excel em português), com os mesmos filtros da listagem")
    public ResponseEntity<byte[]> exportar(@ParameterObject TransacaoFiltro filtro) {
        byte[] csv = service.exportarCsv(filtro);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nomeDoArquivo(filtro))
                        .build().toString())
                .body(csv);
    }

    private String nomeDoArquivo(TransacaoFiltro filtro) {
        if (filtro.inicio() != null && filtro.fim() != null) {
            return "transacoes-" + filtro.inicio() + "_a_" + filtro.fim() + ".csv";
        }
        if (filtro.inicio() != null) {
            return "transacoes-desde-" + filtro.inicio() + ".csv";
        }
        if (filtro.fim() != null) {
            return "transacoes-ate-" + filtro.fim() + ".csv";
        }
        return "transacoes.csv";
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma transação pelo id")
    public TransacaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Registra uma receita ou despesa")
    public ResponseEntity<TransacaoResponse> criar(@RequestBody @Valid TransacaoRequest request) {
        TransacaoResponse criada = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma transação")
    public TransacaoResponse atualizar(@PathVariable Long id, @RequestBody @Valid TransacaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma transação")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
