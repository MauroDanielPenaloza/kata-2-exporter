# Requerimiento — CSV Export Strategy

Implementar `CSVExportStrategy`.

## Objetivo

Crear el strategy de exportación CSV con encabezado de metadatos y filas de transacciones, compatible con Excel en Windows.

## Checklist

1. Crear `CSVExportStrategy` en `application/strategy/` (ver modelo en `StatementExportStrategy.java`)
2. Descomentar el bloque `CSVTests` en `StatementExportStrategyTest`
3. Agregar al smoke test en `ExportResultContractTest`

## Especificación

- **Content-type:** `text/csv; charset=UTF-8`
- **Filename:** `movimientos_{accountId}.csv`
- **Encoding:** UTF-8 con BOM (0xEF 0xBB 0xBF) para compatibilidad con Excel en Windows
- **Encabezado:** líneas con `#` (titular, CBU, alias, moneda)
- **Columnas:** `fecha,referencia,descripcion,tipo,importe,moneda,saldo_posterior`
- **Formato fecha:** `dd/MM/yyyy HH:mm`
- **Amounts:** `toPlainString()` (sin notación científica)
- **Escapado:** campos con comas o comillas envueltos en `"`, escapar `"` como `""`

## Archivos clave

- `src/main/java/com/andbank/exporter/application/strategy/StatementExportStrategy.java` — modelo de referencia
- `src/main/java/com/andbank/exporter/domain/model/ExportResult.java`
- `src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java` — bloque `CSVTests`

## Regla de oro

Todo strategy retorna `ExportResult` con el contenido como `byte[]` y el `ContentType` correspondiente. **Nunca escribir a disco** — eso es responsabilidad del adapter de salida (`StatementExportController`).
