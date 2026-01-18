package uk.bit1.spring_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.bit1.spring_jpa.entity.Customer;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);

    @Query("select c from Customer c where c.orders is not empty")
    List<Customer> findByOrdersNotEmpty();

//    @Query("SELECT c FROM  Customer c")
//    List<Customer> findAllWithOrders();



}
