package br.com.financas.conta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Page<Conta> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<Conta> findByUsuarioIdAndTipo(Long usuarioId, TipoConta tipo, Pageable pageable);

    Optional<Conta> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(Long usuarioId, String nome, Long id);
}
