package uk.bit1.spring_jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    Page<Customer> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    Optional<Customer> findByLastNameIgnoreCaseAndFirstNameIgnoreCase(String lastName, String firstName);

    // Fetch graphs (use when you know you need relationships)
    @EntityGraph(attributePaths = { "orders" })
    Optional<Customer> findWithOrdersById(Long id);

    @EntityGraph(attributePaths = { "contactInfo" })
    Optional<Customer> findWithContactInfoById(Long id);

    // "Big" graph: customer -> orders -> products
    @EntityGraph(attributePaths = { "orders", "orders.products" })
    Optional<Customer> findWithOrdersAndProductsById(Long id);
}
