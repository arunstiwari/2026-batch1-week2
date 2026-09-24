package com.fil.week2.model;

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
    @Column(name="status", nullable = false)
    private CustomerStatus status;

    @Version
    private long version;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "customer_tag", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "tag", nullable = false, length = 40)
    private Set<String> tags = new HashSet<>();

    @OneToMany(mappedBy = "customer", orphanRemoval = false,cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();


    protected Customer() {}

    public Customer(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setCustomer(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setCustomer(null);
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

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
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

    public void setId(Long id) {
        this.id = id;
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
                ", status=" + status +
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
}
