package za.ac.cput.digitalpharmacysystem.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, InsufficientStockException.class})
    public ResponseEntity<Map<String, String>> handleConflict(RuntimeException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrity(DataIntegrityViolationException e) {
        return error(HttpStatus.CONFLICT, "Request conflicts with existing data (duplicate or still referenced)");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return new ResponseEntity<>(Map.of("error", message == null ? status.getReasonPhrase() : message), status);
    }
}