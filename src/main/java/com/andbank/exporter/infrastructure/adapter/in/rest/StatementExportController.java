package com.andbank.exporter.infrastructure.adapter.in.rest;

import com.andbank.exporter.application.service.ExportException;
import com.andbank.exporter.domain.model.ExportFormat;
import com.andbank.exporter.domain.model.ExportResult;
import com.andbank.exporter.domain.model.StatementExportRequest;
import com.andbank.exporter.domain.port.in.ExportStatementUseCase;
import com.andbank.exporter.infrastructure.adapter.in.rest.dto.ExportRequestDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Adapter de entrada REST.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Parsear y validar parámetros HTTP</li>
 *   <li>Traducir a {@link StatementExportRequest}</li>
 *   <li>Delegar al use case</li>
 *   <li>Escribir {@link ExportResult} en la respuesta HTTP (Content-Disposition, content-type)</li>
 * </ul>
 *
 * <h3>Endpoints disponibles:</h3>
 * <pre>
 * GET /api/v1/accounts/{accountId}/statement/export
 *   ?format=CSV|PDF|OFX|JSON
 *   &dateFrom=2024-01-01
 *   &dateTo=2024-03-31
 * </pre>
 *
 * <h3>Ejemplos de uso con curl:</h3>
 * <pre>
 * # CSV — cuenta pesos Morales, Q1 2024
 * curl -o movimientos.csv \
 *   "http://localhost:8080/api/v1/accounts/a001/statement/export?format=CSV&dateFrom=2024-01-01&dateTo=2024-03-31"
 *
 * # PDF
 * curl -o movimientos.pdf \
 *   "http://localhost:8080/api/v1/accounts/a001/statement/export?format=PDF&dateFrom=2024-01-01&dateTo=2024-03-31"
 *
 * # OFX
 * curl -o movimientos.ofx \
 *   "http://localhost:8080/api/v1/accounts/a001/statement/export?format=OFX&dateFrom=2024-01-01&dateTo=2024-03-31"
 *
 * # JSON
 * curl "http://localhost:8080/api/v1/accounts/a001/statement/export?format=JSON&dateFrom=2024-01-01&dateTo=2024-03-31"
 *
 * # Listar accounts disponibles
 * curl "http://localhost:8080/api/v1/accounts"
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class StatementExportController {

    private static final Logger log = LoggerFactory.getLogger(StatementExportController.class);

    private final ExportStatementUseCase exportStatementUseCase;

    public StatementExportController(ExportStatementUseCase exportStatementUseCase) {
        this.exportStatementUseCase = exportStatementUseCase;
    }

    /**
     * Exporta el resumen de movimientos en el formato solicitado.
     *
     * <p>El adapter es el único punto donde se toca la respuesta HTTP.
     * El strategy no sabe nada de HTTP.</p>
     */
    @GetMapping("/{accountId}/statement/export")
    public ResponseEntity<byte[]> exportStatement(
            @PathVariable String accountId,
            @RequestParam ExportFormat format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        log.info("Request exportación: accountId={}, format={}, from={}, to={}",
                accountId, format, dateFrom, dateTo);

        StatementExportRequest request = StatementExportRequest.builder()
                .accountId(accountId)
                .format(format)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        ExportResult result = exportStatementUseCase.execute(request);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, result.getContentType());
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + result.getFilename() + "\"");
        headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.size()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.getContent());
    }

    // ── Error handling ─────────────────────────────────────────────────────

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<String> handleExportException(ExportException ex) {
        log.warn("ExportException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        log.warn("IllegalStateException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno: " + ex.getMessage());
    }
}
