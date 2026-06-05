package com.andbank.exporter.domain.model;

import java.time.LocalDate;

/**
 * Pedido de exportación de movimientos.
 * Entra por el puerto de entrada y viaja hasta el strategy seleccionado.
 */
public class StatementExportRequest {

    private final String accountId;
    private final ExportFormat format;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;

    private StatementExportRequest(Builder builder) {
        this.accountId = builder.accountId;
        this.format    = builder.format;
        this.dateFrom  = builder.dateFrom;
        this.dateTo    = builder.dateTo;
    }

    public String getAccountId()   { return accountId; }
    public ExportFormat getFormat(){ return format; }
    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo()   { return dateTo; }

    // ── Builder ────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String accountId;
        private ExportFormat format;
        private LocalDate dateFrom;
        private LocalDate dateTo;

        public Builder accountId(String accountId)   { this.accountId = accountId; return this; }
        public Builder format(ExportFormat format)   { this.format = format;       return this; }
        public Builder dateFrom(LocalDate dateFrom)  { this.dateFrom = dateFrom;   return this; }
        public Builder dateTo(LocalDate dateTo)      { this.dateTo = dateTo;       return this; }

        public StatementExportRequest build() {
            if (accountId == null) throw new IllegalStateException("accountId requerido");
            if (format == null)    throw new IllegalStateException("format requerido");
            if (dateFrom == null)  throw new IllegalStateException("dateFrom requerido");
            if (dateTo == null)    throw new IllegalStateException("dateTo requerido");
            if (dateFrom.isAfter(dateTo))
                throw new IllegalStateException("dateFrom no puede ser posterior a dateTo");
            return new StatementExportRequest(this);
        }
    }
}
