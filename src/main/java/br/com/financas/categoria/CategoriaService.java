package br.com.financas.categoria;

import br.com.financas.categoria.dto.CategoriaRequest;
import br.com.financas.categoria.dto.CategoriaResponse;
import br.com.financas.shared.exception.AcessoNegadoException;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RegraNegocioException;
import br.com.financas.shared.usuario.UsuarioLogado;
import br.com.financas.transacao.TransacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioLogado usuarioLogado;

    public CategoriaService(CategoriaRepository repository, TransacaoRepository transacaoRepository,
                            UsuarioLogado usuarioLogado) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(TipoCategoria tipo, Pageable pageable) {
        Long usuarioId = usuarioLogado.getId();
        Page<Categoria> pagina = tipo == null
                ? repository.findVisiveis(usuarioId, pageable)
                : repository.findVisiveisPorTipo(usuarioId, tipo, pageable);
        return pagina.map(CategoriaResponse::de);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.de(buscarVisivel(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Long usuarioId = usuarioLogado.getId();
        if (repository.existeVisivelComNome(usuarioId, request.nome(), request.tipo())) {
            throw new RegraNegocioException("Já existe uma categoria de " + request.tipo() + " com o nome '" + request.nome() + "'");
        }
        Categoria categoria = repository.save(new Categoria(usuarioId, request.nome(), request.tipo()));
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarPropria(id);
        if (repository.existeOutraVisivelComNome(categoria.getUsuarioId(), request.nome(), request.tipo(), id)) {
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
        Categoria categoria = buscarPropria(id);
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("A categoria '" + categoria.getNome() + "' possui transações e não pode ser excluída");
        }
        repository.delete(categoria);
    }

    /** Categoria de outro usuário responde 404, para não revelar que existe. */
    private Categoria buscarVisivel(Long id) {
        return repository.findById(id)
                .filter(c -> c.isVisivelPara(usuarioLogado.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    /** Para alterar ou excluir: as globais são visíveis, mas somente leitura (403). */
    private Categoria buscarPropria(Long id) {
        Categoria categoria = buscarVisivel(id);
        if (categoria.isGlobal()) {
            throw new AcessoNegadoException("Categorias padrão do sistema não podem ser alteradas nem excluídas");
        }
        return categoria;
    }
}
