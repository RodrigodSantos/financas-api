package br.com.financas.transacao;

import br.com.financas.categoria.TipoCategoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {

    // Carrega conta e categoria na mesma consulta, evitando o problema N+1 na listagem
    @Override
    @EntityGraph(attributePaths = {"conta", "categoria"})
    Page<Transacao> findAll(Specification<Transacao> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"conta", "categoria"})
    List<Transacao> findAll(Specification<Transacao> spec, Sort sort);

    @EntityGraph(attributePaths = {"conta", "categoria"})
    Optional<Transacao> findByIdAndContaUsuarioId(Long id, Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN t.tipo = :receita THEN t.valor ELSE -t.valor END), 0)
            FROM Transacao t
            WHERE t.conta.id = :contaId
            """)
    BigDecimal somarMovimentacao(@Param("contaId") Long contaId, @Param("receita") TipoCategoria receita);

    // Uma única consulta para todas as contas da página, em vez de uma por conta
    @Query("""
            SELECT new br.com.financas.transacao.MovimentacaoPorConta(
                t.conta.id, SUM(CASE WHEN t.tipo = :receita THEN t.valor ELSE -t.valor END))
            FROM Transacao t
            WHERE t.conta.id IN :contaIds
            GROUP BY t.conta.id
            """)
    List<MovimentacaoPorConta> somarMovimentacaoPorConta(@Param("contaIds") Collection<Long> contaIds,
                                                         @Param("receita") TipoCategoria receita);
}
