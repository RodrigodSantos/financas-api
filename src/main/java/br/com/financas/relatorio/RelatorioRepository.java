package br.com.financas.relatorio;

import br.com.financas.transacao.Transacao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Consultas de leitura dos relatórios. Estende {@link Repository} (e não JpaRepository)
 * porque não precisa de save/delete: só expõe as consultas declaradas aqui.
 */
public interface RelatorioRepository extends Repository<Transacao, Long> {

    // A soma é feita no banco: uma consulta só, não importa quantas transações existam.
    // Agrupa por categoria E conta; o service monta os totais por categoria e o resumo por conta a partir disso.
    @Query("""
            SELECT new br.com.financas.relatorio.TotalPorCategoria(
                c.id, c.nome, t.tipo, co.id, co.nome, SUM(t.valor))
            FROM Transacao t
            JOIN t.categoria c
            JOIN t.conta co
            WHERE co.usuarioId = :usuarioId
              AND t.data BETWEEN :inicio AND :fim
              AND (:contaId IS NULL OR co.id = :contaId)
            GROUP BY c.id, c.nome, t.tipo, co.id, co.nome
            """)
    List<TotalPorCategoria> totaisPorCategoria(@Param("usuarioId") Long usuarioId,
                                               @Param("inicio") LocalDate inicio,
                                               @Param("fim") LocalDate fim,
                                               @Param("contaId") Long contaId);
}
