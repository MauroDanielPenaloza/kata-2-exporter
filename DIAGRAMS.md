# Diagramas de arquitectura

Este documento contiene los diagramas Mermaid de referencia del proyecto **kata-exporter**.  
Para el contexto arquitectónico y las clases involucradas, ver [ARCHITECTURE.md](ARCHITECTURE.md).

---

## 1. Arquitectura hexagonal

Vista de capas: adaptadores de entrada, núcleo (dominio + aplicación) y adaptadores de salida.

```mermaid
flowchart TB
    subgraph driving [Adaptadores de entrada]
        REST["StatementExportController"]
        Explorer["DataExplorerController"]
    end
    subgraph core [Nucleo]
        subgraph app [application]
            Service["StatementExportService"]
            Strategy["StatementExportStrategy (interfaz)"]
        end
        subgraph domain [domain]
            PortIn["ExportStatementUseCase (puerto entrada)"]
            PortOut["*Repository (puertos salida)"]
            Models["Account / Customer / Transaction / ExportResult"]
        end
    end
    subgraph driven [Adaptadores de salida]
        Adapters["*PersistenceAdapter"]
        JPA["Jpa*Repository"]
        DB[("H2 in-memory")]
    end
    REST -->|"implementa"| PortIn
    Service -.->|"implements"| PortIn
    Service --> PortOut
    Service --> Strategy
    Strategy --> Models
    PortOut -.->|"implements"| Adapters
    Adapters --> JPA --> DB
```

**Notas:**

- `StatementExportController` delega al puerto de entrada `ExportStatementUseCase`, implementado por `StatementExportService`.
- `DataExplorerController` accede directamente a los repositorios JPA (excepción pragmática para explorar datos durante la kata).
- Los strategies de exportación viven en `application/strategy/` y operan solo sobre modelos de dominio.

---

## 2. Flujo de una exportación

Secuencia desde la petición HTTP hasta la respuesta con el archivo generado.

```mermaid
sequenceDiagram
    participant Client as Cliente HTTP
    participant Controller as StatementExportController
    participant UseCase as StatementExportService
    participant AccountRepo as AccountRepository
    participant CustomerRepo as CustomerRepository
    participant TxRepo as TransactionRepository
    participant Strategy as StatementExportStrategy
    participant Result as ExportResult

    Client->>Controller: GET /api/v1/accounts/{id}/statement/export
    Controller->>Controller: Construir StatementExportRequest
    Controller->>UseCase: execute(request)
    UseCase->>AccountRepo: findById(accountId)
    AccountRepo-->>UseCase: Account
    UseCase->>CustomerRepo: findByAccountId(accountId)
    CustomerRepo-->>UseCase: Customer
    UseCase->>TxRepo: findByAccountIdAndDateRange(...)
    TxRepo-->>UseCase: List Transaction
    UseCase->>Strategy: export(transactions, account, customer)
    Strategy->>Result: ExportResult.of(bytes, contentType, filename)
    Strategy-->>UseCase: ExportResult
    UseCase-->>Controller: ExportResult
    Controller->>Controller: Set Content-Type, Content-Disposition, Content-Length
    Controller-->>Client: 200 OK + byte[]
```

**Puntos clave del flujo:**

1. El controller traduce parámetros HTTP a `StatementExportRequest` (Builder).
2. El service valida existencia de cuenta y cliente, y recupera transacciones del rango.
3. El strategy correcto se resuelve por `ExportFormat` desde un mapa inyectado por Spring.
4. El strategy genera el contenido **en memoria** y retorna `ExportResult`.
5. Solo el controller escribe la respuesta HTTP (headers + body).
