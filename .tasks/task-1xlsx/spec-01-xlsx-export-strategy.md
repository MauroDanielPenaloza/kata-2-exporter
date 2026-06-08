# Descripción de User Story

Este slice implementa el cuarto formato de exportación del kata-exporter: **XLSX (Excel)**. Encaja en la épica de exportación multi-formato como el único corte vertical pendiente del Módulo 3. La funcionalidad debe integrarse sin modificar el service ni el controller existentes, respetando el contrato del patrón Strategy definido en `StatementExportStrategy`.

El valor diferenciador respecto a CSV es la **agrupación visual por mes**: cada mes de transacciones ocupa su propia hoja dentro del libro Excel, lo que facilita el análisis financiero mensual en una herramienta ampliamente conocida por el cliente bancario.

---

# Story (INVEST)

**Como** cliente bancario que gestiona sus finanzas personales con Excel,  
**quiero** descargar el extracto de mi cuenta en formato XLSX con los movimientos organizados en hojas separadas por mes,  
**para poder** analizar mis ingresos y gastos de cada mes en una hoja de cálculo familiar sin necesidad de filtrar manualmente.

---

# Análisis

## Approach

Corte vertical completo (Walking Skeleton) que habilita el formato XLSX de extremo a extremo:

1. Descomentar la dependencia `poi-ooxml` en `pom.xml`.
2. Agregar el valor `XLSX` al enum `ExportFormat`.
3. Crear `XlsxExportStrategy` anotada con `@Component`, implementando `StatementExportStrategy`.
4. La lógica de agrupación agrupa `List<Transaction>` por `YearMonth` usando `transactionDate`.
5. Por cada mes, crear una hoja (`XSSFSheet`) nombrada con el patrón `YYYY-MM` (ej: `2024-01`).
6. Cada hoja tiene una fila de encabezado y una fila por transacción.
7. El workbook se serializa en un `ByteArrayOutputStream` y se envuelve en `ExportResult`.
8. Si no hay transacciones en el rango, crear una hoja `sin-movimientos` con una fila de mensaje informativo (decisión **R2-B**).

**Decisiones de diseño cerradas** *(ver [`open-questions.md`](./open-questions.md)):*

| ID | Decisión | Detalle aplicado |
|----|----------|------------------|
| B1 | **A** | Una hoja (`XSSFSheet`) por mes, nombrada `YYYY-MM` |
| B2 | **A** | Columnas idénticas al CSV: `fecha`, `referencia`, `descripcion`, `tipo`, `importe`, `moneda`, `saldo_posterior` |
| R1 | **A** | Filename: `movimientos-{accountId}.xlsx` |
| R2 | **B** | Sin transacciones: hoja `sin-movimientos` con mensaje informativo |
| M1 | **A** | Clase: `XlsxExportStrategy` (convención Java; ajustar test al descomentar) |

**Alcance explícito de este slice:**
- Una hoja por mes, ordenadas cronológicamente.
- Columnas: `fecha`, `referencia`, `descripcion`, `tipo`, `importe`, `moneda`, `saldo_posterior`.
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.
- Filename: `movimientos-{accountId}.xlsx`.
- Sin transacciones: hoja única `sin-movimientos` con mensaje informativo (sin filas de datos).

**Fuera de alcance de este slice:**
- Estilos de celda (negrita, colores, bordes).
- Ancho automático de columnas (`autoSizeColumn`).
- Hoja de resumen consolidado.
- Fórmulas Excel (totales).

## API Controller

El endpoint existente ya soporta XLSX; solo requiere que `XLSX` sea un valor válido del enum para que el binding del parámetro `format` lo acepte.

| Método | Ruta | Propósito |
|--------|------|-----------|
| `GET` | `/api/v1/accounts/{accountId}/statement/export?format=XLSX&dateFrom={yyyy-MM-dd}&dateTo={yyyy-MM-dd}` | Descarga extracto en formato Excel con hojas mensuales |

**Ejemplo de request:**

```bash
curl -o extracto.xlsx \
  "http://localhost:8080/api/v1/accounts/a001/statement/export?format=XLSX&dateFrom=2024-01-01&dateTo=2024-03-31"
```

**Respuesta exitosa (`200 OK`):**

```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="movimientos-a001.xlsx"
```

Body: bytes del archivo `.xlsx` (ZIP válido con firma `PK` — `0x50 0x4B`).

**Respuestas de error (sin cambios respecto al controller actual):**

| Código | Condición |
|--------|-----------|
| `404 Not Found` | Cuenta no encontrada |
| `400 Bad Request` | Formato inválido / parámetros faltantes o `dateFrom > dateTo` |
| `500 Internal Server Error` | Error de generación del workbook (propagado como `ExportException`) |

---

# Punto de Arranque

