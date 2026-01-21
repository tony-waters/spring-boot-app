package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private ContactInfo contactInfo;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Order> orders = new HashSet<>();

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    protected Customer() {}

    public Customer(String lastName, String firstName) {
        this.lastName= lastName;
        this.firstName = firstName;
    }

    public Customer(Long id, String lastName, String firstName) {
        this(lastName, firstName);
        this.id = id;
    }

    public void addOrder(Order order) {
        if(order == null) return;
        if(orders.add(order)) {
            order.setCustomer(this);
        }
    }

    public void removeOrder(Order order) {
        if(order == null) return;
        if(orders.remove(order)) {
            order.setCustomer(null);
        }
    }

    public void clearOrders() {
        // Iterating over a copy avoids ConcurrentModificationException
        for (Order order : new HashSet<>(orders)) {
            removeOrder(order);
        }
    }

    public Long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public Set<Order> getOrders() { return orders; }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
        if (contactInfo != null) {
            contactInfo.setCustomer(this);
        }
    }

    // no setOrders by design

    @Override
    public String toString() {
        return "Customer{id=" + id + ", firstName=" + firstName + ", lastName=" + lastName +
                ", orderCount=" + (orders == null ? 0 : orders.size()) + "}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
