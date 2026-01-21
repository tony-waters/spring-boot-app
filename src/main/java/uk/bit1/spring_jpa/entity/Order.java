package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "CustomerOrder")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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

    public Order(Long id, String description, boolean fulfilled, Customer customer) {
        this(description);
        this.id = id;
        this.fulfilled = fulfilled;
        setCustomer(customer);
    }

    public void addProduct(Product product) {
        if(product == null) return;
        if (products.add(product)) {
            product.getOrders().add(this);
        }
    }

    public void removeProduct(Product product) {
        if(product == null) return;
        if (products.remove(product)) {
            product.getOrders().remove(this);
        }
    }

    public void clearProducts() {
        // Iterating over a copy avoids ConcurrentModificationException
        for (Product product : new HashSet<>(products)) {
            removeProduct(product);
        }
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean getFulfilled() {
        return fulfilled;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

//    private void setCustomer(Customer customer) {
//        this.customer = customer;
//    }

    public void setCustomer(Customer newCustomer) {
        if (this.customer == newCustomer) return;

        if (this.customer != null) {
            this.customer.getOrders().remove(this);
        }

        this.customer = newCustomer;

        if (newCustomer != null) {
            newCustomer.getOrders().add(this);
        }
    }

    // no setProducts by design

    @Override
    public String toString() {
        return String.format(
                "Order[id=%d, description='%s']",
                id, description);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
