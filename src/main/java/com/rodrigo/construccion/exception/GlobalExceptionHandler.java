package com.rodrigo.construccion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador Global de Excepciones
 * 
 * Centraliza el manejo de errores en toda la aplicación.
 * Proporciona respuestas consistentes y logs detallados.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Manejo de errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de Validación")
                .message("Los datos enviados no son válidos")
                .path(request.getDescription(false).replace("uri=", ""))
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de errores de negocio (RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleBusinessErrors(RuntimeException ex, WebRequest request) {
        // LOG DEL ERROR PARA DEBUG
        System.err.println("❌❌❌ RuntimeException capturada en GlobalExceptionHandler ❌❌❌");
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Class: " + ex.getClass().getName());
        ex.printStackTrace();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de Negocio (" + ex.getClass().getSimpleName() + ")")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de errores de integridad de datos - evitar duplicados
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityErrors(DataIntegrityViolationException ex,
            WebRequest request) {

        String message = ex.getMessage();
        String specificMessage = "Error de integridad de datos. Verifique que los datos no estén duplicados o que las referencias a otras entidades sean correctas.";

        // LOG DEL ERROR COMPLETO
        System.err.println("❌❌❌ DataIntegrityViolationException ❌❌❌");
        System.err.println("Message: " + message);
        System.err.println("Root Cause: " + (ex.getRootCause() != null ? ex.getRootCause().getMessage() : "null"));
        System.err.println("Most Specific Cause: " + (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "null"));
        System.err.println("Full Stack Trace:");
        ex.printStackTrace();
        System.err.println("❌❌❌ Fin DataIntegrityViolationException ❌❌❌");

        // Lógica mejorada para detectar el error de duplicado de asignación.
        if (message != null && message.contains("uksexpao191og1qty6t5dtt589a")) { // Nombre de la constraint de unicidad
            specificMessage = "No puedes realizar esta actualización porque resultaría en un duplicado: el profesional ya está asignado a esa obra.";
        } else if (message != null && message.contains("duplicate key")) {
            specificMessage = "Ya existe un registro con los datos proporcionados.";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Error de Integridad")
                .message(specificMessage)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Manejo de errores de estado inválido de Pedido de Pago
     */
    @ExceptionHandler(PedidoPagoEstadoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handlePedidoPagoEstadoInvalido(PedidoPagoEstadoInvalidoException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Estado de Pedido Inválido")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de errores de seguridad (permisos, etc.)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Acceso Denegado")
                .message(ex.getMessage()) // Usamos el mensaje específico de la excepción
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }


    /**
     * Manejo de errores de entidad no encontrada (por ejemplo, obra, empresa,
     * presupuesto)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "No existe el recurso solicitado");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Manejo de errores de argumentos inválidos
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "Argumento inválido");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Manejo de errores generales
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "Error interno del servidor");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}