| | |
|--|--|
| **Proyecto** | `kata-exporter` |
| **Clases a crear** | `src/main/java/com/andbank/exporter/application/strategy/XlsxExportStrategy.java` |
| **Clases a modificar** | `src/main/java/com/andbank/exporter/domain/model/ExportFormat.java` — descomentar `XLSX` |
| **pom.xml** | Descomentar bloque `poi-ooxml` (líneas 99–106) |
| **Tests a descomentar** | `src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java` — bloque `XLSXTests`; cambiar `new XLSXExportStrategy()` por `new XlsxExportStrategy()` (decisión **M1-A**) |
| **Strategy a registrar** | `@Component` — Spring lo descubre automáticamente, sin tocar `StatementExportService` |
| **Dependencias de dominio** | `Transaction.getTransactionDate()` (para agrupar) · `Transaction.getAmount()` · `Transaction.getType()` · `Account.getId()` |

---

# Contexto

- Requerimiento original: [`raw-requirement.md`](./raw-requirement.md)
- Contrato del strategy: `src/main/java/com/andbank/exporter/application/strategy/StatementExportStrategy.java`
- Contrato de salida: `src/main/java/com/andbank/exporter/domain/model/ExportResult.java`
- Patrón de extensión documentado: [`PATTERNS.md §A`](../../PATTERNS.md#patrón-a--agregar-un-nuevo-formato-de-exportación)
- Decisiones cerradas: [`open-questions.md`](./open-questions.md)
- Diagramas técnicos: [`solution-diagrams.md`](./solution-diagrams.md)

---

# Criterio de Aceptación

### Escenario 1 — Happy path: exportación con múltiples meses

**Dado que** existe la cuenta `a001` con transacciones en enero, febrero y marzo de 2024  
**Cuando** el cliente solicita `GET /api/v1/accounts/a001/statement/export?format=XLSX&dateFrom=2024-01-01&dateTo=2024-03-31`  
**Entonces** la respuesta tiene código `200 OK`  
**Y** el `Content-Type` es `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`  
**Y** el header `Content-Disposition` contiene `attachment; filename="movimientos-a001.xlsx"`  
**Y** el archivo devuelto es un ZIP válido (magic bytes `0x50 0x4B`)  
**Y** el workbook contiene exactamente 3 hojas nombradas `2024-01`, `2024-02` y `2024-03`  
**Y** cada hoja contiene una fila de encabezado con las columnas: `fecha`, `referencia`, `descripcion`, `tipo`, `importe`, `moneda`, `saldo_posterior`  
**Y** la cantidad de filas de datos en cada hoja coincide con la cantidad de transacciones de ese mes

---

### Escenario 2 — Happy path: exportación de un único mes

**Dado que** existe la cuenta `a001` con transacciones solo en enero 2024 dentro del rango solicitado  
**Cuando** el cliente solicita `GET /api/v1/accounts/a001/statement/export?format=XLSX&dateFrom=2024-01-01&dateTo=2024-01-31`  
**Entonces** la respuesta tiene código `200 OK`  
**Y** el workbook contiene exactamente 1 hoja nombrada `2024-01`  
**Y** la hoja contiene las 5 transacciones del mes de enero de los fixtures

---

### Escenario 3 — Happy path: cuenta con cero transacciones en el rango

**Dado que** existe la cuenta `a004` (Luciana Gómez) que no tiene transacciones  
**Cuando** el cliente solicita `GET /api/v1/accounts/a004/statement/export?format=XLSX&dateFrom=2024-01-01&dateTo=2024-03-31`  
**Entonces** la respuesta tiene código `200 OK`  
**Y** el header `Content-Disposition` contiene `attachment; filename="movimientos-a004.xlsx"`  
**Y** el workbook contiene exactamente 1 hoja nombrada `sin-movimientos`  
**Y** la hoja `sin-movimientos` contiene una fila con un mensaje informativo (ej: `No se encontraron movimientos en el rango solicitado`)  
**Y** no existen filas de datos de transacciones  
**Y** el tamaño del archivo es mayor que 0 bytes

---

### Escenario 4 — Unhappy path: cuenta inexistente

**Dado que** no existe ninguna cuenta con ID `a999`  
**Cuando** el cliente solicita `GET /api/v1/accounts/a999/statement/export?format=XLSX&dateFrom=2024-01-01&dateTo=2024-03-31`  
**Entonces** la respuesta tiene código `404 Not Found`

---

### Escenario 5 — Unhappy path: parámetro `format` inválido

**Dado que** el endpoint existe y la cuenta `a001` existe  
**Cuando** el cliente solicita `GET /api/v1/accounts/a001/statement/export?format=EXCEL&dateFrom=2024-01-01&dateTo=2024-03-31`  
**Entonces** la respuesta tiene código `400 Bad Request`

---

### Escenario 6 — Unhappy path: rango de fechas invertido

**Dado que** el endpoint existe y la cuenta `a001` existe  
**Cuando** el cliente solicita `GET /api/v1/accounts/a001/statement/export?format=XLSX&dateFrom=2024-03-31&dateTo=2024-01-01`  
**Entonces** la respuesta tiene código `400 Bad Request` o `500 Internal Server Error`  
*(según el manejo actual de `StatementExportRequest.build()` que lanza `IllegalStateException`)*

---

### Escenario 7 — Contrato del strategy: content-type y magic bytes

**Dado que** se invoca directamente `XlsxExportStrategy.export(transactions, account, customer)` con los fixtures de `TestFixtures`  
**Cuando** se evalúa el `ExportResult` retornado  
**Entonces** `result.getContentType()` es igual a `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`  
**Y** `result.getFilename()` termina en `.xlsx`  
**Y** `result.size()` es mayor a `1024` bytes  
**Y** los primeros dos bytes del contenido son `0x50` y `0x4B` (firma ZIP/OOXML)

---

# Test Plan

## Contexto

| Elemento | Detalle |
|---------|---------|
| Fixture de cuenta | `TestFixtures.anAccount()` — cuenta `a001`, alias `morales.ahorro.pesos`, moneda ARS |
| Fixture de cliente | `TestFixtures.aCustomer()` — Valentina Morales |
| Fixture de transacciones | `TestFixtures.someTransactions()` — 5 transacciones, todas de enero 2024 |
| Transacciones de integración | Cuenta `a001`, rango `2024-01-01 / 2024-03-31` → 20 transacciones en 3 meses |
| Ambiente | H2 in-memory; levantar con `mvn spring-boot:run` |

## Escenarios de test

| # | Tipo | Clase de test | Descripción | Resultado esperado |
|---|------|--------------|-------------|-------------------|
| 1 | Contrato unitario | `StatementExportStrategyTest.XLSXTests` | `supportedFormat()` retorna `ExportFormat.XLSX` | `assertThat(strategy.supportedFormat()).isEqualTo(ExportFormat.XLSX)` |
| 2 | Contrato unitario | `StatementExportStrategyTest.XLSXTests` | Content-type correcto | Igual a `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| 3 | Contrato unitario | `StatementExportStrategyTest.XLSXTests` | Magic bytes ZIP (`PK`) | `content[0] == 0x50` y `content[1] == 0x4B` |
| 4 | Contrato unitario | `StatementExportStrategyTest.XLSXTests` | Tamaño mínimo razonable | `result.size() > 1024` |
| 5 | Contrato unitario | `StatementExportStrategyTest.XLSXTests` | Filename termina en `.xlsx` | `result.getFilename().endsWith(".xlsx")` |
| 6 | Integración manual | `curl` / Postman | Descarga real con cuenta `a001` Q1 2024 | Archivo Excel abre en LibreOffice/Excel; contiene 3 hojas (Ene, Feb, Mar) |
| 7 | Contrato unitario | `StatementExportStrategyTest.XLSXTests` | Lista vacía de transacciones | Workbook con hoja `sin-movimientos` y mensaje informativo; `result.size() > 0` |
| 8 | Regresión | `StatementExportStrategyTest` (todos) | No romper strategies anteriores | Todos los otros bloques siguen comentados o en verde |

---

# Validación post implementación

1. `mvn clean compile` — debe compilar sin errores (valida que POI está en classpath y el enum tiene `XLSX`).
2. `mvn test` — el bloque `XLSXTests` en `StatementExportStrategyTest` debe estar en verde.
3. `mvn spring-boot:run` — la aplicación debe arrancar e imprimir en el log: `StatementExportService iniciado con N strategies: [..., XLSX]`.
4. Ejecutar el curl de prueba:
   ```bash
   curl -o extracto.xlsx \
     "http://localhost:8080/api/v1/accounts/a001/statement/export?format=XLSX&dateFrom=2024-01-01&dateTo=2024-03-31"
   ```
5. Abrir `extracto.xlsx` con Excel o LibreOffice Calc y verificar visualmente:
   - 3 hojas con nombres `2024-01`, `2024-02`, `2024-03`.
   - Encabezado de columnas en la fila 1 de cada hoja.
   - Filas de datos que corresponden a las transacciones del mes.
6. Probar cuenta sin movimientos:
   ```bash
   curl -o extracto-vacio.xlsx \
     "http://localhost:8080/api/v1/accounts/a004/statement/export?format=XLSX&dateFrom=2024-01-01&dateTo=2024-03-31"
   ```
   Verificar que el archivo contiene una hoja `sin-movimientos` con mensaje informativo y sin filas de transacciones.

---

# Non-functional notes

| NFR | Descripción |
|-----|-------------|
| **Generación en memoria** | El workbook debe generarse completamente en `ByteArrayOutputStream`; está prohibido escribir en disco desde el strategy (regla de oro del contrato `StatementExportStrategy`). |
| **Sin dependencias HTTP** | `XlsxExportStrategy` no puede importar ninguna clase de `javax.servlet`, `jakarta.servlet` ni de Spring MVC. |
| **Independencia del service** | La clase debe ser auto-descubierta por Spring vía `@Component`; no se debe modificar `StatementExportService`. |
| **Compatibilidad de versión** | Usar Apache POI `5.2.5` (`poi-ooxml`) ya declarado en `pom.xml` como propiedad `${poi.version}`. |
| **Formato de celdas numéricas** | Los importes deben almacenarse como valores numéricos (`setCellValue(double)`) para permitir fórmulas futuras, no como strings. |
