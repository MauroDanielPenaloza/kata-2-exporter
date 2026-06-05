# Kata — Exportador de Movimientos Bancarios

Proyecto base para la capacitación IA — Módulos 3, 4 y 5.

---

## Cómo levantar el proyecto

```bash
mvn spring-boot:run
```

- App: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:andbank`
  - User: `sa` / Password: *(vacío)*

---

## Datos disponibles en la BD

| Account ID | Alias                | Moneda | Titular           | Transacciones        |
|------------|----------------------|--------|-------------------|----------------------|
| `a001`     | morales.ahorro.pesos | ARS    | Valentina Morales | 20 (Ene–Mar 2024)    |
| `a002`     | morales.usd          | USD    | Valentina Morales | 3                    |
| `a003`     | fernandez.cuenta     | ARS    | Rodrigo Fernández | 4                    |
| `a004`     | gomez.ahorro         | ARS    | Luciana Gómez     | 0                    |

---

## Endpoints de exploración

```bash
# Info general de la BD
curl http://localhost:8080/api/v1/info

# Ver todas las cuentas
curl http://localhost:8080/api/v1/accounts

# Ver movimientos de una cuenta
curl http://localhost:8080/api/v1/accounts/a001/transactions
```

---

## Endpoint de exportación

```
GET /api/v1/accounts/{accountId}/statement/export
  ?format=CSV|PDF|OFX|JSON
  &dateFrom=YYYY-MM-DD
  &dateTo=YYYY-MM-DD
```

> **Nota:** el endpoint está conectado pero devuelve error 400 hasta que
> implementes al menos un strategy.

---

## Arquitectura hexagonal

```
domain/
  model/
    Transaction.java            ← entidad de dominio pura
    Account.java
    Customer.java
    ExportFormat.java           ← enum: CSV, PDF, OFX, JSON  ← agregar XLSX aquí
    ExportResult.java           ← contrato de output (byte[] + ContentType)
    StatementExportRequest.java ← input del use case
  port/
    in/  ExportStatementUseCase.java      ← lo llama el REST adapter
    out/ TransactionRepository.java       ← lo implementa la infraestructura
         AccountRepository.java
         CustomerRepository.java

application/
  strategy/
    StatementExportStrategy.java    ← interfaz + modelos de todas las implementaciones
    CSVExportStrategy.java          ← TODO implementar
    PDFExportStrategy.java          ← TODO implementar
    OFXExportStrategy.java          ← TODO implementar (Módulo 4)
    JSONExportStrategy.java         ← TODO implementar
    XLSXExportStrategy.java         ← TODO Módulo 3
  service/
    StatementExportService.java     ← orquesta el strategy, ya implementado

infrastructure/
  adapter/
    in/rest/
      StatementExportController.java    ← ya implementado, escribe a HTTP
      DataExplorerController.java       ← utilidad para explorar la BD
    out/persistence/
      PersistenceAdapter.java           ← ya implementado
      entity/   (JPA entities)
      repository/ (Spring Data)
      mapper/   (Entity → Domain)
```# kata-2-exporter
# kata-2-exporter
