package uk.bit1.spring_jpa.entity;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import uk.bit1.spring_jpa.repository.OrderRepository;
import uk.bit1.spring_jpa.repository.projection.OrderWithProductCount;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    OrderRepository orderRepository;
    @Autowired TestEntityManager em;

    @Test
    void findOrdersAndProductCountByCustomerId_countsCorrectly_includingZeroProducts() {
        // Products
        Product p1 = em.persist(new Product("P1", "D1"));
        Product p2 = em.persist(new Product("P2", "D2"));

        // Customer + orders
        Customer c = new Customer("Beta", "Bob");

        Order o0 = new Order("O0"); // 0 products
        Order o1 = new Order("O1"); o1.addProduct(p1); // 1 product
        Order o2 = new Order("O2"); o2.addProduct(p1); o2.addProduct(p2); // 2 products

        c.addOrder(o0);
        c.addOrder(o1);
        c.addOrder(o2);

        em.persist(c);
        em.flush();
        em.clear();

        List<OrderWithProductCount> rows =
                orderRepository.findOrdersAndProductCountByCustomerId(c.getId());

        assertThat(rows).hasSize(3);

        OrderWithProductCount r0 = rows.stream().filter(r -> r.getDescription().equals("O0")).findFirst().orElseThrow();
        OrderWithProductCount r1 = rows.stream().filter(r -> r.getDescription().equals("O1")).findFirst().orElseThrow();
        OrderWithProductCount r2 = rows.stream().filter(r -> r.getDescription().equals("O2")).findFirst().orElseThrow();

        assertThat(r0.getProductCount()).isEqualTo(0);
        assertThat(r1.getProductCount()).isEqualTo(1);
        assertThat(r2.getProductCount()).isEqualTo(2);
    }

    @Test
    void findOrdersWithProductsByCustomerId_fetchesProductsForAllOrders() {
        Product p1 = em.persist(new Product("P1", "D1"));
        Product p2 = em.persist(new Product("P2", "D2"));

        Customer c = new Customer("Gamma", "Gina");

        Order o1 = new Order("O1"); o1.addProduct(p1); o1.addProduct(p2);
        Order o2 = new Order("O2"); o2.addProduct(p2);

        c.addOrder(o1);
        c.addOrder(o2);

        em.persist(c);
        em.flush();
        em.clear();

        List<Order> orders = orderRepository.findOrdersWithProductsByCustomerId(c.getId());

        assertThat(orders).hasSize(2);
        for (Order o : orders) {
            assertThat(Hibernate.isInitialized(o.getProducts())).isTrue();
        }

        Order loadedO1 = orders.stream().filter(o -> o.getDescription().equals("O1")).findFirst().orElseThrow();
        assertThat(loadedO1.getProducts()).extracting(Product::getName)
                .containsExactlyInAnyOrder("P1", "P2");
    }
}
