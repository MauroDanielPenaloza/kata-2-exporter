package com.andbank.exporter.application.strategy;

import com.andbank.exporter.application.service.ExportException;
import com.andbank.exporter.domain.model.Account;
import com.andbank.exporter.domain.model.Customer;
import com.andbank.exporter.domain.model.ExportFormat;
import com.andbank.exporter.domain.model.ExportResult;
import com.andbank.exporter.domain.model.Transaction;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Strategy de exportación a Excel (XLSX) con agrupación mensual.
 * Una hoja por mes ({@code YYYY-MM}); sin movimientos → hoja {@code sin-movimientos}.
 */
@Component
public class XlsxExportStrategy implements StatementExportStrategy {

    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EMPTY_SHEET_NAME = "sin-movimientos";
    private static final String EMPTY_MESSAGE =
            "No se encontraron movimientos en el rango solicitado";
    private static final String[] HEADERS = {
            "fecha", "referencia", "descripcion", "tipo", "importe", "moneda", "saldo_posterior"
    };
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ExportFormat supportedFormat() {
        return ExportFormat.XLSX;
    }

    @Override
    public ExportResult export(List<Transaction> transactions, Account account, Customer customer) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            if (transactions == null || transactions.isEmpty()) {
                createEmptySheetMessage(workbook);
            } else {
                groupByMonth(transactions).forEach((yearMonth, monthTransactions) -> {
                    XSSFSheet sheet = workbook.createSheet(yearMonth.toString());
                    buildSheet(sheet, monthTransactions);
                });
            }

            workbook.write(outputStream);
            String filename = "movimientos-" + account.getId() + ".xlsx";
            return ExportResult.of(outputStream.toByteArray(), CONTENT_TYPE, filename);

        } catch (IOException e) {
            throw new ExportException("Error al generar archivo XLSX", e);
        }
    }

    private Map<YearMonth, List<Transaction>> groupByMonth(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getTransactionDate()),
                        TreeMap::new,
                        Collectors.toList()));
    }

    private void buildSheet(XSSFSheet sheet, List<Transaction> transactions) {
        createHeaderRow(sheet);
        for (int i = 0; i < transactions.size(); i++) {
            createDataRow(sheet, i + 1, transactions.get(i));
        }
    }

    private void createHeaderRow(XSSFSheet sheet) {
        var row = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            row.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private void createDataRow(XSSFSheet sheet, int rowIndex, Transaction transaction) {
        var row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(transaction.getTransactionDate().format(DATE_FORMAT));
        row.createCell(1).setCellValue(transaction.getReference());
        row.createCell(2).setCellValue(transaction.getDescription());
        row.createCell(3).setCellValue(transaction.getType().name());
        row.createCell(4).setCellValue(transaction.getAmount().doubleValue());
        row.createCell(5).setCellValue(transaction.getCurrency());
        row.createCell(6).setCellValue(transaction.getBalanceAfter().doubleValue());
    }

    private void createEmptySheetMessage(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(EMPTY_SHEET_NAME);
        sheet.createRow(0).createCell(0).setCellValue(EMPTY_MESSAGE);
    }
}
