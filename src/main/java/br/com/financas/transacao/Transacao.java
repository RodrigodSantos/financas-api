package br.com.financas.transacao;

import br.com.financas.categoria.Categoria;
import br.com.financas.categoria.TipoCategoria;
import br.com.financas.conta.Conta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false, length = 160)
    private String descricao;

    // Sempre positivo: quem define se soma ou subtrai é o tipo
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCategoria tipo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Transacao() {
    }

    public Transacao(Conta conta, Categoria categoria, String descricao, BigDecimal valor, TipoCategoria tipo, LocalDate data) {
        this.conta = conta;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
        this.criadoEm = LocalDateTime.now();
    }

    public void atualizar(Conta conta, Categoria categoria, String descricao, BigDecimal valor, TipoCategoria tipo, LocalDate data) {
        this.conta = conta;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public TipoCategoria getTipo() {
        return tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
