package com.andbank.exporter.application.strategy;

import com.andbank.exporter.domain.model.Account;
import com.andbank.exporter.domain.model.Customer;
import com.andbank.exporter.domain.model.ExportFormat;
import com.andbank.exporter.domain.model.ExportResult;
import com.andbank.exporter.domain.model.Transaction;

import java.util.List;

/**
 * Contrato del patrón Strategy para exportación de movimientos bancarios.
 *
 * <p><strong>Regla de oro (Módulo 5):</strong></p>
 * <ul>
 *   <li>Siempre retorna {@link ExportResult} con {@code byte[]} y {@code contentType}.</li>
 *   <li><em>Nunca</em> escribe a disco — eso es responsabilidad del adapter de salida.</li>
 *   <li>No depende de {@code HttpServletResponse} ni de ninguna clase de infraestructura.</li>
 *   <li>Todo el contenido se genera en memoria ({@code ByteArrayOutputStream}).</li>
 * </ul>
 *
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * IMPLEMENTACIONES A CREAR — modelos de referencia
 *
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ── 1. CSVExportStrategy ─────────────────────────────────────────────────
 *
 *  @Component
 *  public class CSVExportStrategy implements StatementExportStrategy {
 *
 *      // Content-type: "text/csv; charset=UTF-8"
 *      // Filename: "movimientos_{accountId}.csv"
 *      // Encoding: UTF-8 con BOM (0xEF 0xBB 0xBF) para compatibilidad con Excel en Windows
 *
 *      // Estructura del archivo:
 *      // ─────────────────────────────────────────────────────
 *      // # Resumen de cuenta — AndBank
 *      // # Titular: {customer.fullName}
 *      // # CBU: {account.cbu}
 *      // # Alias: {account.alias}
 *      // # Moneda: {account.currency}
 *      // #
 *      // fecha,referencia,descripcion,tipo,importe,moneda,saldo_posterior
 *      // 05/01/2024 09:00,REF-SAL-2401,Acreditación de sueldo,CREDIT,50000.00,ARS,550000.00
 *      // 08/01/2024 11:30,REF-SVC-2401,Pago servicio eléctrico,DEBIT,12500.00,ARS,537500.00
 *      // ...
 *
 *      // Reglas de formato:
 *      // - Fecha: dd/MM/yyyy HH:mm
 *      // - Campos con comas o comillas: envolver en comillas dobles, escapar " como ""
 *      // - Los amounts se escriben con toPlainString() (sin notación científica)
 *
 *      @Override
 *      public ExportFormat supportedFormat() { return ExportFormat.CSV; }
 *
 *      @Override
 *      public ExportResult export(List<Transaction> transactions,
 *                                 Account account, Customer customer) { ... }
 *  }
 *
 * ── 2. JSONExportStrategy ────────────────────────────────────────────────
 *
 *  @Component
 *  public class JSONExportStrategy implements StatementExportStrategy {
 *
 *      // Content-type: "application/json; charset=UTF-8"
 *      // Filename: "movimientos_{accountId}.json"
 *      // Usar Jackson ObjectMapper con INDENT_OUTPUT y JavaTimeModule
 *
 *      // Estructura del JSON de salida:
 *      // {
 *      //   "account": {
 *      //     "id": "a001",
 *      //     "alias": "morales.ahorro.pesos",
 *      //     "cbu": "0000003100074926130001",
 *      //     "currency": "ARS",
 *      //     "balance": 485200.00
 *      //   },
 *      //   "customer": {
 *      //     "fullName": "Valentina Morales",
 *      //     "email": "vmorales@mail.com",
 *      //     "cuit": "27-38291047-4"
 *      //   },
 *      //   "transactions": [
 *      //     {
 *      //       "id": "t001",
 *      //       "date": "2024-01-05T09:00:00",
 *      //       "type": "CREDIT",
 *      //       "amount": 50000.00,
 *      //       "currency": "ARS",
 *      //       "balanceAfter": 550000.00,
 *      //       "description": "Acreditación de sueldo",
 *      //       "reference": "REF-SAL-2401"
 *      //     },
 *      //     ...
 *      //   ],
 *      //   "summary": {
 *      //     "count": 20,
 *      //     "totalCredits": 135000.00,
 *      //     "totalDebits": 185350.00,
 *      //     "netBalance": -50350.00,
 *      //     "generatedAt": "2024-04-01T10:00:00"
 *      //   }
 *      // }
 *
 *      @Override
 *      public ExportFormat supportedFormat() { return ExportFormat.JSON; }
 *
 *      @Override
 *      public ExportResult export(List<Transaction> transactions,
 *                                 Account account, Customer customer) { ... }
 *  }
 *
 * ── 3. PDFExportStrategy ─────────────────────────────────────────────────
 *
 *  @Component
 *  public class PDFExportStrategy implements StatementExportStrategy {
 *
 *      // Content-type: "application/pdf"
 *      // Filename: "movimientos_{accountId}.pdf"
 *      // Librería: iText 8 (ya en el pom.xml: kernel + layout)
 *      // Generación: completamente en memoria con ByteArrayOutputStream
 *      // Magic bytes: el PDF siempre empieza con "%PDF" (0x25 0x50 0x44 0x46)
 *
 *      // Estructura del documento:
 *      //
 *      // [ENCABEZADO]
 *      //   AndBank — Resumen de Movimientos          (título centrado, bold, 18pt)
 *      //   Titular: {customer.fullName}              (10pt)
 *      //   CUIT: {customer.cuit}                     (10pt)
 *      //   Cuenta: {account.alias} | CBU: {cbu}      (10pt)
 *      //   Moneda: {currency} | Saldo: {balance}     (10pt)
 *      //
 *      // [TABLA DE MOVIMIENTOS]  — useAllAvailableWidth()
 *      //   Columnas (anchos relativos): Fecha(2) | Referencia(3) | Descripción(5)
 *      //                                Tipo(1.5) | Importe(2) | Saldo(2)
 *      //   Header: fondo LIGHT_GRAY, bold, centrado, 9pt
 *      //   Filas alternas: blanco / #f5f5f5, 8pt
 *      //   Importes DEBIT: color rojo
 *      //   Importes CREDIT: color verde (0,128,0)
 *      //   Importe format: "- 12500.00" para débitos, "+ 50000.00" para créditos
 *      //
 *      // [PIE]
 *      //   "Total movimientos: N | Créditos: X | Débitos: Y"   (9pt, alineado a la derecha)
 *
 *      @Override
 *      public ExportFormat supportedFormat() { return ExportFormat.PDF; }
 *
 *      @Override
 *      public ExportResult export(List<Transaction> transactions,
 *                                 Account account, Customer customer) { ... }
 *  }
 *
 * ── 4. OFXExportStrategy ─────────────────────────────────────────────────
 *
 *  @Component
 *  public class OFXExportStrategy implements StatementExportStrategy {
 *
 *      // Content-type: "application/x-ofx"
 *      // Filename: "movimientos_{accountId}.ofx"
 *      // Encoding: ISO-8859-1 (Latin-1) — estándar del formato OFX heredado, NO cambiar a UTF-8
 *
 *      // OFX es un derivado de SGML (anterior a XML): los tags de valor NO tienen cierre.
 *      // Parsers OFX son muy sensibles al formato del header — respetar exactamente.
 *
 *      // Estructura completa del archivo:
 *      // ─────────────────────────────────────────────────────
 *      // OFXHEADER:100
 *      // DATA:OFXSGML
 *      // VERSION:151
 *      // SECURITY:NONE
 *      // ENCODING:1252
 *      // CHARSET:1252
 *      // COMPRESSION:NONE
 *      // OLDFILEUID:NONE
 *      // NEWFILEUID:{UUID sin guiones, mayúsculas}
 *      //                                              ← línea en blanco obligatoria
 *      // <OFX>
 *      // <SIGNONMSGSRSV1>
 *      //   <SONRS>
 *      //     <STATUS>
 *      //       <CODE>0
 *      //       <SEVERITY>INFO
 *      //       <MESSAGE>OK
 *      //     </STATUS>
 *      //     <DTSERVER>{YYYYMMDDHHmmss}
 *      //     <LANGUAGE>SPA
 *      //     <FI>
 *      //       <ORG>AndBank Argentina</ORG>
 *      //       <FID>AR002</FID>
 *      //     </FI>
 *      //   </SONRS>
 *      // </SIGNONMSGSRSV1>
 *      // <BANKMSGSRSV1>
 *      //   <STMTTRNRS>
 *      //     <TRNUID>{16 chars hex}
 *      //     <STATUS><CODE>0<SEVERITY>INFO</STATUS>
 *      //     <STMTRS>
 *      //       <CURDEF>{account.currency}
 *      //       <BANKACCTFROM>
 *      //         <BANKID>ANDBANK_AR
 *      //         <ACCTID>{account.cbu}
 *      //         <ACCTTYPE>SAVINGS
 *      //       </BANKACCTFROM>
 *      //       <BANKTRANLIST>
 *      //         <DTSTART>{fecha más antigua, YYYYMMDDHHmmss}
 *      //         <DTEND>{fecha más reciente, YYYYMMDDHHmmss}
 *      //         <STMTTRN>
 *      //           <TRNTYPE>{resolveOFXType(tx)}
 *      //           <DTPOSTED>{YYYYMMDDHHmmss}
 *      //           <TRNAMT>{amount — negativo si DEBIT}
 *      //           <FITID>{tx.id sanitizado, máx 36 chars, sin espacios ni caracteres especiales}
 *      //           <REFNUM>{tx.reference}
 *      //           <MEMO>{tx.description sanitizado, máx 96 chars, sin < > &}
 *      //         </STMTTRN>
 *      //         ...
 *      //       </BANKTRANLIST>
 *      //       <LEDGERBAL>
 *      //         <BALAMT>{account.balance}
 *      //         <DTASOF>{now, YYYYMMDDHHmmss}
 *      //       </LEDGERBAL>
 *      //     </STMTRS>
 *      //   </STMTTRNRS>
 *      // </BANKMSGSRSV1>
 *      // </OFX>
 *
 *      // Tipos OFX (TRNTYPE) — heurística a implementar en resolveOFXType():
 *      //   DIRECTDEP  → descripción contiene "sueldo" o "acreditacion"
 *      //   XFER       → descripción contiene "transferencia"
 *      //   PAYMENT    → descripción contiene "servicio" o "pago"
 *      //   REPEATPMT  → descripción contiene "expensas" o "alquiler"
 *      //   CREDIT     → cualquier otro CREDIT
 *      //   DEBIT      → cualquier otro DEBIT
 *      //
 *      // Tipos OFX estándar completos (para extender la heurística):
 *      //   INT, FEE, XFER, CHECK, PAYMENT, ATM, POS, DEP, DIRECTDEP, REPEATPMT, OTHER
 *
 *      @Override
 *      public ExportFormat supportedFormat() { return ExportFormat.OFX; }
 *
 *      @Override
 *      public ExportResult export(List<Transaction> transactions,
 *                                 Account account, Customer customer) { ... }
 *  }
 *
 * ── 5. XLSXExportStrategy — TODO Módulo 3 ───────────────────────────────
 *
 *  @Component
 *  public class XLSXExportStrategy implements StatementExportStrategy {
 *
 *      // Content-type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
 *      // Filename: "movimientos_{accountId}.xlsx"
 *      // Librería: Apache POI poi-ooxml (descomentar en pom.xml)
 *      // Magic bytes: XLSX es un ZIP → empieza con PK (0x50 0x4B)
 *
 *      // Estructura: una hoja por mes con las transacciones de ese mes
 *      // Nombre de cada hoja: "Enero 2024", "Febrero 2024", etc.
 *
 *      @Override
 *      public ExportFormat supportedFormat() { return ExportFormat.XLSX; }
 *
 *      @Override
 *      public ExportResult export(List<Transaction> transactions,
 *                                 Account account, Customer customer) { ... }
 *  }
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
public interface StatementExportStrategy {

    /**
     * Formato que maneja esta implementación.
     * Usado por {@code StatementExportService} para resolver el strategy en O(1).
     */
    ExportFormat supportedFormat();

    /**
     * Genera el contenido exportado en memoria y lo envuelve en un {@link ExportResult}.
     *
     * @param transactions movimientos ya filtrados por el service
     * @param account      cuenta del titular
     * @param customer     datos del cliente para el encabezado
     * @return resultado listo para ser enviado por HTTP — nunca null
     */
    ExportResult export(List<Transaction> transactions, Account account, Customer customer);
}
