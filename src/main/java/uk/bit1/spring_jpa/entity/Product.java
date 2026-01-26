package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Product extends BaseEntity {

    @ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    private String name;
    private String description;

    protected Product() {
    }

    public Product(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected void addOrder(Order order) {
        if (order == null) return;
        orders.add(order);
    }

    protected void removeOrder(Order order) {
        if(order == null) return;
        orders.remove(order);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Order> getOrders() {
        // stop modification via the Collection interface
        return java.util.Collections.unmodifiableSet(orders);
    }

    // no setOrders() by design

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
