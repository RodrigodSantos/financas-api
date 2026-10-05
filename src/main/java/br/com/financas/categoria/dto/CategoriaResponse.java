package br.com.financas.categoria.dto;

import br.com.financas.categoria.Categoria;
import br.com.financas.categoria.TipoCategoria;

public record CategoriaResponse(Long id, String nome, TipoCategoria tipo, boolean global) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getTipo(),
                categoria.getUsuarioId() == null
        );
    }
}
