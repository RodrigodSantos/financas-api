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

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> listar(Pageable pageable) {
        return repository.findByContaUsuarioId(usuarioLogado.getId(), pageable)
                .map(TransacaoResponse::de);
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
                .filter(c -> c.getUsuarioId() == null || c.getUsuarioId().equals(usuarioLogado.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", categoriaId));
    }
}
