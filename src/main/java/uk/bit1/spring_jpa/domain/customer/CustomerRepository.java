package uk.bit1.spring_jpa.domain.customer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @EntityGraph(attributePaths = {"profile", "tickets", "tickets.tags"})
    Optional<Customer> findAggregateById(Long id);
}