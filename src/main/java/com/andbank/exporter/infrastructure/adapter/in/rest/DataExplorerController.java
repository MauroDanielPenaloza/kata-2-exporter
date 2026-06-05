package com.andbank.exporter.infrastructure.adapter.in.rest;

import com.andbank.exporter.infrastructure.adapter.out.persistence.entity.AccountEntity;
import com.andbank.exporter.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.andbank.exporter.infrastructure.adapter.out.persistence.entity.TransactionEntity;
import com.andbank.exporter.infrastructure.adapter.out.persistence.repository.JpaAccountRepository;
import com.andbank.exporter.infrastructure.adapter.out.persistence.repository.JpaCustomerRepository;
import com.andbank.exporter.infrastructure.adapter.out.persistence.repository.JpaTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller de utilidad para explorar los datos de la base durante la kata.
 *
 * <p>Endpoints de consulta (solo GET, read-only):</p>
 * <pre>
 * GET /api/v1/accounts                    → lista todas las cuentas
 * GET /api/v1/accounts/{id}/transactions  → movimientos de una cuenta
 * GET /api/v1/customers                   → lista todos los clientes
 * GET /api/v1/info                        → resumen del estado de la BD
 * </pre>
 */
@RestController
@RequestMapping("/api/v1")
public class DataExplorerController {

    private final JpaAccountRepository     accountRepo;
    private final JpaCustomerRepository    customerRepo;
    private final JpaTransactionRepository transactionRepo;

    public DataExplorerController(JpaAccountRepository accountRepo,
                                   JpaCustomerRepository customerRepo,
                                   JpaTransactionRepository transactionRepo) {
        this.accountRepo     = accountRepo;
        this.customerRepo    = customerRepo;
        this.transactionRepo = transactionRepo;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Map<String, Object>>> getAllAccounts() {
        List<Map<String, Object>> result = accountRepo.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions(
            @PathVariable String accountId) {
        List<TransactionEntity> txs = transactionRepo
                .findByAccountIdOrderByTransactionDateAsc(accountId);
        List<Map<String, Object>> result = txs.stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Map<String, Object>>> getAllCustomers() {
        List<Map<String, Object>> result = customerRepo.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("accounts",     accountRepo.count());
        info.put("customers",    customerRepo.count());
        info.put("transactions", transactionRepo.count());
        info.put("formatos_disponibles", List.of("CSV", "PDF", "OFX", "JSON"));
        info.put("endpoint_export",
                "GET /api/v1/accounts/{accountId}/statement/export?format=CSV&dateFrom=2024-01-01&dateTo=2024-03-31");
        return ResponseEntity.ok(info);
    }

    private Map<String, Object> toMap(AccountEntity a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",       a.getId());
        m.put("alias",    a.getAlias());
        m.put("cbu",      a.getCbu());
        m.put("currency", a.getCurrency());
        m.put("balance",  a.getBalance());
        m.put("customerId", a.getCustomerId());
        return m;
    }

    private Map<String, Object> toMap(CustomerEntity c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",        c.getId());
        m.put("fullName",  c.getFirstName() + " " + c.getLastName());
        m.put("email",     c.getEmail());
        m.put("cuit",      c.getCuit());
        return m;
    }

    private Map<String, Object> toMap(TransactionEntity t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",          t.getId());
        m.put("type",        t.getType());
        m.put("amount",      t.getAmount());
        m.put("currency",    t.getCurrency());
        m.put("description", t.getDescription());
        m.put("reference",   t.getReference());
        m.put("date",        t.getTransactionDate());
        m.put("balanceAfter",t.getBalanceAfter());
        return m;
    }
}
