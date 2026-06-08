# Requerimiento — XLSX Export Strategy (Módulo 3)

Implementar `XLSXExportStrategy` con una hoja por mes.

## Objetivo

Crear el strategy de exportación XLSX que genere un archivo Excel en memoria con las transacciones agrupadas por mes.

## Checklist

1. Agregar `XLSX` al enum `ExportFormat`
2. Descomentar la dependencia `poi-ooxml` en `pom.xml`
3. Crear `XLSXExportStrategy` en `application/strategy/` (ver modelo en `StatementExportStrategy.java`)
4. Descomentar el bloque `XLSXTests` en `StatementExportStrategyTest`
5. Agregar al smoke test en `ExportResultContractTest`

## Especificación

- **Content-type:** `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Filename:** `movimientos_{accountId}.xlsx`
- **Librería:** Apache POI poi-ooxml
- **Magic bytes:** XLSX es un ZIP → empieza con `PK` (0x50 0x4B)
- **Estructura:** una hoja por mes con las transacciones de ese mes
- **Nombre de cada hoja:** `"Enero 2024"`, `"Febrero 2024"`, etc.

## Archivos clave

- `src/main/java/com/andbank/exporter/application/strategy/StatementExportStrategy.java` — modelo de referencia
- `src/main/java/com/andbank/exporter/domain/model/ExportResult.java`
- `src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java` — bloque `XLSXTests`

## Regla de oro

Todo strategy retorna `ExportResult` con el contenido como `byte[]` y el `ContentType` correspondiente. **Nunca escribir a disco** — eso es responsabilidad del adapter de salida (`StatementExportController`).
