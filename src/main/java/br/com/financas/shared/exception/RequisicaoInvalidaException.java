package br.com.financas.shared.exception;

/** Parâmetros da requisição incoerentes entre si (ex.: período com início depois do fim). */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
