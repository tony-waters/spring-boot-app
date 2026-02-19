package uk.bit1.spring_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
