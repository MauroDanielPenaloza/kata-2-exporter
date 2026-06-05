package com.andbank.exporter.domain.port.in;

import com.andbank.exporter.domain.model.ExportResult;
import com.andbank.exporter.domain.model.StatementExportRequest;

/**
 * Puerto de entrada — caso de uso principal.
 *
 * El adapter REST llama a este puerto.
 * La capa de aplicación lo implementa.
 */
public interface ExportStatementUseCase {

    /**
     * Exporta los movimientos de una cuenta según el formato solicitado.
     *
     * @param request datos del pedido (cuenta, formato, rango de fechas)
     * @return resultado listo para streaming HTTP
     * @throws com.andbank.exporter.application.service.ExportException si el formato no está soportado
     *         o la cuenta no existe
     */
    ExportResult execute(StatementExportRequest request);
}
