package br.com.financas.shared.exception;

import org.hibernate.query.sqm.PathElementException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Padroniza as respostas de erro no formato RFC 7807 (Problem Details).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern ATRIBUTO_NAO_ENCONTRADO = Pattern.compile("attribute '([^']+)'");

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso não encontrado");
        return problem;
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail handleRegraNegocio(RegraNegocioException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Regra de negócio violada");
        return problem;
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ProblemDetail handleRequisicaoInvalida(RequisicaoInvalidaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Requisição inválida");
        return problem;
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Não autenticado");
        return problem;
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ProblemDetail handleAcessoNegado(AcessoNegadoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Acesso negado");
        return problem;
    }

    /** "sort" com campo inexistente em consultas derivadas ou Specifications (ex.: sort=string). */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleOrdenacaoInvalida(PropertyReferenceException ex) {
        return ordenacaoInvalida(ex.getPropertyName());
    }

    /**
     * O mesmo problema em consultas com @Query: quem recusa é o Hibernate, e a exceção chega embrulhada.
     * Qualquer outro uso indevido da API de dados é erro do código e continua sendo 500.
     */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail handleUsoInvalidoDeDados(InvalidDataAccessApiUsageException ex) {
        Throwable causa = NestedExceptionUtils.getMostSpecificCause(ex);
        if (causa instanceof PathElementException) {
            Matcher atributo = ATRIBUTO_NAO_ENCONTRADO.matcher(causa.getMessage());
            return ordenacaoInvalida(atributo.find() ? atributo.group(1) : "desconhecido");
        }
        throw ex;
    }

    private ProblemDetail ordenacaoInvalida(String campo) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Não é possível ordenar por '" + campo + "'. Use um campo da resposta, ex.: sort=nome,asc");
        problem.setTitle("Ordenação inválida");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos");
        problem.setTitle("Erro de validação");
        problem.setProperty("campos", campos);
        return problem;
    }
}
