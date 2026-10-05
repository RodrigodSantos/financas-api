package br.com.financas.conta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConta tipo;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Conta() {
    }

    public Conta(Long usuarioId, String nome, TipoConta tipo, BigDecimal saldoInicial) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
        this.criadoEm = LocalDateTime.now();
    }

    public void atualizar(String nome, TipoConta tipo, BigDecimal saldoInicial) {
        this.nome = nome;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
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

    public TipoConta getTipo() {
        return tipo;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
