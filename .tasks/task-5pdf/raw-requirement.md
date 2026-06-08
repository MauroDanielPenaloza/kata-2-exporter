# Requerimiento — PDF Export Strategy

Implementar `PDFExportStrategy`.

## Objetivo

Crear el strategy de exportación PDF con encabezado, tabla de movimientos y pie de resumen.

## Checklist

1. Crear `PDFExportStrategy` en `application/strategy/` (ver modelo en `StatementExportStrategy.java`)
2. Descomentar el bloque `PDFTests` en `StatementExportStrategyTest`
3. Agregar al smoke test en `ExportResultContractTest`

## Especificación

- **Content-type:** `application/pdf`
- **Filename:** `movimientos_{accountId}.pdf`
- **Librería:** iText 8 (kernel + layout, ya en `pom.xml`)
- **Magic bytes:** el PDF siempre empieza con `%PDF` (0x25 0x50 0x44 0x46)
- **Encabezado:** título centrado, datos del titular, cuenta, moneda y saldo
- **Tabla:** columnas Fecha, Referencia, Descripción, Tipo, Importe, Saldo
- **Estilos:** header gris, filas alternas, débitos en rojo, créditos en verde
- **Pie:** total movimientos, créditos y débitos

## Archivos clave

- `src/main/java/com/andbank/exporter/application/strategy/StatementExportStrategy.java` — modelo de referencia
- `src/main/java/com/andbank/exporter/domain/model/ExportResult.java`
- `src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java` — bloque `PDFTests`

## Regla de oro

Todo strategy retorna `ExportResult` con el contenido como `byte[]` y el `ContentType` correspondiente. **Nunca escribir a disco** — eso es responsabilidad del adapter de salida (`StatementExportController`).
