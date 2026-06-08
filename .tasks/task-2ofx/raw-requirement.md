# Requerimiento — OFX Export Strategy (Módulo 4)

Implementar `OFXExportStrategy`.

## Objetivo

Crear el strategy de exportación OFX que genere un archivo compatible con parsers bancarios estándar, completamente en memoria.

## Contexto quirúrgico

Al trabajar este módulo **no necesitás ver** CSV, JSON ni PDF. Solo necesitás:

- `OFXExportStrategy.java` (a crear)
- `StatementExportStrategy.java` — el modelo completo está en el Javadoc
- `ExportResult.java`
- el bloque `OFXTests` en `StatementExportStrategyTest`

## Checklist

1. Crear `OFXExportStrategy` en `application/strategy/` (ver modelo en `StatementExportStrategy.java`)
2. Descomentar el bloque `OFXTests` en `StatementExportStrategyTest`
3. Agregar al smoke test en `ExportResultContractTest`

## Especificación

- **Content-type:** `application/x-ofx`
- **Filename:** `movimientos_{accountId}.ofx`
- **Encoding:** ISO-8859-1 (Latin-1) — estándar OFX heredado, **no cambiar a UTF-8**
- **Formato:** derivado de SGML — tags de valor **sin cierre**
- **Header:** respetar exactamente el formato OFX (OFXHEADER:100, DATA:OFXSGML, etc.)
- **TRNTYPE:** implementar heurística `resolveOFXType()` según descripción de la transacción

## Archivos clave

- `src/main/java/com/andbank/exporter/application/strategy/StatementExportStrategy.java` — estructura completa del OFX en Javadoc
- `src/main/java/com/andbank/exporter/domain/model/ExportResult.java`
- `src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java` — bloque `OFXTests`

## Regla de oro

Todo strategy retorna `ExportResult` con el contenido como `byte[]` y el `ContentType` correspondiente. **Nunca escribir a disco** — eso es responsabilidad del adapter de salida (`StatementExportController`).
