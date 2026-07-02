package br.edu.gestaocrises.config;

import br.edu.gestaocrises.common.CredenciaisInvalidasException;
import br.edu.gestaocrises.common.ErrorResponseDTO;
import br.edu.gestaocrises.common.RecursoNaoEncontradoException;
import br.edu.gestaocrises.common.RegraNegocioException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), List.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponseDTO> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Autenticação inválida", ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleRegraNegocio(RegraNegocioException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Regra de negócio violada", ex.getMessage(), List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Autenticação inválida", "Credenciais inválidas", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação", "Entrada inválida", detalhes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> detalhes = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação", "Entrada inválida", detalhes);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para acessar este recurso", List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação", "Corpo da requisição inválido", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado", List.of());
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String erro, String mensagem, List<String> detalhes) {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .status(status.value())
                .erro(erro)
                .mensagem(mensagem)
                .detalhes(detalhes)
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
