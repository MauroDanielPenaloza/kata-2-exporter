package com.andbank.exporter.application.strategy;

import com.andbank.exporter.domain.model.Account;
import com.andbank.exporter.domain.model.Customer;
import com.andbank.exporter.domain.model.ExportFormat;
import com.andbank.exporter.domain.model.ExportResult;
import com.andbank.exporter.domain.model.Transaction;

import java.util.List;

/**
 * Contrato del patrón Strategy para exportación de movimientos bancarios.
 *
 * <p><strong>Regla de oro (Módulo 5):</strong></p>
 * <ul>
 *   <li>Siempre retorna {@link ExportResult} con {@code byte[]} y {@code contentType}.</li>
 *   <li><em>Nunca</em> escribe a disco — eso es responsabilidad del adapter de salida.</li>
 *   <li>No depende de {@code HttpServletResponse} ni de ninguna clase de infraestructura.</li>
 *   <li>Todo el contenido se genera en memoria ({@code ByteArrayOutputStream}).</li>
 * </ul>
 *
**/
public interface StatementExportStrategy {

    /**
     * Formato que maneja esta implementación.
     * Usado por {@code StatementExportService} para resolver el strategy en O(1).
     */
    ExportFormat supportedFormat();

    /**
     * Genera el contenido exportado en memoria y lo envuelve en un {@link ExportResult}.
     *
     * @param transactions movimientos ya filtrados por el service
     * @param account      cuenta del titular
     * @param customer     datos del cliente para el encabezado
     * @return resultado listo para ser enviado por HTTP — nunca null
     */
    ExportResult export(List<Transaction> transactions, Account account, Customer customer);
}
