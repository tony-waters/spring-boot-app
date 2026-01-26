package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Customer extends BaseEntity {

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

    public void removeAllOrders() {
        // Iterating over a copy avoids ConcurrentModificationException
        for (Order order : new HashSet<>(orders)) {
            removeOrder(order);
        }
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        if (this.contactInfo != null) {
            this.contactInfo.setCustomer(null);
        }
        this.contactInfo = contactInfo;
        if (contactInfo != null) {
            contactInfo.setCustomer(this);
        }
    }

    public Set<Order> getOrders() {
        return java.util.Collections.unmodifiableSet(orders);
    }

    // no setOrders by design

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", firstName=" + firstName + ", lastName=" + lastName +
                ", orderCount=" + (orders == null ? 0 : orders.size()) + "}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
