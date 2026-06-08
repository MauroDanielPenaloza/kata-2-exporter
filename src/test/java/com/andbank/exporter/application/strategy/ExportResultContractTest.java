package com.andbank.exporter.application.strategy;

import com.andbank.exporter.TestFixtures;
import com.andbank.exporter.domain.model.ExportResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica el contrato de {@link ExportResult}.
 *
 * Este test corre desde el inicio — no depende de ningún strategy.
 *
 * Módulo 5 — Regla: Todo strategy retorna ExportResult con byte[] y
 * ContentType no nulos. El size() debe ser > 0 para cualquier input válido.
 *
 * Cuando implementes cada strategy, agregar al smoke test de abajo.
 */
@DisplayName("ExportResult — Contrato de output")
class ExportResultContractTest {

    @Test
    @DisplayName("no permite content null")
    void noPermiteContentNull() {
        assertThatThrownBy(() -> ExportResult.of(null, "text/csv", "file.csv"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content");
    }

    @Test
    @DisplayName("no permite contentType null")
    void noPermiteContentTypeNull() {
        assertThatThrownBy(() -> ExportResult.of(new byte[]{1}, null, "file.csv"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contentType");
    }

    @Test
    @DisplayName("no permite filename null")
    void noPermiteFilenameNull() {
        assertThatThrownBy(() -> ExportResult.of(new byte[]{1}, "text/csv", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("filename");
    }

    @Test
    @DisplayName("size() refleja el tamaño real del contenido")
    void sizeEsCorrecto() {
        byte[] content = "hola mundo".getBytes();
        ExportResult result = ExportResult.of(content, "text/plain", "test.txt");
        assertThat(result.size()).isEqualTo(content.length);
    }

    /**
     * Smoke test: todos los strategies implementados deben producir output con size > 0.
     *
     * TODO: a medida que implementes cada strategy, descomentarlo aquí.
     */
    @Test
    @DisplayName("smoke test — strategies implementados producen output no vacío")
    void smokeTodosLosStrategies() {
        var transactions = TestFixtures.someTransactions();
        var account      = TestFixtures.anAccount();
        var customer     = TestFixtures.aCustomer();

        assertThat(new XlsxExportStrategy()
                .export(transactions, account, customer).size()).isGreaterThan(0);
    }
}
