package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity // (name = "CustomerOrder")
@Table(name = "customer_orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    private String description;

    private boolean fulfilled = false;

    protected Order() {
    }

    public Order(String description) {
        this.description = description;
    }

    public void addProduct(Product product) {
        if(product == null) return;
        if (products.add(product)) {
            product.addOrder(this);
        }
    }

    public void removeProduct(Product product) {
        if(product == null) return;
        if (products.remove(product)) {
            product.removeOrder(this);
        }
    }

    public void removeAllProducts() {
        // Iterating over a copy avoids ConcurrentModificationException
        for (Product product : new HashSet<>(products)) {
            removeProduct(product);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public Customer getCustomer() {
        return customer;
    }

    protected void setCustomer(Customer newCustomer) {
        if (this.customer == newCustomer) return;
        if (this.customer != null) {
            this.customer.removeOrder(this);
        }
        this.customer = newCustomer;
        if (newCustomer != null) {
            newCustomer.addOrder(this);
        }
    }

    public Set<Product> getProducts() {
        // stop modification via the Collection interface
        // breaks symmetry (Product.orders not updated)
        return java.util.Collections.unmodifiableSet(products);
    }

    // no setProducts by design

    @Override
    public String toString() {
        return String.format(
                "Order[id=%d, description='%s']",
                getId(), description);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
