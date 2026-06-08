package com.andbank.exporter.application.strategy;

import com.andbank.exporter.TestFixtures;
import com.andbank.exporter.domain.model.Account;
import com.andbank.exporter.domain.model.Customer;
import com.andbank.exporter.domain.model.ExportFormat;
import com.andbank.exporter.domain.model.ExportResult;
import com.andbank.exporter.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios de los strategies.
 *
 * Todos los bloques @Nested están comentados — uno por formato.
 * El ejercicio consiste en:
 *   1. Implementar el strategy correspondiente
 *   2. Descomentar el bloque de test
 *   3. Hacer pasar los tests
 *
 * La estructura de cada bloque es idéntica para facilitar la comparación
 * entre implementaciones. Seguir el mismo patrón al agregar XLSX.
 */
@DisplayName("StatementExportStrategy — Tests unitarios")
class StatementExportStrategyTest {

    private List<Transaction> transactions;
    private Account account;
    private Customer customer;

    @BeforeEach
    void setUp() {
        transactions = TestFixtures.someTransactions();
        account      = TestFixtures.anAccount();
        customer     = TestFixtures.aCustomer();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TODO: Implementar CSVExportStrategy y descomentar este bloque
    // ══════════════════════════════════════════════════════════════════════
    /*
    @Nested
    @DisplayName("CSVExportStrategy")
    class CSVTests {

        private final CSVExportStrategy strategy = new CSVExportStrategy();

        @Test
        @DisplayName("supportedFormat() retorna CSV")
        void supportedFormat() {
            assertThat(strategy.supportedFormat()).isEqualTo(ExportFormat.CSV);
        }

        @Test
        @DisplayName("el resultado tiene content-type CSV")
        void contentType() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getContentType()).startsWith("text/csv");
        }

        @Test
        @DisplayName("el contenido no es vacío")
        void contentNotEmpty() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.size()).isGreaterThan(100);
        }

        @Test
        @DisplayName("el filename termina en .csv")
        void filename() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getFilename()).endsWith(".csv");
        }

        @Test
        @DisplayName("el contenido incluye los datos del titular")
        void contenidoIncluyeTitular() {
            ExportResult result = strategy.export(transactions, account, customer);
            String csv = new String(result.getContent(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(csv).contains("Valentina Morales");
            assertThat(csv).contains("morales.ahorro.pesos");
        }

        @Test
        @DisplayName("el CSV contiene el header de columnas")
        void contieneHeader() {
            ExportResult result = strategy.export(transactions, account, customer);
            String csv = new String(result.getContent(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(csv).contains("fecha,referencia,descripcion,tipo,importe,moneda,saldo_posterior");
        }

        @Test
        @DisplayName("tiene tantas filas de datos como transacciones")
        void cantidadFilas() {
            ExportResult result = strategy.export(transactions, account, customer);
            String csv = new String(result.getContent(), java.nio.charset.StandardCharsets.UTF_8);
            long dataRows = csv.lines()
                    .filter(l -> !l.startsWith("#") && !l.startsWith("fecha"))
                    .filter(l -> !l.isBlank())
                    .count();
            assertThat(dataRows).isEqualTo(transactions.size());
        }
    }
    */

    // ══════════════════════════════════════════════════════════════════════
    //  TODO: Implementar JSONExportStrategy y descomentar este bloque
    // ══════════════════════════════════════════════════════════════════════
    /*
    @Nested
    @DisplayName("JSONExportStrategy")
    class JSONTests {

        private final JSONExportStrategy strategy = new JSONExportStrategy();

        @Test
        @DisplayName("supportedFormat() retorna JSON")
        void supportedFormat() {
            assertThat(strategy.supportedFormat()).isEqualTo(ExportFormat.JSON);
        }

        @Test
        @DisplayName("el resultado tiene content-type JSON")
        void contentType() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getContentType()).contains("application/json");
        }

        @Test
        @DisplayName("el contenido tiene las claves raíz esperadas")
        void jsonValido() {
            ExportResult result = strategy.export(transactions, account, customer);
            String json = new String(result.getContent(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(json).contains("\"account\"");
            assertThat(json).contains("\"customer\"");
            assertThat(json).contains("\"transactions\"");
            assertThat(json).contains("\"summary\"");
        }

        @Test
        @DisplayName("el summary contiene el count correcto de transacciones")
        void summaryCorrecto() {
            ExportResult result = strategy.export(transactions, account, customer);
            String json = new String(result.getContent(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(json).contains("\"count\" : " + transactions.size());
        }

        @Test
        @DisplayName("el filename termina en .json")
        void filename() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getFilename()).endsWith(".json");
        }
    }
    */

    // ══════════════════════════════════════════════════════════════════════
    //  TODO: Implementar PDFExportStrategy y descomentar este bloque
    // ══════════════════════════════════════════════════════════════════════
    /*
    @Nested
    @DisplayName("PDFExportStrategy")
    class PDFTests {

        private final PDFExportStrategy strategy = new PDFExportStrategy();

        @Test
        @DisplayName("supportedFormat() retorna PDF")
        void supportedFormat() {
            assertThat(strategy.supportedFormat()).isEqualTo(ExportFormat.PDF);
        }

        @Test
        @DisplayName("el resultado tiene content-type application/pdf")
        void contentType() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getContentType()).isEqualTo("application/pdf");
        }

        @Test
        @DisplayName("el PDF tiene firma magic bytes %PDF")
        void magicBytes() {
            ExportResult result = strategy.export(transactions, account, customer);
            // Un PDF siempre empieza con %PDF
            assertThat(new String(result.getContent(), 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("el tamaño mínimo del PDF es razonable (> 1KB)")
        void tamañoMinimo() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.size()).isGreaterThan(1024);
        }

        @Test
        @DisplayName("el filename termina en .pdf")
        void filename() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getFilename()).endsWith(".pdf");
        }
    }
    */

    // ══════════════════════════════════════════════════════════════════════
    //  TODO: Implementar OFXExportStrategy y descomentar este bloque
    // ══════════════════════════════════════════════════════════════════════
    /*
    @Nested
    @DisplayName("OFXExportStrategy")
    class OFXTests {

        private final OFXExportStrategy strategy = new OFXExportStrategy();

        @Test
        @DisplayName("supportedFormat() retorna OFX")
        void supportedFormat() {
            assertThat(strategy.supportedFormat()).isEqualTo(ExportFormat.OFX);
        }

        @Test
        @DisplayName("el resultado tiene content-type application/x-ofx")
        void contentType() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getContentType()).isEqualTo("application/x-ofx");
        }

        @Test
        @DisplayName("el OFX contiene el header estándar")
        void headerOFX() {
            ExportResult result = strategy.export(transactions, account, customer);
            String ofx = new String(result.getContent(), java.nio.charset.Charset.forName("ISO-8859-1"));
            assertThat(ofx).contains("OFXHEADER:100");
            assertThat(ofx).contains("DATA:OFXSGML");
        }

        @Test
        @DisplayName("el OFX contiene los tags raíz <OFX>")
        void tagRaiz() {
            ExportResult result = strategy.export(transactions, account, customer);
            String ofx = new String(result.getContent(), java.nio.charset.Charset.forName("ISO-8859-1"));
            assertThat(ofx).contains("<OFX>");
            assertThat(ofx).contains("</OFX>");
        }

        @Test
        @DisplayName("el OFX contiene al menos una transacción <STMTTRN>")
        void contieneTransacciones() {
            ExportResult result = strategy.export(transactions, account, customer);
            String ofx = new String(result.getContent(), java.nio.charset.Charset.forName("ISO-8859-1"));
            assertThat(ofx).contains("<STMTTRN>");
        }

        @Test
        @DisplayName("los débitos tienen importe negativo en OFX")
        void debitosNegativos() {
            List<Transaction> soloDebitos = transactions.stream()
                    .filter(t -> t.getType() == com.andbank.exporter.domain.model.TransactionType.DEBIT)
                    .toList();
            ExportResult result = strategy.export(soloDebitos, account, customer);
            String ofx = new String(result.getContent(), java.nio.charset.Charset.forName("ISO-8859-1"));
            assertThat(ofx).contains("<TRNAMT>-");
        }

        @Test
        @DisplayName("el filename termina en .ofx")
        void filename() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getFilename()).endsWith(".ofx");
        }
    }
    */

    // ══════════════════════════════════════════════════════════════════════
    //  XlsxExportStrategy — Módulo 3
    // ══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("XlsxExportStrategy")
    class XLSXTests {

        private final XlsxExportStrategy strategy = new XlsxExportStrategy();

        @Test
        @DisplayName("supportedFormat() retorna XLSX")
        void supportedFormat() {
            assertThat(strategy.supportedFormat()).isEqualTo(ExportFormat.XLSX);
        }

        @Test
        @DisplayName("el resultado tiene content-type correcto para XLSX")
        void contentType() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        @Test
        @DisplayName("el XLSX tiene firma magic bytes PK (ZIP)")
        void magicBytes() {
            ExportResult result = strategy.export(transactions, account, customer);
            byte[] content = result.getContent();
            assertThat(content[0]).isEqualTo((byte) 0x50);
            assertThat(content[1]).isEqualTo((byte) 0x4B);
        }

        @Test
        @DisplayName("el tamaño mínimo del XLSX es razonable (> 1KB)")
        void tamañoMinimo() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.size()).isGreaterThan(1024);
        }

        @Test
        @DisplayName("el filename termina en .xlsx")
        void filename() {
            ExportResult result = strategy.export(transactions, account, customer);
            assertThat(result.getFilename()).endsWith(".xlsx");
        }

        @Test
        @DisplayName("lista vacía de transacciones produce hoja sin-movimientos")
        void sinTransacciones() throws Exception {
            ExportResult result = strategy.export(List.of(), account, customer);

            assertThat(result.size()).isGreaterThan(0);
            assertThat(result.getFilename()).isEqualTo("movimientos-a001.xlsx");

            try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(
                    new java.io.ByteArrayInputStream(result.getContent()))) {
                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
                assertThat(workbook.getSheetName(0)).isEqualTo("sin-movimientos");
                assertThat(workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue())
                        .isEqualTo("No se encontraron movimientos en el rango solicitado");
            }
        }
    }
}
