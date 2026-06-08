# Arquitectura — kata-exporter

Referencia de arquitectura del exportador bancario multi-formato.  
Diagramas en [DIAGRAMS.md](DIAGRAMS.md) · Patrones de extensión en [PATTERNS.md](PATTERNS.md).

---

## 1. Objetivo de la aplicación

**kata-exporter** es una API REST que exporta movimientos bancarios de una cuenta en distintos formatos (CSV, PDF, OFX, JSON y XLSX planificado). Recibe un rango de fechas y un formato, consulta transacciones desde una base H2 en memoria y devuelve el archivo generado como respuesta HTTP. La arquitectura hexagonal separa dominio, aplicación e infraestructura; el patrón Strategy permite agregar formatos sin modificar el caso de uso principal.

---

## 2. Stack tecnológico

| Tecnología | Uso |
|------------|-----|
| Spring Boot 3.3 | Framework de aplicación |
| Java 17 | Lenguaje |
| H2 | Base de datos in-memory (desarrollo y pruebas) |
| Spring Data JPA | Persistencia |
| Lombok | Reducción de boilerplate |
| MapStruct | Mapeo Entity ↔ Domain |
| OpenCSV | Generación CSV |
| iText 8 | Generación PDF |
| Apache POI | Generación XLSX *(pendiente — dependencia comentada en `pom.xml`)* |

---

## 3. Diagramas

