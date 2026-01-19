package uk.bit1.spring_jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bit1.spring_jpa.entity.Order;
import uk.bit1.spring_jpa.repository.projection.OrderWithProductCount;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    // When you need products for a specific order
    @EntityGraph(attributePaths = { "products" })
    Optional<Order> findWithProductsById(Long id);

    @Query("""
        select o.id as orderId,
               o.description as description,
               o.fulfilled as fulfilled,
               count(p.id) as productCount
        from CustomerOrder o
        left join o.products p
        where o.customer.id = :customerId
        group by o.id, o.description, o.fulfilled
        order by o.id
        """)
    List<OrderWithProductCount> findOrdersAndProductCountByCustomerId(@Param("customerId") Long customerId);

    @Query("""
        select distinct o
        from CustomerOrder o
        left join fetch o.products p
        where o.customer.id = :customerId
        order by o.id
        """)
    List<Order> findOrdersWithProductsByCustomerId(@Param("customerId") Long customerId);
}
