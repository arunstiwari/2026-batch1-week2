package com.fil.week2.model;

import com.fil.week2.exception.InsufficientFundsException;
import jakarta.persistence.*;


@Entity
@Table(name = "account",
        uniqueConstraints = @UniqueConstraint(name = "org_account_number",
                columnNames = "account_number"))
public class Account extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AccountNumberConverter.class)
    @Column(name = "account_number", nullable = false, length = 14)
    private AccountNumber accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="account_type", nullable = false)
    private AccountType accountType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    // an override REPLACES the column, so precision/scale must be restated
                    // here or the stored scale is whatever the database chooses
                    column = @Column(name = "balance_amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "balance_currency", nullable = false, length = 3))
    })
    private Money balance = Money.zero();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Version
    private long version;

    protected Account() { }

    /** An account always opens at zero: money enters only through {@link #deposit}. */
    public Account(AccountNumber accountNumber, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = Money.zero();
    }

    public Long getId() {
        return id;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Money getBalance() {
        return balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public long getVersion() {
        return version;
    }

    /** Only {@link Customer#addAccount} and {@link Customer#removeAccount} may set this. */
    void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void deposit(Money amount) {
        this.balance = this.balance.plus(requirePositive(amount, "A deposit"));
    }

    public void withdraw(Money amount) {
        Money requested = requirePositive(amount, "A withdrawal");
        if (balance.isLessThan(requested)) {
            throw new InsufficientFundsException(accountNumber.value(), requested.getAmount());
        }
        this.balance = balance.minus(requested);
    }

    /**
     * Money cannot be negative, so a negative deposit is now impossible to
     * construct. Zero still has to be refused here: it is a valid balance but
     * not a valid movement.
     */
    private static Money requirePositive(Money amount, String what) {
        if (amount == null) {
            throw new IllegalArgumentException(what + " requires an amount");
        }
        if (amount.hasZeroAmount()) {
            throw new IllegalArgumentException(what + " must be for a positive amount");
        }
        return amount;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                '}';
    }
}
