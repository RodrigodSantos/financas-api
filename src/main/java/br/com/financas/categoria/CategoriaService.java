package br.com.financas.categoria;

import br.com.financas.categoria.dto.CategoriaRequest;
import br.com.financas.categoria.dto.CategoriaResponse;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RegraNegocioException;
import br.com.financas.transacao.TransacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final TransacaoRepository transacaoRepository;

    public CategoriaService(CategoriaRepository repository, TransacaoRepository transacaoRepository) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(TipoCategoria tipo, Pageable pageable) {
        Page<Categoria> pagina = tipo == null
                ? repository.findAll(pageable)
                : repository.findByTipo(tipo, pageable);
        return pagina.map(CategoriaResponse::de);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        if (repository.existsByNomeIgnoreCaseAndTipo(request.nome(), request.tipo())) {
            throw new RegraNegocioException("Já existe uma categoria de " + request.tipo() + " com o nome '" + request.nome() + "'");
        }
        Categoria categoria = repository.save(new Categoria(request.nome(), request.tipo()));
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        if (repository.existsByNomeIgnoreCaseAndTipoAndIdNot(request.nome(), request.tipo(), id)) {
            throw new RegraNegocioException("Já existe uma categoria de " + request.tipo() + " com o nome '" + request.nome() + "'");
        }
        // Trocar o tipo deixaria as transações existentes incoerentes (ex.: despesas numa categoria de receita)
        if (categoria.getTipo() != request.tipo() && transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("A categoria '" + categoria.getNome() + "' possui transações e não pode mudar de tipo");
        }
        categoria.atualizar(request.nome(), request.tipo());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("A categoria '" + categoria.getNome() + "' possui transações e não pode ser excluída");
        }
        repository.delete(categoria);
    }

    private Categoria buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }
}
