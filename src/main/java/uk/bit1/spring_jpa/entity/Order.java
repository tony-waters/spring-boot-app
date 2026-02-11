package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "customer_order",
        indexes = @Index(name = "idx_customer_order_customer_id", columnList = "customer_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "order_products",
//            joinColumns = @JoinColumn(name = "order_id"),
//            inverseJoinColumns = @JoinColumn(name = "product_id")
//    )
//    private Set<Product> products = new HashSet<>();

    @Getter
    @NotBlank
    @Size(min = 2, max = 255)
    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Getter
    @Column(name = "fulfilled", nullable = false)
    private boolean fulfilled = false;

    Order(String description) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description must not be empty");
        this.description = description;
    }

//    public void addProduct(Product product) {
//        if(product == null) return;
//        if (products.add(product)) {
//            product.addOrder(this);
//        }
//    }
//
//    public void removeProduct(Product product) {
//        if(product == null) return;
//        if (products.remove(product)) {
//            product.removeOrder(this);
//        }
//    }
//
//    public void removeAllProducts() {
//        // Iterating over a copy avoids ConcurrentModificationException
//        for (Product product : new HashSet<>(products)) {
//            removeProduct(product);
//        }

//    }

    void setCustomerInternal(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Order must have a Customer");
        if (this.customer != null && this.customer != customer) {
            throw new IllegalStateException("Order customer cannot be changed; delete and recreate instead");
        }
        this.customer = customer;
    }

//    public Set<Product> getProducts() {
//        // stop modification via the Collection interface
//        // breaks symmetry (Product.orders not updated)
//        return java.util.Collections.unmodifiableSet(products);
//    }

    // no setProducts by design

    @Override
    public String toString() {
        return String.format(
                "Order[id=%d, description='%s']",
                getId(), description);
    }

}
