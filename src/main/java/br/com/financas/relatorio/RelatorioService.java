package br.com.financas.relatorio;

import br.com.financas.categoria.TipoCategoria;
import br.com.financas.conta.ContaRepository;
import br.com.financas.relatorio.dto.RelatorioMensalResponse;
import br.com.financas.relatorio.dto.RelatorioMensalResponse.Item;
import br.com.financas.relatorio.dto.RelatorioMensalResponse.ResumoConta;
import br.com.financas.relatorio.dto.RelatorioMensalResponse.ValorPorConta;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RequisicaoInvalidaException;
import br.com.financas.shared.usuario.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final RelatorioRepository repository;
    private final ContaRepository contaRepository;
    private final UsuarioLogado usuarioLogado;
    private final Clock clock;

    public RelatorioService(RelatorioRepository repository, ContaRepository contaRepository,
                            UsuarioLogado usuarioLogado, Clock clock) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.usuarioLogado = usuarioLogado;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public RelatorioMensalResponse mensal(Integer ano, Integer mes, Long contaId) {
        YearMonth periodo = periodo(ano, mes);
        Long usuarioId = usuarioLogado.getId();

        if (contaId != null && contaRepository.findByIdAndUsuarioId(contaId, usuarioId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Conta", contaId);
        }

        List<TotalPorCategoria> linhas = repository.totaisPorCategoria(
                usuarioId, periodo.atDay(1), periodo.atEndOfMonth(), contaId);

        BigDecimal receitas = somar(linhas, l -> l.tipo() == TipoCategoria.RECEITA);
        BigDecimal despesas = somar(linhas, l -> l.tipo() == TipoCategoria.DESPESA);

        return new RelatorioMensalResponse(
                periodo.toString(),
                receitas,
                despesas,
                receitas.subtract(despesas),
                resumoPorConta(linhas),
                itens(linhas, TipoCategoria.DESPESA, despesas),
                itens(linhas, TipoCategoria.RECEITA, receitas));
    }

    /** Sem ano e mês, usa o mês atual. Informar só um dos dois é erro. */
    private YearMonth periodo(Integer ano, Integer mes) {
        if (ano == null && mes == null) {
            return YearMonth.now(clock);
        }
        if (ano == null || mes == null) {
            throw new RequisicaoInvalidaException("Informe ano e mês juntos, ou nenhum dos dois para o mês atual");
        }
        try {
            return YearMonth.of(ano, mes);
        } catch (DateTimeException e) {
            throw new RequisicaoInvalidaException("Mês inválido: " + mes + " (use de 1 a 12)");
        }
    }

    /** Receitas, despesas e saldo do mês de cada conta, em ordem alfabética. */
    private List<ResumoConta> resumoPorConta(List<TotalPorCategoria> linhas) {
        Map<Long, List<TotalPorCategoria>> porConta = linhas.stream()
                .collect(Collectors.groupingBy(TotalPorCategoria::contaId, LinkedHashMap::new, Collectors.toList()));

        return porConta.values().stream()
                .map(daConta -> {
                    BigDecimal receitas = somar(daConta, l -> l.tipo() == TipoCategoria.RECEITA);
                    BigDecimal despesas = somar(daConta, l -> l.tipo() == TipoCategoria.DESPESA);
                    TotalPorCategoria primeira = daConta.get(0);
                    return new ResumoConta(primeira.contaId(), primeira.conta(), receitas, despesas, receitas.subtract(despesas));
                })
                .sorted(Comparator.comparing(ResumoConta::nome))
                .toList();
    }

    /** Uma entrada por categoria do tipo, do maior para o menor total, com o valor separado por conta. */
    private List<Item> itens(List<TotalPorCategoria> linhas, TipoCategoria tipo, BigDecimal totalDoTipo) {
        Map<Long, List<TotalPorCategoria>> porCategoria = linhas.stream()
                .filter(l -> l.tipo() == tipo)
                .collect(Collectors.groupingBy(TotalPorCategoria::categoriaId, LinkedHashMap::new, Collectors.toList()));

        return porCategoria.values().stream()
                .map(daCategoria -> {
                    BigDecimal total = somar(daCategoria, l -> true);
                    List<ValorPorConta> contas = daCategoria.stream()
                            .map(l -> new ValorPorConta(l.contaId(), l.conta(), l.total()))
                            .sorted(Comparator.comparing(ValorPorConta::total).reversed()
                                    .thenComparing(ValorPorConta::nome))
                            .toList();
                    TotalPorCategoria primeira = daCategoria.get(0);
                    return new Item(primeira.categoriaId(), primeira.categoria(), total,
                            total.multiply(CEM).divide(totalDoTipo, 2, RoundingMode.HALF_UP), contas);
                })
                .sorted(Comparator.comparing(Item::total).reversed().thenComparing(Item::categoria))
                .toList();
    }

    private BigDecimal somar(List<TotalPorCategoria> linhas, Predicate<TotalPorCategoria> filtro) {
        return linhas.stream()
                .filter(filtro)
                .map(TotalPorCategoria::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
