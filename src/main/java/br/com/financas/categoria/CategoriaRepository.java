package br.com.financas.categoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Page<Categoria> findByTipo(TipoCategoria tipo, Pageable pageable);

    boolean existsByNomeIgnoreCaseAndTipo(String nome, TipoCategoria tipo);

    boolean existsByNomeIgnoreCaseAndTipoAndIdNot(String nome, TipoCategoria tipo, Long id);
}
