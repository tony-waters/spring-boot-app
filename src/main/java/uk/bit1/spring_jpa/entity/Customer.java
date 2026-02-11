package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

//    @OneToOne(
//            mappedBy = "customer",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private ContactInfo contactInfo;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Order> orders = new HashSet<>();

    @Getter
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Getter
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    public Customer(String lastName, String firstName) {
        this.lastName= lastName;
        this.firstName = firstName;
    }

    public Order createOrder(String description) {
        Order order = new Order(description);
        addOrderInternal(order);
        return order;
    }

    private void addOrderInternal(Order order) {
        if (order == null) throw new IllegalArgumentException("Order must not be null");
        if (order.getCustomer() != null && order.getCustomer() != this) {
            throw new IllegalStateException("Cannot move Order between Customers");
        }
        order.setCustomerInternal(this); // safe even if already set
        orders.add(order);
    }

    public void removeOrderAndDelete(Order order) {
        if (order == null) return;
        if (order.getCustomer() != this) {
            throw new IllegalArgumentException("Order does not belong to this customer");
        }
        boolean removed = orders.remove(order);
        if (!removed) {
            throw new IllegalStateException("Order was not in Customer.orders (detached instance?)");
        }
        // orphanRemoval will delete on flush
    }

    public void deleteAllOrders() {
        // Iterating over a copy avoids ConcurrentModificationException
        for (Order order : new HashSet<>(orders)) {
            removeOrderAndDelete(order);
        }
    }

//    public ContactInfo getContactInfo() {
//        return contactInfo;
//    }

//    public void setContactInfo(ContactInfo contactInfo) {
//        if (this.contactInfo != null) {
//            this.contactInfo.setCustomer(null);
//        }
//        this.contactInfo = contactInfo;
//        if (contactInfo != null) {
//            contactInfo.setCustomer(this);
//        }
//    }

    public Set<Order> getOrders() {
        return java.util.Collections.unmodifiableSet(orders);
    }

    // no setOrders by design

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", firstName=" + firstName + ", lastName=" + lastName + "}";
    }

}
