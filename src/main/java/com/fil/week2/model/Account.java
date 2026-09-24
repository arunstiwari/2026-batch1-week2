package com.fil.week2.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "account",
        uniqueConstraints = @UniqueConstraint(name = "org_account_number",
                columnNames = "accountNumber"))
public class Account extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="account_type", nullable = false)
    private AccountType accountType;

    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Version
    private long version;

    public long getVersion() {
        return version;
    }

    protected Account() { }

    public Account(String accountNumber, AccountType accountType, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                ", customer=" + customer +
                '}';
    }
}
