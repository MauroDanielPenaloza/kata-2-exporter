package com.andbank.exporter.application.service;

/**
 * Excepción de dominio para errores en el proceso de exportación.
 */
public class ExportException extends RuntimeException {

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
