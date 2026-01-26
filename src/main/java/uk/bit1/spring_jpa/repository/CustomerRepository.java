package uk.bit1.spring_jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.repository.projection.CustomerWithOrderCount;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    // Fetch graphs (use when you know you need relationships)
    @EntityGraph(attributePaths = { "orders" })
    Optional<Customer> findWithOrdersById(Long id);

    @EntityGraph(attributePaths = { "contactInfo" })
    Optional<Customer> findWithContactInfoById(Long id);

    @EntityGraph(attributePaths = { "orders", "orders.products" })
    Optional<Customer> findWithOrdersAndProductsById(Long id);

    @Query(
            value = """
            select c.id as customerId,
                   c.firstName as firstName,
                   c.lastName as lastName,
                   count(o.id) as orderCount
            from Customer c
            left join c.orders o
            group by c.id, c.firstName, c.lastName
            order by c.lastName, c.id
            """,
            countQuery = "select count(c) from Customer c"
    )
    Page<CustomerWithOrderCount> findCustomersAndOrderCount(Pageable pageable);
}
