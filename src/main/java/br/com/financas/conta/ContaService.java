package br.com.financas.conta;

import br.com.financas.conta.dto.ContaRequest;
import br.com.financas.conta.dto.ContaResponse;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RegraNegocioException;
import br.com.financas.shared.usuario.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private final ContaRepository repository;
    private final UsuarioLogado usuarioLogado;

    public ContaService(ContaRepository repository, UsuarioLogado usuarioLogado) {
        this.repository = repository;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional(readOnly = true)
    public Page<ContaResponse> listar(TipoConta tipo, Pageable pageable) {
        Long usuarioId = usuarioLogado.getId();
        Page<Conta> pagina = tipo == null
                ? repository.findByUsuarioId(usuarioId, pageable)
                : repository.findByUsuarioIdAndTipo(usuarioId, tipo, pageable);
        return pagina.map(ContaResponse::de);
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id) {
        return ContaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Long usuarioId = usuarioLogado.getId();
        if (repository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, request.nome())) {
            throw new RegraNegocioException("Você já tem uma conta com o nome '" + request.nome() + "'");
        }
        Conta conta = repository.save(new Conta(usuarioId, request.nome(), request.tipo(), request.saldoInicialOuZero()));
        return ContaResponse.de(conta);
    }

    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest request) {
        Conta conta = buscarEntidade(id);
        if (repository.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(conta.getUsuarioId(), request.nome(), id)) {
            throw new RegraNegocioException("Você já tem uma conta com o nome '" + request.nome() + "'");
        }
        conta.atualizar(request.nome(), request.tipo(), request.saldoInicialOuZero());
        return ContaResponse.de(conta);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntidade(id));
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
