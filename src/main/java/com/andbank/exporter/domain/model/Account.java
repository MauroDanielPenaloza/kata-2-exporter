package com.andbank.exporter.domain.model;

import java.math.BigDecimal;

/**
 * Cuenta bancaria — entidad de dominio pura.
 */
public class Account {

    private String id;
    private String alias;
    private String cbu;
    private String currency;
    private BigDecimal balance;
    private String customerId;

    public Account() {}

    public Account(String id, String alias, String cbu,
                   String currency, BigDecimal balance, String customerId) {
        this.id = id;
        this.alias = alias;
        this.cbu = cbu;
        this.currency = currency;
        this.balance = balance;
        this.customerId = customerId;
    }

    public String getId()                        { return id; }
    public void setId(String id)                 { this.id = id; }

    public String getAlias()                     { return alias; }
    public void setAlias(String alias)           { this.alias = alias; }

    public String getCbu()                       { return cbu; }
    public void setCbu(String cbu)               { this.cbu = cbu; }

    public String getCurrency()                  { return currency; }
    public void setCurrency(String currency)     { this.currency = currency; }

    public BigDecimal getBalance()               { return balance; }
    public void setBalance(BigDecimal balance)   { this.balance = balance; }

    public String getCustomerId()                { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
