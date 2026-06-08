# Decision Log — task-1xlsx

Registro de decisiones, bloqueos y ambigüedades detectados al explorar el requerimiento de exportación XLSX.  
Marcar la opción elegida con `[x]` y completar **Decisión:** con la letra seleccionada.  
Si ninguna opción encaja, completar `Otra:` con la respuesta correcta.

---

## Blockers — deben resolverse antes de implementar

---

### B1 — spec-01: Estructura interna del Excel para la agrupación mensual

**Contexto:** El requerimiento indica "transacciones agrupadas por mes" pero no especifica la estructura visual dentro del archivo Excel. La elección impacta directamente en la API de Apache POI a usar y en la experiencia del usuario final al abrir el archivo.

**Opciones:**

- [x] **A)** Una hoja (`XSSFSheet`) por mes, nombrada `YYYY-MM` (ej: `2024-01`, `2024-02`). Máxima claridad visual; el usuario navega entre hojas. Es la opción más común en extractos bancarios Excel.
- [ ] **B)** Una única hoja con filas separadoras de encabezado de mes (fila de título `=== Enero 2024 ===` seguida de las transacciones). Más simple de implementar, pero dificulta el filtrado nativo de Excel.
- [ ] **C)** Una única hoja con una columna adicional `mes` (formato `YYYY-MM`) que agrupa visualmente. Compatible con tablas dinámicas de Excel; el usuario filtra por esa columna.

- [ ] **Otra:** ______

**Decisión:** A  
**Notas:** La spec-01 asume la opción **A** como default para los criterios de aceptación. Si se elige B o C, los escenarios 1 y 2 de AC deben ajustarse.

---

### B2 — spec-01: Columnas a incluir en cada hoja

**Contexto:** El requerimiento no explicita qué columnas debe tener el Excel. Usar las mismas que CSV mantiene consistencia entre formatos, pero el formato Excel permite columnas adicionales sin penalizar la legibilidad. Decidir ahora evita rework en los tests de contenido.

**Opciones:**

- [x] **A)** Mismas columnas que CSV: `fecha`, `referencia`, `descripcion`, `tipo`, `importe`, `moneda`, `saldo_posterior`. Consistencia máxima entre formatos.
- [ ] **B)** Columnas de A más `id_transaccion`. Facilita la trazabilidad; útil para conciliaciones.
- [ ] **C)** Columnas enriquecidas: `fecha`, `referencia`, `descripcion`, `tipo`, `importe`, `moneda`, `saldo_posterior`, `id_cuenta`. Agrega contexto de la cuenta en cada fila.

- [ ] **Otra:** ______

**Decisión:** A  
**Notas:** La spec-01 asume la opción **A** para mantener paridad con el CSV ya implementado.

---

## Risks — pueden causar retrabajo si no se resuelven

---

### R1 — spec-01: Convención del nombre del archivo generado (`filename`)

**Contexto:** El requerimiento no especifica el nombre del `.xlsx`. Los otros formatos siguen convenciones distintas entre sí (no hay estándar documentado en el proyecto). Un nombre inconsistente puede dificultar la automatización de descargas del lado del cliente.

**Opciones:**

- [x] **A)** `movimientos-{accountId}.xlsx` — simple, sin rango de fechas. Ejemplo: `movimientos-a001.xlsx`.
- [ ] **B)** `statement-{accountId}-{dateFrom}-{dateTo}.xlsx` — incluye rango. Ejemplo: `statement-a001-2024-01-01-2024-03-31.xlsx`. Consistente si los otros formatos siguen este patrón.
- [ ] **C)** `extracto-{accountId}-{YYYYMM}.xlsx` — usa el mes de generación en lugar del rango. Solo aplica si el rango siempre cubre un único mes.

- [ ] **Otra:** ______

**Decisión:** A  
**Notas:** La spec-01 usa `movimientos-{accountId}.xlsx` (opción A) como valor provisional para no bloquear el test de contrato `filename.endsWith(".xlsx")`.

---

### R2 — spec-01: Comportamiento cuando el rango contiene cero transacciones

**Contexto:** El requerimiento no especifica qué debe retornar el strategy cuando no hay transacciones en el rango (`a004` o cualquier cuenta sin movimientos). Las opciones producen archivos Excel semánticamente distintos. Si el controller o el cliente asume siempre al menos una hoja, un workbook completamente vacío puede causar errores al abrirse en algunas versiones de Excel.

**Opciones:**

- [ ] **A)** Retornar un workbook sin hojas. Técnicamente válido para OOXML, pero Excel puede mostrar advertencia al abrirlo.
- [x] **B)** Retornar un workbook con una hoja vacía llamada `sin-movimientos` y una fila de mensaje informativo. Comportamiento más amigable para el usuario final.
- [ ] **C)** Retornar un workbook con una hoja vacía llamada `datos` solo con la fila de encabezado de columnas. Consistente con la estructura de las hojas con datos.

- [ ] **Otra:** ______

**Decisión:** B  
**Notas:** El escenario 3 del AC requiere que `result.size() > 0` independientemente de la opción elegida.

---

## Inconsistencias menores

---

### M1 — spec-01: Nombre de la clase strategy

**Contexto:** El test comentado en `StatementExportStrategyTest` usa `XLSXExportStrategy` (todo en mayúsculas antes de `Strategy`), pero la convención de Java para siglas en nombres de clase es `XlsxExportStrategy` (solo primera letra mayúscula). Una discrepancia aquí obligará a ajustar el test comentado al descomentarlo.

**Opciones:**

- [x] **A)** `XlsxExportStrategy` — sigue la convención Java estándar (Google Style Guide / Oracle naming conventions).
- [ ] **B)** `XLSXExportStrategy` — mantiene exactamente el nombre usado en el test comentado; no requiere editar el test.

- [ ] **Otra:** ______

**Decisión:** A  
**Notas:** Si se elige A, editar la línea `new XLSXExportStrategy()` del test al descomentarlo. Si se elige B, ajustar la especificación.

---

## Resumen

| ID | Spec | Impacto | Descripción corta | Decisión | Estado |
|----|------|---------|-------------------|----------|--------|
| B1 | 01 | Blocker | Estructura del Excel: hojas por mes vs. una hoja con separadores vs. columna de mes | A | ✅ Cerrado |
| B2 | 01 | Blocker | Columnas a incluir en cada fila de transacción | A | ✅ Cerrado |
| R1 | 01 | Risk | Convención del nombre del archivo `.xlsx` | A | ✅ Cerrado |
| R2 | 01 | Risk | Comportamiento del workbook cuando no hay transacciones | B | ✅ Cerrado |
| M1 | 01 | Minor | Nombre de la clase: `XlsxExportStrategy` vs. `XLSXExportStrategy` | A | ✅ Cerrado |

> Todas las decisiones están reflejadas en `spec-01-xlsx-export-strategy.md`, `index-spec.md` y `solution-diagrams.md`.

