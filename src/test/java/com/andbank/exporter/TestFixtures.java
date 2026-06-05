package com.andbank.exporter;

import com.andbank.exporter.domain.model.Account;
import com.andbank.exporter.domain.model.Customer;
import com.andbank.exporter.domain.model.Transaction;
import com.andbank.exporter.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fábrica de fixtures de dominio para tests.
 * Centraliza la creación de objetos de prueba.
 */
public class TestFixtures {

    public static Customer aCustomer() {
        return new Customer("c001", "Valentina", "Morales",
                "vmorales@mail.com", "27-38291047-4");
    }

    public static Account anAccount() {
        return new Account("a001", "morales.ahorro.pesos",
                "0000003100074926130001", "ARS",
                new BigDecimal("485200.00"), "c001");
    }

    public static List<Transaction> someTransactions() {
        return List.of(
                tx("t001", TransactionType.CREDIT, "50000.00", "550000.00",
                        "Acreditación de sueldo", "REF-SAL-2401",
                        LocalDateTime.of(2024, 1, 5, 9, 0)),
                tx("t002", TransactionType.DEBIT,  "12500.00", "537500.00",
                        "Pago servicio eléctrico", "REF-SVC-2401",
                        LocalDateTime.of(2024, 1, 8, 11, 30)),
                tx("t003", TransactionType.DEBIT,   "3200.00", "534300.00",
                        "Supermercado Coto", "REF-COM-2401",
                        LocalDateTime.of(2024, 1, 12, 18, 45)),
                tx("t004", TransactionType.DEBIT,   "8750.00", "525550.00",
                        "Expensas Edificio Belgrano", "REF-EXP-2401",
                        LocalDateTime.of(2024, 1, 15, 10, 0)),
                tx("t005", TransactionType.CREDIT, "10000.00", "535550.00",
                        "Transferencia recibida Fernández", "REF-TRF-2401",
                        LocalDateTime.of(2024, 1, 20, 14, 22))
        );
    }

    private static Transaction tx(String id, TransactionType type,
                                   String amount, String balanceAfter,
                                   String description, String reference,
                                   LocalDateTime date) {
        return new Transaction(id, "a001", type,
                new BigDecimal(amount), "ARS",
                new BigDecimal(balanceAfter),
                description, reference, date);
    }
}
