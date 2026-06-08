# Patrones de extensión

Guías para extender el proyecto **kata-exporter** siguiendo la arquitectura hexagonal.  
Para el mapa de clases y contratos, ver [ARCHITECTURE.md](ARCHITECTURE.md).

---

## Patrón A — Agregar un nuevo formato de exportación

Ejemplo: implementar `XLSXExportStrategy`.

### Checklist

1. **Agregar valor al enum** [`ExportFormat`](src/main/java/com/andbank/exporter/domain/model/ExportFormat.java)  
   Descomentar o agregar el nuevo valor (ej: `XLSX`).

2. **Habilitar dependencia en `pom.xml`** si aplica  
   Para XLSX, descomentar la dependencia `poi-ooxml` (Apache POI).

3. **Crear la clase strategy** en `application/strategy/`  
   Implementar [`StatementExportStrategy`](src/main/java/com/andbank/exporter/application/strategy/StatementExportStrategy.java) y anotarla con `@Component`.

4. **Implementar `supportedFormat()`**  
   Retornar el valor del enum correspondiente.

5. **Implementar `export()`**  
   - Recibir `List<Transaction>`, `Account` y `Customer` (ya filtrados por el service).
   - Generar el contenido en un `ByteArrayOutputStream` (nunca escribir a disco).
   - Retornar [`ExportResult.of(bytes, contentType, filename)`](src/main/java/com/andbank/exporter/domain/model/ExportResult.java).

6. **Descomentar el bloque de test** en [`StatementExportStrategyTest`](src/test/java/com/andbank/exporter/application/strategy/StatementExportStrategyTest.java)  
   Cada formato tiene un bloque `@Nested` comentado con tests de contrato.

7. **Agregar al smoke test** en [`ExportResultContractTest`](src/test/java/com/andbank/exporter/application/strategy/ExportResultContractTest.java)  
   Verificar que el strategy produce output con `size() > 0`.

### Regla de oro

> **Nunca escribir a disco desde el strategy.** Todo el contenido se genera en memoria. La escritura HTTP es responsabilidad del adapter (`StatementExportController`).

### Content-types de referencia

| Formato | Content-Type |
|---------|--------------|
| CSV | `text/csv; charset=UTF-8` |
| PDF | `application/pdf` |
| OFX | `application/x-ofx` |
| JSON | `application/json; charset=UTF-8` |
| XLSX | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |

### Descubrimiento automático

`StatementExportService` recibe `List<StatementExportStrategy>` por inyección de Spring y construye un mapa `ExportFormat → Strategy`. Al agregar un `@Component` nuevo, **no es necesario modificar el service**.

---

## Patrón B — Agregar un nuevo puerto de salida (repositorio)

Pasos para exponer acceso a una nueva entidad desde el dominio.

### Checklist

1. **Crear interfaz en `domain/port/out/`**  
   Sin dependencias de framework. Solo tipos del dominio.

2. **Crear `*Entity` en `infrastructure/adapter/out/persistence/entity/`**  
   Con anotaciones JPA (`@Entity`, `@Table`, etc.).

3. **Crear `Jpa*Repository extends JpaRepository`** en `infrastructure/adapter/out/persistence/repository/`  
   Agregar queries custom si el puerto lo requiere.

4. **Agregar métodos de mapeo** en [`PersistenceMapper`](src/main/java/com/andbank/exporter/infrastructure/adapter/out/persistence/mapper/PersistenceMapper.java)  
   Conversiones `Entity ↔ Domain` (MapStruct o manual).

5. **Crear `*PersistenceAdapter @Component`** en `infrastructure/adapter/out/persistence/`  
   Implementar el puerto de salida delegando al `Jpa*Repository` y al mapper.

### Ejemplo de referencia (cuentas)

| Capa | Clase |
|------|-------|
| Puerto | [`AccountRepository`](src/main/java/com/andbank/exporter/domain/port/out/AccountRepository.java) |
| Entidad JPA | `AccountEntity` |
| Repositorio Spring | `JpaAccountRepository` |
| Mapper | `PersistenceMapper.toDomain(AccountEntity)` |
| Adaptador | `AccountPersistenceAdapter` |

### Principio

El dominio y la aplicación dependen **solo** de la interfaz del puerto. La infraestructura implementa el contrato y encapsula JPA/H2.
