package de.tum.cit.aet.core.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that maps exceptions to appropriate HTTP responses.
 * Prevents stack trace leakage by returning safe error messages for unhandled exceptions.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Error response body returned to clients.
     *
     * @param status  the HTTP status code
     * @param error   the HTTP status reason phrase
     * @param message a human-readable error message
     */
    public record ErrorResponse(int status, String error, String message) {}

    /**
     * Handles resource not found exceptions.
     *
     * @param ex the exception
     * @return 404 response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles Spring Security's access denied exceptions, which also covers the custom
     * {@link de.tum.cit.aet.core.exceptions.AccessDeniedException} since it extends Spring's.
     *
     * @param ex the exception
     * @return 403 response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Handles constraint violation exceptions from bean validation.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles method argument validation exceptions from {@code @Valid} annotated parameters.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Catches all unhandled exceptions and returns a safe 500 response without stack trace details.
     * The actual exception is logged at ERROR level for debugging.
     *
     * @param ex the exception
     * @return 500 response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(body);
    }
}
