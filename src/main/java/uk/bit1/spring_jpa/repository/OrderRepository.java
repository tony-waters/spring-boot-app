package uk.bit1.spring_jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    List<Order> findByCustomerIdAndFulfilled(Long customerId, boolean fulfilled);

    long countByCustomerId(Long customerId);

    // When you need products for a specific order
    @EntityGraph(attributePaths = { "products" })
    Optional<Order> findWithProductsById(Long id);

    // When you need both sides (rarely; be careful with size)
    @EntityGraph(attributePaths = { "customer", "products" })
    Optional<Order> findWithCustomerAndProductsById(Long id);
}
