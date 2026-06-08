# Diagramas de Solución — task-1xlsx (XLSX Export Strategy)

Diagramas técnicos de la solución propuesta para implementar `XlsxExportStrategy` en el kata-exporter.

---

## 1. Diagrama de Secuencia

Muestra el flujo completo desde la petición HTTP del cliente hasta la descarga del archivo `.xlsx`, incluyendo la agrupación mensual interna de la strategy.

```mermaid
sequenceDiagram
    actor Cliente as Cliente (curl / browser)
    participant Controller as StatementExportController
    participant Service as StatementExportService
    participant AccRepo as AccountRepository
    participant CustRepo as CustomerRepository
    participant TxRepo as TransactionRepository
    participant Strategy as XlsxExportStrategy
    participant POI as Apache POI (XSSFWorkbook)
    participant BAOS as ByteArrayOutputStream

    Cliente->>Controller: GET /api/v1/accounts/{accountId}/statement/export?format=XLSX&dateFrom=...&dateTo=...

    Controller->>Service: execute(StatementExportRequest)

    Service->>AccRepo: findById(accountId)
    AccRepo-->>Service: Optional<Account>
    alt cuenta no encontrada
        Service-->>Controller: throw ExportException(404)
        Controller-->>Cliente: 404 Not Found
    end

    Service->>CustRepo: findByAccountId(accountId)
    CustRepo-->>Service: Optional<Customer>

    Service->>TxRepo: findByAccountIdAndDateRange(accountId, from, to)
    TxRepo-->>Service: List<Transaction>

    Service->>Strategy: export(transactions, account, customer)

    Note over Strategy: Agrupar transacciones por YearMonth
    Strategy->>Strategy: groupByMonth(transactions)<br/>→ Map<YearMonth, List<Transaction>>

    alt sin transacciones en el rango
        Strategy->>POI: workbook.createSheet("sin-movimientos")
        Strategy->>POI: sheet.createRow(0) → mensaje informativo
    else con transacciones
        loop Por cada YearMonth (ordenado cronológicamente)
            Strategy->>POI: workbook.createSheet("YYYY-MM")
            Strategy->>POI: sheet.createRow(0) → fila de encabezado
            loop Por cada Transaction del mes
                Strategy->>POI: sheet.createRow(n) → fila de datos
            end
        end
    end

    Strategy->>BAOS: workbook.write(outputStream)
    Strategy->>POI: workbook.close()
    Strategy-->>Service: ExportResult.of(bytes, contentType, filename)

    Service-->>Controller: ExportResult

    Controller-->>Cliente: 200 OK<br/>Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet<br/>Content-Disposition: attachment; filename="movimientos-{accountId}.xlsx"<br/>Body: byte[]
```

---

## 2. Diagrama de Clases

Muestra la estructura estática de los componentes involucrados, sus relaciones, atributos y métodos relevantes para la solución.

```mermaid
classDiagram
    direction TB

    class StatementExportStrategy {
        <<interface>>
        +supportedFormat() ExportFormat
        +export(List~Transaction~, Account, Customer) ExportResult
    }

    class XlsxExportStrategy {
        <<Component>>
        -CONTENT_TYPE : String
        +supportedFormat() ExportFormat
        +export(List~Transaction~, Account, Customer) ExportResult
        -groupByMonth(List~Transaction~) Map~YearMonth, List~Transaction~~
        -buildSheet(XSSFSheet, List~Transaction~) void
        -createHeaderRow(XSSFSheet) void
        -createDataRow(XSSFSheet, int, Transaction) void
        -createEmptySheetMessage(XSSFWorkbook) void
    }

    class ExportFormat {
        <<enumeration>>
        CSV
        PDF
        OFX
        JSON
        XLSX
    }

    class ExportResult {
        -content : byte[]
        -contentType : String
        -filename : String
        +of(byte[], String, String) ExportResult$
        +getContent() byte[]
        +getContentType() String
        +getFilename() String
        +size() int
    }

    class StatementExportService {
        <<Service>>
        -strategyMap : Map~ExportFormat, StatementExportStrategy~
        +execute(StatementExportRequest) ExportResult
        -resolveStrategy(ExportFormat) StatementExportStrategy
    }

    class Transaction {
        -id : String
        -accountId : String
        -type : TransactionType
        -amount : BigDecimal
        -currency : String
        -balanceAfter : BigDecimal
        -description : String
        -reference : String
        -transactionDate : LocalDateTime
    }

    class Account {
        -id : String
        -alias : String
        -currency : String
    }

    class Customer {
        -id : String
        -firstName : String
        -lastName : String
    }

    XlsxExportStrategy ..|> StatementExportStrategy : implements
    XlsxExportStrategy ..> ExportFormat : returns XLSX
    XlsxExportStrategy ..> ExportResult : creates via of()
    XlsxExportStrategy ..> Transaction : reads fields
    XlsxExportStrategy ..> Account : reads id for filename
    XlsxExportStrategy ..> Customer : reads name (opcional header)
    StatementExportService o-- StatementExportStrategy : strategyMap
    StatementExportService ..> ExportResult : returns
```

---

## 3. Diagrama de Flujo Interno — Agrupación mensual

Ilustra la lógica de agrupación y construcción del workbook dentro de `XlsxExportStrategy.export()`.

```mermaid
flowchart TD
    A([Inicio: export llamado]) --> B[Crear XSSFWorkbook vacío]
    B --> C[groupByMonth: agrupar transactions por YearMonth]
    C --> D{¿Hay meses con\ntransacciones?}

    D -- No --> E[Crear hoja 'sin-movimientos'\ncon fila de mensaje informativo]
    E --> J

    D -- Sí --> F[Ordenar meses cronológicamente\nascendente]
    F --> G[Para cada YearMonth]

    G --> H[createSheet con nombre 'YYYY-MM']
    H --> I1[createHeaderRow: fecha, referencia,\ndescripcion, tipo, importe,\nmoneda, saldo_posterior]
    I1 --> I2[Para cada Transaction del mes]
    I2 --> I3[createDataRow con valores de la transacción]
    I3 --> I4{¿Más transacciones\nen este mes?}
    I4 -- Sí --> I2
    I4 -- No --> I5{¿Más meses?}
    I5 -- Sí --> G
    I5 -- No --> J

    J[workbook.write a ByteArrayOutputStream] --> K[workbook.close]
    K --> L[ExportResult.of\nbytes, contentType, filename]
    L --> M([Retornar ExportResult])
```
