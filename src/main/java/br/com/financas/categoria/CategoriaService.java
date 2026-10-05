package br.com.financas.categoria;

import br.com.financas.categoria.dto.CategoriaRequest;
import br.com.financas.categoria.dto.CategoriaResponse;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RegraNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
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
        categoria.atualizar(request.nome(), request.tipo());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntidade(id));
    }

    private Categoria buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }
}
