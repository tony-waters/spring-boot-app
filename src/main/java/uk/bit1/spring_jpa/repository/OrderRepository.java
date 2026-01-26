package uk.bit1.spring_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByFulfilled(boolean fulfilled);
}
