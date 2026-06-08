# Requerimiento — JSON Export Strategy

Implementar `JSONExportStrategy`.

## Objetivo

Crear el strategy de exportación JSON con estructura anidada (account, customer, transactions, summary).

## Checklist

1. Crear `JSONExportStrategy` en `application/strategy/` (ver modelo en `StatementExportStrategy.java`)
2. Descomentar el bloque `JSONTests` en `StatementExportStrategyTest`
3. Agregar al smoke test en `ExportResultContractTest`

## Especificación

- **Content-type:** `application/json; charset=UTF-8`
- **Filename:** `movimientos_{accountId}.json`
- **Serialización:** Jackson `ObjectMapper` con `INDENT_OUTPUT` y `JavaTimeModule`
- **Estructura raíz:** `account`, `customer`, `transactions`, `summary`
- **Summary:** incluir `count`, `totalCredits`, `totalDebits`, `netBalance`, `generatedAt`

## Archivos clave

- `src/main/java/com/andbank/exporter/application/strategy/StatementExportStrategy.java` — modelo JSON completo en Javadoc
- `src/main/java/com/andbank/exporter/domain/model/ExportResult.java`
- `src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java` — bloque `JSONTests`

## Regla de oro

Todo strategy retorna `ExportResult` con el contenido como `byte[]` y el `ContentType` correspondiente. **Nunca escribir a disco** — eso es responsabilidad del adapter de salida (`StatementExportController`).
