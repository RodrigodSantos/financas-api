package br.com.financas.conta;

import br.com.financas.categoria.TipoCategoria;
import br.com.financas.conta.dto.ContaRequest;
import br.com.financas.conta.dto.ContaResponse;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RegraNegocioException;
import br.com.financas.shared.usuario.UsuarioLogado;
import br.com.financas.transacao.MovimentacaoPorConta;
import br.com.financas.transacao.TransacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContaService {

    private final ContaRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioLogado usuarioLogado;

    public ContaService(ContaRepository repository, TransacaoRepository transacaoRepository, UsuarioLogado usuarioLogado) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional(readOnly = true)
    public Page<ContaResponse> listar(TipoConta tipo, Pageable pageable) {
        Long usuarioId = usuarioLogado.getId();
        Page<Conta> pagina = tipo == null
                ? repository.findByUsuarioId(usuarioId, pageable)
                : repository.findByUsuarioIdAndTipo(usuarioId, tipo, pageable);

        // Uma consulta de soma para a página inteira, não uma por conta
        Map<Long, BigDecimal> movimentacoes = pagina.isEmpty()
                ? Map.of()
                : transacaoRepository.somarMovimentacaoPorConta(
                                pagina.map(Conta::getId).getContent(), TipoCategoria.RECEITA)
                        .stream()
                        .collect(Collectors.toMap(MovimentacaoPorConta::contaId, MovimentacaoPorConta::total));

        return pagina.map(conta -> ContaResponse.de(conta, movimentacoes.getOrDefault(conta.getId(), BigDecimal.ZERO)));
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id) {
        return comSaldo(buscarEntidade(id));
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Long usuarioId = usuarioLogado.getId();
        if (repository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, request.nome())) {
            throw new RegraNegocioException("Você já tem uma conta com o nome '" + request.nome() + "'");
        }
        Conta conta = repository.save(new Conta(usuarioId, request.nome(), request.tipo(), request.saldoInicialOuZero()));
        return ContaResponse.de(conta, BigDecimal.ZERO);
    }

    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest request) {
        Conta conta = buscarEntidade(id);
        if (repository.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(conta.getUsuarioId(), request.nome(), id)) {
            throw new RegraNegocioException("Você já tem uma conta com o nome '" + request.nome() + "'");
        }
        conta.atualizar(request.nome(), request.tipo(), request.saldoInicialOuZero());
        return comSaldo(conta);
    }

    @Transactional
    public void excluir(Long id) {
        Conta conta = buscarEntidade(id);
        if (transacaoRepository.existsByContaId(id)) {
            throw new RegraNegocioException("A conta '" + conta.getNome() + "' possui transações e não pode ser excluída");
        }
        repository.delete(conta);
    }

    private ContaResponse comSaldo(Conta conta) {
        return ContaResponse.de(conta, transacaoRepository.somarMovimentacao(conta.getId(), TipoCategoria.RECEITA));
    }

    /**
     * Busca sempre filtrando pelo usuário: a conta de outra pessoa responde 404,
     * para não revelar que ela existe.
     */
    private Conta buscarEntidade(Long id) {
        return repository.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta", id));
    }
}
