package uk.bit1.spring_jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.entity.Order;
import uk.bit1.spring_jpa.entity.Product;
import uk.bit1.spring_jpa.repository.projection.OrderWithProductCount;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired OrderRepository orderRepository;

    @Test
    void findOrdersAndProductCountByCustomerId_countsDistinctProducts() {
        Product p1 = new Product("Tea", "Yorkshire");
        Product p2 = new Product("Biscuits", "Hobnobs");
        em.persist(p1);
        em.persist(p2);

        Customer c = new Customer("Brown", "Esther");
        Order o1 = new Order("o1");
        Order o2 = new Order("o2");
        c.addOrder(o1);
        c.addOrder(o2);

        // o1 has 2 products, o2 has 1
        o1.addProduct(p1);
        o1.addProduct(p2);
        o2.addProduct(p1);

        em.persist(c);
        em.flush();
        em.clear();

        var page = orderRepository.findOrdersAndProductCountByCustomerId(c.getId(), PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);

        List<OrderWithProductCount> rows = page.getContent();
        assertThat(rows).hasSize(2);

        // ordered by o.id, so just assert counts appear
        assertThat(rows).extracting(OrderWithProductCount::getProductCount)
                .containsExactlyInAnyOrder(2L, 1L);
    }

    @Test
    void findOrderIdsByCustomerId_pagesIdsInOrder() {
        Customer c = new Customer("Jones", "Belinda");
        Order o1 = new Order("o1");
        Order o2 = new Order("o2");
        Order o3 = new Order("o3");
        c.addOrder(o1);
        c.addOrder(o2);
        c.addOrder(o3);

        em.persist(c);
        em.flush();
        em.clear();

        var page1 = orderRepository.findOrderIdsByCustomerId(c.getId(), PageRequest.of(0, 2));
        var page2 = orderRepository.findOrderIdsByCustomerId(c.getId(), PageRequest.of(1, 2));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page1.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findOrdersWithProductsByIdIn_fetchesProductsForGivenIds() {
        Product p1 = new Product("Tea", "Yorkshire");
        Product p2 = new Product("Biscuits", "Hobnobs");
        em.persist(p1);
        em.persist(p2);

        Customer c = new Customer("Brown", "Esther");
        Order o1 = new Order("o1");
        Order o2 = new Order("o2");
        c.addOrder(o1);
        c.addOrder(o2);

        o1.addProduct(p1);
        o1.addProduct(p2);
        o2.addProduct(p1);

        em.persist(c);
        em.flush();
        em.clear();

        // Load ids (simulate step 1)
        var idPage = orderRepository.findOrderIdsByCustomerId(c.getId(), PageRequest.of(0, 10));
        List<Long> ids = idPage.getContent();

        // Step 2
        List<Order> fetched = orderRepository.findOrdersWithProductsByIdIn(ids);

        assertThat(fetched).hasSize(2);
        assertThat(fetched)
                .allSatisfy(o -> assertThat(o.getProducts()).isNotEmpty());

        // Important: ordering by IN isn't guaranteed; you can either:
        // - reorder in service, or
        // - add ORDER BY o.id in the fetch query (recommended)
        assertThat(fetched).extracting(Order::getId).containsExactlyInAnyOrderElementsOf(ids);
    }
}
