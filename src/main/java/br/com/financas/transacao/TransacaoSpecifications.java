package br.com.financas.transacao;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Monta o WHERE da consulta só com os filtros que foram informados.
 * Sem isso, cada combinação de filtros opcionais precisaria de um método próprio no repository.
 */
public final class TransacaoSpecifications {

    private TransacaoSpecifications() {
    }

    public static Specification<Transacao> doUsuario(Long usuarioId, TransacaoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            // Sempre presente: isola os dados do usuário, independente dos filtros
            predicados.add(cb.equal(root.get("conta").get("usuarioId"), usuarioId));

            if (filtro.inicio() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("data"), filtro.inicio()));
            }
            if (filtro.fim() != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("data"), filtro.fim()));
            }
            if (filtro.contaId() != null) {
                predicados.add(cb.equal(root.get("conta").get("id"), filtro.contaId()));
            }
            if (filtro.categoriaId() != null) {
                predicados.add(cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            }
            if (filtro.tipo() != null) {
                predicados.add(cb.equal(root.get("tipo"), filtro.tipo()));
            }
            if (filtro.descricao() != null && !filtro.descricao().isBlank()) {
                String trecho = "%" + escaparLike(filtro.descricao().trim().toLowerCase(Locale.ROOT)) + "%";
                predicados.add(cb.like(cb.lower(root.get("descricao")), trecho, '\\'));
            }

            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }

    /** Impede que % e _ digitados pelo usuário virem curingas do LIKE. */
    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
