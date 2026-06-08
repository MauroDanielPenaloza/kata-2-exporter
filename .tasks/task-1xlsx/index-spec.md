# task-1xlsx — Índice de Especificaciones

> **Épica:** Agregar exportación XLSX al kata-exporter (Módulo 3).  
> **Objetivo:** Generar un archivo Excel en memoria con transacciones agrupadas por mes y retornarlo al cliente mediante el endpoint existente.

---

## Artefactos fuente

| Archivo | Propósito |
|---------|-----------|
| `raw-requirement.md` | Requerimiento original del módulo 3 |
| `spec-01-xlsx-export-strategy.md` | Historia principal — implementar `XlsxExportStrategy` con agrupación mensual |
| `open-questions.md` | Registro de decisiones de diseño — **todas resueltas** |
| `solution-diagrams.md` | Diagramas técnicos de la solución (secuencia, clases, flujo interno) |

---

## Orden de ejecución

| Orden | Archivo spec | Resumen | Depende de |
|-------|-------------|---------|------------|
| 01 | `spec-01-xlsx-export-strategy.md` | Implementar `XlsxExportStrategy` con agrupación mensual, habilitar Apache POI y registrar `XLSX` en el enum | — |

---

## Mapeo de componentes clave

| Componente | Acción requerida | Ruta desde raíz |
|------------|-----------------|-----------------|
| `ExportFormat` enum | Descomentar / agregar valor `XLSX` | `src/main/java/com/andbank/exporter/domain/model/ExportFormat.java` |
| `pom.xml` | Descomentar dependencia `poi-ooxml` v5.2.5 | `pom.xml` |
| `XlsxExportStrategy` | Crear clase nueva `@Component` | `src/main/java/com/andbank/exporter/application/strategy/XlsxExportStrategy.java` |
| `StatementExportStrategyTest` | Descomentar bloque `XLSXTests` y renombrar `XLSXExportStrategy` → `XlsxExportStrategy` en el test | `src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java` |
| `ExportResultContractTest` | Agregar aserción de tamaño para XLSX | `src/test/java/com/andbank/exporter/application/strategy/ExportResultContractTest.java` |

---

## Backbone — Incrementos sugeridos (Story Mapping)

| Franja | Descripción | Tipo |
|--------|-------------|------|
| **Slice 1 — Walking Skeleton (MVP)** | `XlsxExportStrategy` funcional: habilitar POI, agregar enum, crear strategy con agrupación por mes (una hoja por mes), retornar `ExportResult` válido. Tests de contrato en verde. | **Este sprint** |
| **Slice 2 — Make it better** | Estilos de celda: encabezado en negrita, columnas con ancho automático (`autoSizeColumn`), formato de fecha legible, totales por hoja. | Mejora futura |
| **Slice 3 — Make it releasable** | Hoja resumen con totales consolidados de todos los meses (créditos, débitos, balance). | Mejora futura |

---

## Decisiones de diseño cerradas

| ID | Decisión | Resumen |
|----|----------|---------|
| B1 | A | Una hoja por mes (`YYYY-MM`) |
| B2 | A | Columnas iguales al CSV |
| R1 | A | `movimientos-{accountId}.xlsx` |
| R2 | B | Hoja `sin-movimientos` con mensaje informativo si no hay transacciones |
| M1 | A | Clase `XlsxExportStrategy` |

---

## Notas de contexto

- El `StatementExportService` descubre automáticamente cualquier `@Component` que implemente `StatementExportStrategy`; **no requiere modificación**.
- El `StatementExportController` ya soporta retornar `ResponseEntity<byte[]>` con `Content-Disposition: attachment`; **no requiere modificación**.
- Apache POI versión `5.2.5` ya está declarada como propiedad `<poi.version>` en `pom.xml`; solo hay que descomentar la dependencia `poi-ooxml`.
