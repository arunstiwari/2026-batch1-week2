package com.fil.week2.model;

import com.fil.week2.exception.AccountNotFoundException;
import com.fil.week2.exception.AccountOpeningNotPermittedException;
import com.fil.week2.exception.InvalidStateTransitionException;
import com.fil.week2.exception.KycNotFoundException;
import com.fil.week2.exception.WithdrawalNotPermittedException;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "customer",
        uniqueConstraints = @UniqueConstraint(name = "org_unique_email", columnNames = "email"))
public class Customer extends AuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    @Embedded
    private Address address;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city",
                    column = @Column(name = "billing_city")),
            @AttributeOverride(name = "zip",
                    column = @Column(name = "billing_zip")),
            @AttributeOverride(name = "street",
                    column = @Column(name = "billing_street")),
    })
    private Address billingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "standing", nullable = false)
    private Standing standing;

    @Version
    private long version;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "customer_tag", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "tag", nullable = false, length = 40)
    private Set<String> tags = new HashSet<>();

    /**
     * Accounts are members of this aggregate, not a separate one: the rule limiting
     * how many a Customer may hold cannot be evaluated from a single Account, and no
     * database constraint can express it. Every write to an Account therefore goes
     * through this root.
     */
    @OneToMany(mappedBy = "customer", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();

    /** Append-only: a decided verification is never edited, so the trail survives. */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<KycVerification> kycVerifications = new ArrayList<>();


    protected Customer() {}

    /** No id: identity is the database's to issue, never the caller's to choose. */
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
        this.standing = Standing.ACTIVE;
    }

    /**
     * The root is the factory: opening an account needs this Customer's verification
     * and standing, and - once the cap exists - a count of the accounts already held.
     * <p>
     * To enforce a cap, add the count check here and load the Customer with
     * {@code OPTIMISTIC_FORCE_INCREMENT}. Adding a child does not dirty this row, so
     * without a forced increment two concurrent openings would both pass the check.
     */
    public Account openAccount(AccountType accountType, AccountNumber accountNumber) {
        requireAccountOpeningPermitted();
        Account account = new Account(accountNumber, accountType);
        accounts.add(account);
        account.setCustomer(this);
        return account;
    }

    public Account depositTo(AccountNumber accountNumber, Money amount) {
        Account account = requireAccount(accountNumber);
        account.deposit(amount);
        return account;
    }

    /** Money may come in under a hold; none goes out. */
    public Account withdrawFrom(AccountNumber accountNumber, Money amount) {
        if (!isInGoodStanding()) {
            throw new WithdrawalNotPermittedException(id, "standing is " + standing + ", not ACTIVE");
        }
        Account account = requireAccount(accountNumber);
        account.withdraw(amount);
        return account;
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    /** The Account Opening Policy: two independent clauses, verification and standing. */
    private void requireAccountOpeningPermitted() {
        if (!isInGoodStanding()) {
            throw new AccountOpeningNotPermittedException(id,
                    "standing is " + standing + ", not ACTIVE");
        }
        if (currentVerification().isEmpty()) {
            throw new AccountOpeningNotPermittedException(id,
                    "no KYC verification has been submitted");
        }
        if (!isVerified()) {
            throw new AccountOpeningNotPermittedException(id,
                    "KYC verification is "
                            + standingVerification().map(v -> v.getStatus().name()).orElse("undecided")
                            + ", not APPROVED");
        }
    }

    /**
     * Resolved within this aggregate, so an account belonging to someone else is
     * simply not here. No caller can forget to scope the lookup.
     */
    private Account requireAccount(AccountNumber accountNumber) {
        return accounts.stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(id, accountNumber.value()));
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Standing getStanding() {
        return standing;
    }

    /**
     * A single value cannot express two concurrent holds, or say who placed one
     * and who may lift it. Those arrive together with Restriction; until then
     * suspension is a bare transition rather than a half-built reason field.
     */
    public void suspend() {
        if (standing != Standing.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Customer " + id + " cannot be suspended from standing " + standing);
        }
        standing = Standing.SUSPENDED;
    }

    public void reinstate() {
        if (standing != Standing.SUSPENDED) {
            throw new InvalidStateTransitionException(
                    "Customer " + id + " cannot be reinstated from standing " + standing);
        }
        standing = Standing.ACTIVE;
    }

    public boolean isInGoodStanding() {
        return standing == Standing.ACTIVE;
    }

    public Address getAddress() {
        return address;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=' "+getCreatedAt()+ '\''+
                ", updatedAt=' "+getUpdatedAt()+ '\''+
                ", address=' "+address+ '\''+
                ", billingAddress=' "+billingAddress+ '\''+
                ", standing=" + standing +
                ", tags=" + tags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) && Objects.equals(name, customer.name) && Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }

    /** Every submission appends. The invariant is that only one may be open at a time. */
    public KycVerification submitKyc(DocumentType documentType, DocumentNumber documentNumber) {
        openVerification().ifPresent(open -> {
            throw new InvalidStateTransitionException(
                    "Customer " + id + " already has a verification awaiting a decision");
        });
        KycVerification verification = new KycVerification(this, documentType, documentNumber);
        kycVerifications.add(verification);
        return verification;
    }

    public KycVerification approveKyc(String reviewedBy) {
        KycVerification open = requireOpenVerification();
        open.approve(reviewedBy);
        return open;
    }

    public KycVerification rejectKyc(String reviewedBy, String reason) {
        KycVerification open = requireOpenVerification();
        open.reject(reviewedBy, reason);
        return open;
    }

    public List<KycVerification> getKycVerifications() {
        return Collections.unmodifiableList(kycVerifications);
    }

    /** The most recent verification, whatever its status. */
    public Optional<KycVerification> currentVerification() {
        return kycVerifications.isEmpty()
                ? Optional.empty()
                : Optional.of(kycVerifications.get(kycVerifications.size() - 1));
    }

    /**
     * The most recent decided verification: the one that answers whether
     * this customer is verified. A refresh awaiting review revokes nothing, so
     * submitting one does not cost a customer their access.
     */
    public Optional<KycVerification> standingVerification() {
        for (int i = kycVerifications.size() - 1; i >= 0; i--) {
            KycVerification verification = kycVerifications.get(i);
            if (!verification.isOpen()) {
                return Optional.of(verification);
            }
        }
        return Optional.empty();
    }

    public boolean isVerified() {
        return standingVerification().filter(KycVerification::isApproved).isPresent();
    }

    private Optional<KycVerification> openVerification() {
        return currentVerification().filter(KycVerification::isOpen);
    }

    /**
     * Never submitted is a different situation from submitted-and-already-decided:
     * the first is a missing record, the second an illegal transition.
     */
    private KycVerification requireOpenVerification() {
        if (kycVerifications.isEmpty()) {
            throw new KycNotFoundException(id);
        }
        return openVerification().orElseThrow(() -> new InvalidStateTransitionException(
                "Customer " + id + " has no verification awaiting a decision"));
    }
}
