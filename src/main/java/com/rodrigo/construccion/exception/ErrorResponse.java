package com.rodrigo.construccion.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Respuesta estándar para errores
 * 
 * Proporciona una estructura consistente para todas las respuestas de error.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private java.util.Map<String, String> validationErrors;
        private Object details;
        private String traceId;

        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder status(int status) { this.status = status; return this; }
        public Builder error(String error) { this.error = error; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder validationErrors(java.util.Map<String, String> validationErrors) { this.validationErrors = validationErrors; return this; }
        public Builder details(Object details) { this.details = details; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }

        public ErrorResponse build() {
            return new ErrorResponse(timestamp, status, error, message, path, validationErrors, details, traceId);
        }
    }

    /**
     * Timestamp del error
     */
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private java.util.Map<String, String> validationErrors;
    private Object details;
    private String traceId;

    public ErrorResponse() {}

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, java.util.Map<String, String> validationErrors, Object details, String traceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
        this.details = details;
        this.traceId = traceId;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public java.util.Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(java.util.Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}