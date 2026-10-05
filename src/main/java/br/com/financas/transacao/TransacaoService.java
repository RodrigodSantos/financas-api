package br.com.financas.transacao;

import br.com.financas.categoria.Categoria;
import br.com.financas.categoria.CategoriaRepository;
import br.com.financas.conta.Conta;
import br.com.financas.conta.ContaRepository;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RegraNegocioException;
import br.com.financas.shared.usuario.UsuarioLogado;
import br.com.financas.transacao.dto.TransacaoRequest;
import br.com.financas.transacao.dto.TransacaoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioLogado usuarioLogado;

    public TransacaoService(TransacaoRepository repository,
                            ContaRepository contaRepository,
                            CategoriaRepository categoriaRepository,
                            UsuarioLogado usuarioLogado) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioLogado = usuarioLogado;
    }

    static final int LIMITE_EXPORTACAO = 10_000;

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> listar(TransacaoFiltro filtro, Pageable pageable) {
        filtro.validar();
        return repository.findAll(TransacaoSpecifications.doUsuario(usuarioLogado.getId(), filtro), pageable)
                .map(TransacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(TransacaoFiltro filtro) {
        filtro.validar();
        Specification<Transacao> spec = TransacaoSpecifications.doUsuario(usuarioLogado.getId(), filtro);

        long total = repository.count(spec);
        if (total > LIMITE_EXPORTACAO) {
            throw new RegraNegocioException("A exportação tem " + total + " transações, acima do limite de "
                    + LIMITE_EXPORTACAO + ". Refine o período ou os filtros.");
        }
        return TransacaoCsv.gerar(repository.findAll(spec, Sort.by("data", "id")));
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscar(Long id) {
        return TransacaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request) {
        Conta conta = buscarConta(request.contaId());
        Categoria categoria = buscarCategoria(request.categoriaId());
        validarTipo(request, categoria);

        Transacao transacao = repository.save(new Transacao(
                conta, categoria, request.descricao(), request.valor(), request.tipo(), request.data()));
        return TransacaoResponse.de(transacao);
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request) {
        Transacao transacao = buscarEntidade(id);
        Conta conta = buscarConta(request.contaId());
        Categoria categoria = buscarCategoria(request.categoriaId());
        validarTipo(request, categoria);

        transacao.atualizar(conta, categoria, request.descricao(), request.valor(), request.tipo(), request.data());
        return TransacaoResponse.de(transacao);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntidade(id));
    }

    private void validarTipo(TransacaoRequest request, Categoria categoria) {
        if (request.tipo() != categoria.getTipo()) {
            throw new RegraNegocioException("Uma " + request.tipo() + " não pode usar a categoria '"
                    + categoria.getNome() + "', que é de " + categoria.getTipo());
        }
    }

    private Transacao buscarEntidade(Long id) {
        return repository.findByIdAndContaUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação", id));
    }

    private Conta buscarConta(Long contaId) {
        return contaRepository.findByIdAndUsuarioId(contaId, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta", contaId));
    }

    /** Aceita categoria global (sem dono) ou do próprio usuário. */
    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .filter(c -> c.isVisivelPara(usuarioLogado.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", categoriaId));
    }
}
