package com.andbank.exporter.application.service;

import com.andbank.exporter.application.strategy.StatementExportStrategy;
import com.andbank.exporter.domain.model.Account;
import com.andbank.exporter.domain.model.Customer;
import com.andbank.exporter.domain.model.ExportFormat;
import com.andbank.exporter.domain.model.ExportResult;
import com.andbank.exporter.domain.model.StatementExportRequest;
import com.andbank.exporter.domain.model.Transaction;
import com.andbank.exporter.domain.port.in.ExportStatementUseCase;
import com.andbank.exporter.domain.port.out.AccountRepository;
import com.andbank.exporter.domain.port.out.CustomerRepository;
import com.andbank.exporter.domain.port.out.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso principal — orquesta el patrón Strategy de exportación.
 *
 * <p>Responsabilidades:</p>
 * <ol>
 *   <li>Validar que la cuenta existe.</li>
 *   <li>Recuperar transacciones del rango solicitado.</li>
 *   <li>Seleccionar el {@link StatementExportStrategy} correcto.</li>
 *   <li>Delegar la generación al strategy y retornar el {@link ExportResult}.</li>
 * </ol>
 *
 * <p>Spring inyecta automáticamente todas las implementaciones de
 * {@link StatementExportStrategy} en la lista. El mapa {@code strategyMap}
 * permite resolver en O(1) según el formato solicitado.</p>
 */
@Service
public class StatementExportService implements ExportStatementUseCase {

    private static final Logger log = LoggerFactory.getLogger(StatementExportService.class);

    private final Map<ExportFormat, StatementExportStrategy> strategyMap;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * Spring inyecta la lista de todos los beans {@link StatementExportStrategy}
     * registrados en el contexto. Al agregar un nuevo strategy (ej: XLSX),
     * basta con anotarlo con {@code @Component} — sin tocar este constructor.
     */
    public StatementExportService(List<StatementExportStrategy> strategies,
                                  TransactionRepository transactionRepository,
                                  AccountRepository accountRepository,
                                  CustomerRepository customerRepository) {

        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        StatementExportStrategy::supportedFormat,
                        Function.identity()
                ));

        log.info("StatementExportService iniciado con {} strategies: {}",
                strategyMap.size(), strategyMap.keySet());

        this.transactionRepository = transactionRepository;
        this.accountRepository     = accountRepository;
        this.customerRepository    = customerRepository;
    }

    @Override
    public ExportResult execute(StatementExportRequest request) {

        log.debug("Exportando cuenta={}, formato={}, desde={}, hasta={}",
                request.getAccountId(), request.getFormat(),
                request.getDateFrom(), request.getDateTo());

        // 1. Verificar que la cuenta existe
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ExportException(
                        "Cuenta no encontrada: " + request.getAccountId()));

        // 2. Recuperar el cliente
        Customer customer = customerRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new ExportException(
                        "Cliente no encontrado para cuenta: " + request.getAccountId()));

        // 3. Recuperar transacciones del rango
        LocalDateTime from = request.getDateFrom().atStartOfDay();
        LocalDateTime to   = request.getDateTo().atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository
                .findByAccountIdAndDateRange(request.getAccountId(), from, to);

        log.debug("Se encontraron {} transacciones", transactions.size());

        // 4. Resolver el strategy
        StatementExportStrategy strategy = resolveStrategy(request.getFormat());

        // 5. Delegar la generación — el strategy NO escribe a disco
        ExportResult result = strategy.export(transactions, account, customer);

        log.info("Export completado: {}", result);
        return result;
    }

    /**
     * Resuelve el strategy correspondiente al formato solicitado.
     *
     * @throws ExportException si el formato no tiene implementación registrada
     */
    private StatementExportStrategy resolveStrategy(ExportFormat format) {
        StatementExportStrategy strategy = strategyMap.get(format);
        if (strategy == null) {
            throw new ExportException(
                    "Formato de exportación no soportado: " + format
                    + ". Formatos disponibles: " + strategyMap.keySet());
        }
        return strategy;
    }
}
