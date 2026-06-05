package com.andbank.exporter.infrastructure.adapter.in.rest.dto;

import com.andbank.exporter.domain.model.ExportFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO de entrada para el endpoint de exportación.
 * Recibe los parámetros por query string.
 */
public class ExportRequestDTO {

    @NotNull(message = "El parámetro 'format' es requerido")
    private ExportFormat format;

    @NotNull(message = "El parámetro 'dateFrom' es requerido")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @NotNull(message = "El parámetro 'dateTo' es requerido")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    public ExportFormat getFormat()              { return format; }
    public void setFormat(ExportFormat format)   { this.format = format; }

    public LocalDate getDateFrom()               { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom)  { this.dateFrom = dateFrom; }

    public LocalDate getDateTo()                 { return dateTo; }
    public void setDateTo(LocalDate dateTo)      { this.dateTo = dateTo; }
}
