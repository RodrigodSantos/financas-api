package br.com.financas.shared.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        // Mensagem genérica de propósito: não revela se o e-mail existe
        super("E-mail ou senha inválidos");
    }
}
