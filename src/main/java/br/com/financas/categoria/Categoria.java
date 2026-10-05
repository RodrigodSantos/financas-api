package br.com.financas.categoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nulo = categoria global (padrão do sistema, visível para todos e somente leitura)
    @Column(name = "usuario_id", updatable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 60)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCategoria tipo;

    protected Categoria() {
    }

    public Categoria(Long usuarioId, String nome, TipoCategoria tipo) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.tipo = tipo;
    }

    public boolean isGlobal() {
        return usuarioId == null;
    }

    /** Global ou do próprio usuário. */
    public boolean isVisivelPara(Long usuarioId) {
        return isGlobal() || this.usuarioId.equals(usuarioId);
    }

    public void atualizar(String nome, TipoCategoria tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public TipoCategoria getTipo() {
        return tipo;
    }
}
