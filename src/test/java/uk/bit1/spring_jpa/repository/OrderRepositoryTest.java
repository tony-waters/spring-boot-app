package uk.bit1.spring_jpa.repository;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.TestPropertySource;
import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.entity.Order;
import uk.bit1.spring_jpa.entity.Product;
import uk.bit1.spring_jpa.repository.projection.OrderWithProductCount;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@DataJpaTest
class OrderRepositoryTest {

    @Autowired OrderRepository orderRepository;
    @Autowired TestEntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;

    private Statistics getStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        return statistics;
    }

    @Test
    void findOrdersAndProductCountByCustomerId_countsCorrectly() {
        Product p1 = entityManager.persist(new Product("P1", "D1"));
        Product p2 = entityManager.persist(new Product("P2", "D2"));

        Customer c = new Customer("Delta", "Dan");

        Order o0 = new Order("O0");                 // 0 products
        Order o1 = new Order("O1"); o1.addProduct(p1);
        Order o2 = new Order("O2"); o2.addProduct(p1); o2.addProduct(p2);

        c.addOrder(o0);
        c.addOrder(o1);
        c.addOrder(o2);

        entityManager.persist(c);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        List<OrderWithProductCount> rows = orderRepository.findOrdersAndProductCountByCustomerId(c.getId());
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1); // check only one SELECT statement was issued

        assertThat(rows).hasSize(3);

        assertThat(rows)
                .filteredOn(r -> r.getDescription().equals("O0"))
                .first()
                .extracting(OrderWithProductCount::getProductCount)
                .isEqualTo(0L);

        assertThat(rows)
                .filteredOn(r -> r.getDescription().equals("O2"))
                .first()
                .extracting(OrderWithProductCount::getProductCount)
                .isEqualTo(2L);
    }

    @Test
    void findOrdersWithProductsByCustomerId_fetchesProducts() {
        Product p = entityManager.persist(new Product("Widget", "Thing"));

        Customer c = new Customer("Echo", "Eve");
        Order o = new Order("Order-1");
        o.addProduct(p);
        c.addOrder(o);

        entityManager.persist(c);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        List<Order> orders = orderRepository.findOrdersWithProductsByCustomerId(c.getId());
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1); // check only one SELECT statement was issued

        assertThat(orders).hasSize(1);
        assertThat(Hibernate.isInitialized(orders.get(0).getProducts())).isTrue();
        assertThat(orders.get(0).getProducts()).hasSize(1);
    }

    @Test
    void findByCustomerId_pagesCorrectly() {
        Customer c = new Customer("Foxtrot", "Fred");

        for (int i = 0; i < 12; i++) {
            c.addOrder(new Order("Order-" + i));
        }

        entityManager.persist(c);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        var page = orderRepository.findByCustomerId(c.getId(), PageRequest.of(0, 5));
        // check only one or two SELECT statement issued
        // Why it can be 2:
        // Even with @EntityGraph(attributePaths = {"orders","orders.products"}),
        // Hibernate may choose a 2-step fetch plan to avoid a big cartesian product:
        // 1 - Query customer + orders
        // 2 - Query products for those orders (often via an IN (...))
        assertThat(statistics.getPrepareStatementCount()).isBetween(1L, 2L);

        assertThat(page.getTotalElements()).isEqualTo(12);
        assertThat(page.getContent()).hasSize(5);
    }
}
