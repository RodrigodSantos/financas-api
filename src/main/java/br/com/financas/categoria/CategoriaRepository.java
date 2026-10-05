package br.com.financas.categoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * "Visível" = categoria global (usuario_id nulo) ou do próprio usuário.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("SELECT c FROM Categoria c WHERE c.usuarioId IS NULL OR c.usuarioId = :usuarioId")
    Page<Categoria> findVisiveis(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT c FROM Categoria c WHERE c.tipo = :tipo AND (c.usuarioId IS NULL OR c.usuarioId = :usuarioId)")
    Page<Categoria> findVisiveisPorTipo(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoCategoria tipo,
                                        Pageable pageable);

    // O nome não pode repetir nem entre as categorias do usuário, nem com as globais
    @Query("""
            SELECT COUNT(c) > 0 FROM Categoria c
            WHERE LOWER(c.nome) = LOWER(:nome) AND c.tipo = :tipo
              AND (c.usuarioId IS NULL OR c.usuarioId = :usuarioId)
            """)
    boolean existeVisivelComNome(@Param("usuarioId") Long usuarioId, @Param("nome") String nome,
                                 @Param("tipo") TipoCategoria tipo);

    @Query("""
            SELECT COUNT(c) > 0 FROM Categoria c
            WHERE LOWER(c.nome) = LOWER(:nome) AND c.tipo = :tipo AND c.id <> :id
              AND (c.usuarioId IS NULL OR c.usuarioId = :usuarioId)
            """)
    boolean existeOutraVisivelComNome(@Param("usuarioId") Long usuarioId, @Param("nome") String nome,
                                      @Param("tipo") TipoCategoria tipo, @Param("id") Long id);
}
