package com.andbank.exporter.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio pura — sin anotaciones de framework.
 * Representa un movimiento bancario ya ocurrido.
 */
public class Transaction {

    private String id;
    private String accountId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceAfter;
    private String description;
    private String reference;
    private LocalDateTime transactionDate;

    public Transaction() {}

    public Transaction(String id, String accountId, TransactionType type,
                       BigDecimal amount, String currency, BigDecimal balanceAfter,
                       String description, String reference, LocalDateTime transactionDate) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.reference = reference;
        this.transactionDate = transactionDate;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getAccountId()                   { return accountId; }
    public void setAccountId(String accountId)     { this.accountId = accountId; }

    public TransactionType getType()               { return type; }
    public void setType(TransactionType type)      { this.type = type; }

    public BigDecimal getAmount()                  { return amount; }
    public void setAmount(BigDecimal amount)       { this.amount = amount; }

    public String getCurrency()                    { return currency; }
    public void setCurrency(String currency)       { this.currency = currency; }

    public BigDecimal getBalanceAfter()            { return balanceAfter; }
    public void setBalanceAfter(BigDecimal b)      { this.balanceAfter = b; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference()                   { return reference; }
    public void setReference(String reference)     { this.reference = reference; }

    public LocalDateTime getTransactionDate()      { return transactionDate; }
    public void setTransactionDate(LocalDateTime d){ this.transactionDate = d; }
}