| Diagrama | Archivo | Descripción |
|----------|---------|-------------|
| Arquitectura hexagonal | [DIAGRAMS.md §1](DIAGRAMS.md#1-arquitectura-hexagonal) | Capas: adaptadores de entrada, núcleo y adaptadores de salida |
| Flujo de exportación | [DIAGRAMS.md §2](DIAGRAMS.md#2-flujo-de-una-exportación) | Secuencia HTTP → Controller → Service → Strategy → respuesta |

---

## 4. Estructura de paquetes

Base: `com.andbank.exporter`

### domain/model

| Clase | Path |
|-------|------|
| `Account` | `domain/model/Account.java` |
| `Customer` | `domain/model/Customer.java` |
| `Transaction` | `domain/model/Transaction.java` |
| `TransactionType` | `domain/model/TransactionType.java` |
| `ExportFormat` | `domain/model/ExportFormat.java` |
| `ExportResult` | `domain/model/ExportResult.java` |
| `StatementExportRequest` | `domain/model/StatementExportRequest.java` |

### domain/port/in

| Clase | Path |
|-------|------|
| `ExportStatementUseCase` | `domain/port/in/ExportStatementUseCase.java` |

### domain/port/out

| Clase | Path |
|-------|------|
| `AccountRepository` | `domain/port/out/AccountRepository.java` |
| `CustomerRepository` | `domain/port/out/CustomerRepository.java` |
| `TransactionRepository` | `domain/port/out/TransactionRepository.java` |

### application/service

| Clase | Path |
|-------|------|
| `StatementExportService` | `application/service/StatementExportService.java` |
| `ExportException` | `application/service/ExportException.java` |

### application/strategy

| Clase | Path |
|-------|------|
| `StatementExportStrategy` *(interfaz)* | `application/strategy/StatementExportStrategy.java` |
| `*ExportStrategy` *(implementaciones)* | `application/strategy/` — pendientes de implementar en la kata |

### infrastructure/adapter/in/rest

| Clase | Path |
|-------|------|
| `StatementExportController` | `infrastructure/adapter/in/rest/StatementExportController.java` |
| `DataExplorerController` | `infrastructure/adapter/in/rest/DataExplorerController.java` |
| `ExportRequestDTO` | `infrastructure/adapter/in/rest/dto/ExportRequestDTO.java` |

### infrastructure/adapter/out/persistence

| Clase | Path |
|-------|------|
| `AccountPersistenceAdapter` | `infrastructure/adapter/out/persistence/AccountPersistenceAdapter.java` |
| `CustomerPersistenceAdapter` | `infrastructure/adapter/out/persistence/CustomerPersistenceAdapter.java` |
| `TransactionPersistenceAdapter` | `infrastructure/adapter/out/persistence/TransactionPersistenceAdapter.java` |
| `AccountEntity` | `infrastructure/adapter/out/persistence/entity/AccountEntity.java` |
| `CustomerEntity` | `infrastructure/adapter/out/persistence/entity/CustomerEntity.java` |
| `TransactionEntity` | `infrastructure/adapter/out/persistence/entity/TransactionEntity.java` |
| `JpaAccountRepository` | `infrastructure/adapter/out/persistence/repository/JpaAccountRepository.java` |
| `JpaCustomerRepository` | `infrastructure/adapter/out/persistence/repository/JpaCustomerRepository.java` |
| `JpaTransactionRepository` | `infrastructure/adapter/out/persistence/repository/JpaTransactionRepository.java` |
| `PersistenceMapper` | `infrastructure/adapter/out/persistence/mapper/PersistenceMapper.java` |

### Bootstrap

| Clase | Path |
|-------|------|
| `KataExporterApplication` | `KataExporterApplication.java` |

---

## 5. Contratos clave

### StatementExportStrategy.export()

- Siempre retorna `ExportResult` no-null.
- Los campos `content`, `contentType` y `filename` son obligatorios y no-nulos.
- No depende de clases HTTP ni de infraestructura.
- Genera el contenido en memoria (`ByteArrayOutputStream`).

### StatementExportRequest

- Se construye mediante `StatementExportRequest.builder()`.
- Valida en `build()` que `accountId`, `format`, `dateFrom` y `dateTo` estén presentes.
- Valida que `dateFrom <= dateTo`; si no, lanza `IllegalStateException`.

### ExportResult.of()

- Factory estático: `ExportResult.of(byte[] content, String contentType, String filename)`.
- Lanza `IllegalArgumentException` si alguno de los tres parámetros es `null`.
- Expone `size()` como longitud del array de bytes.

### StatementExportService

- Implementa `ExportStatementUseCase`.
- Resuelve el strategy por `ExportFormat` desde un mapa inyectado por Spring.
- Lanza `ExportException` si la cuenta no existe, el cliente no se encuentra o el formato no tiene strategy registrado.

---

## 6. Datos de prueba disponibles (H2)

Seed en `src/main/resources/data.sql`. Se carga al iniciar la aplicación.

### Clientes

| ID | Nombre | Email |
|----|--------|-------|
| `c001` | Valentina Morales | vmorales@mail.com |
| `c002` | Rodrigo Fernández | rfernandez@mail.com |
| `c003` | Luciana Gómez | lgomez@mail.com |

### Cuentas

| Account ID | Alias | Moneda | Titular | Transacciones |
|------------|-------|--------|---------|---------------|
| `a001` | morales.ahorro.pesos | ARS | Valentina Morales | 20 (Ene–Mar 2024) |
| `a002` | morales.usd | USD | Valentina Morales | 3 |
| `a003` | fernandez.cuenta | ARS | Rodrigo Fernández | 4 |
| `a004` | gomez.ahorro | ARS | Luciana Gómez | 0 |

### Rango útil para pruebas de exportación

Cuenta `a001`, Q1 2024: `dateFrom=2024-01-01`, `dateTo=2024-03-31` → 20 transacciones.

### Conexión H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC: `jdbc:h2:mem:andbank`
- Usuario: `sa` / Contraseña: *(vacío)*

---

## 7. Endpoints REST disponibles

### Exportación

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/v1/accounts/{accountId}/statement/export?format={CSV\|PDF\|OFX\|JSON}&dateFrom={yyyy-MM-dd}&dateTo={yyyy-MM-dd}` | Exporta movimientos en el formato solicitado. Retorna archivo con `Content-Disposition: attachment`. |

**Ejemplo:**

```bash
curl -o movimientos.csv \
  "http://localhost:8080/api/v1/accounts/a001/statement/export?format=CSV&dateFrom=2024-01-01&dateTo=2024-03-31"
```

### Exploración de datos

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/v1/info` | Resumen del estado de la BD (conteos y formatos disponibles) |
| `GET` | `/api/v1/accounts` | Lista todas las cuentas |
| `GET` | `/api/v1/accounts/{accountId}/transactions` | Movimientos de una cuenta |
| `GET` | `/api/v1/customers` | Lista todos los clientes |

---

## 8. Patrones de extensión

| Patrón | Archivo | Descripción |
|--------|---------|-------------|
| Agregar formato de exportación | [PATTERNS.md §A](PATTERNS.md#patrón-a--agregar-un-nuevo-formato-de-exportación) | Checklist para crear un nuevo `*ExportStrategy` |
| Agregar puerto de salida | [PATTERNS.md §B](PATTERNS.md#patrón-b--agregar-un-nuevo-puerto-de-salida-repositorio) | Checklist para nuevo repositorio hexagonal |

---

## 9. Cómo levantar el proyecto

```bash
mvn spring-boot:run
```

App disponible en `http://localhost:8080`.
