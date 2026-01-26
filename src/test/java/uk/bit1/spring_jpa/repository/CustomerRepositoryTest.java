package uk.bit1.spring_jpa.repository;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.entity.Order;
import uk.bit1.spring_jpa.repository.projection.CustomerWithOrderCount;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@DataJpaTest
class CustomerRepositoryTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired TestEntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;

    private Statistics getStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        return statistics;
    }

    @Test
    void findCustomersAndOrderCount_returnsStablePagedProjection() {
        // Aardvark, Zebra to make ordering obvious
        Customer a = new Customer("Aardvark", "Amy");
        a.addOrder(new Order("a1"));
        a.addOrder(new Order("a2"));

        Customer z = new Customer("Zebra", "Zoe");
        z.addOrder(new Order("z1"));

        entityManager.persist(a);
        entityManager.persist(z);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        var page = customerRepository.findCustomersAndOrderCount(PageRequest.of(0, 10));
        // check number of SELECT statements is 2 (or 1)
        // hibernate may (depending on implementation) optimise last page and not bother running the SELECT count as it can infer from pages being less than 10
        assertThat(statistics.getPrepareStatementCount()).isBetween(1L, 2L);

        assertThat(page.getTotalElements()).isEqualTo(2);

        CustomerWithOrderCount first = page.getContent().get(0);
        CustomerWithOrderCount second = page.getContent().get(1);

        assertThat(first.getLastName()).isEqualTo("Aardvark");
        assertThat(first.getOrderCount()).isEqualTo(2);

        assertThat(second.getLastName()).isEqualTo("Zebra");
        assertThat(second.getOrderCount()).isEqualTo(1);
    }

    @Test
    void findCustomersAndOrderCount_includesCustomersWithZeroOrders_andCountsCorrectly() {

        Customer customer1 = new Customer("Alpha", "Alice");
        entityManager.persist(customer1);

        Customer customer2 = new Customer("Beta", "Bob");
        customer2.addOrder(new Order("B-1"));
        customer2.addOrder(new Order("B-2"));
        entityManager.persist(customer2);

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        Page<CustomerWithOrderCount> page = customerRepository.findCustomersAndOrderCount(PageRequest.of(0, 10));
        // hibernate may (depending on implementation) optimise last page and not bother running the SELECT count as it can infer from pages being less than 10
        assertThat(statistics.getPrepareStatementCount()).isBetween(1L, 2L);
        assertThat(page.getTotalElements()).isEqualTo(2);

        // Use the projection values; order of rows is undefined unless you ORDER BY in the query.
        List<CustomerWithOrderCount> rows = page.getContent();

        CustomerWithOrderCount row1 = rows.stream()
                .filter(r -> r.getLastName().equals("Alpha"))
                .findFirst()
                .orElseThrow();
        assertThat(row1.getOrderCount()).isEqualTo(0);

        CustomerWithOrderCount row2 = rows.stream()
                .filter(r -> r.getLastName().equals("Beta"))
                .findFirst()
                .orElseThrow();
        assertThat(row2.getOrderCount()).isEqualTo(2);
    }

    @Test
    void findCustomersAndOrderCount_pagingUsesCountQueryCorrectly() {
        // Create 25 customers, with i orders each for a bit of variety
        for (int i = 0; i < 25; i++) {
            Customer c = new Customer("Last" + i, "First" + i);
            // e.g. 0..2 orders repeating
            int orderCount = i % 3;
            for (int o = 0; o < orderCount; o++) {
                c.addOrder(new Order("Order " + i + "-" + o));
            }
            entityManager.persist(c);
        }

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        Page<CustomerWithOrderCount> page0 = customerRepository.findCustomersAndOrderCount(PageRequest.of(0, 10));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);

        statistics = getStatistics();
        Page<CustomerWithOrderCount> page1 = customerRepository.findCustomersAndOrderCount(PageRequest.of(1, 10));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);

        statistics = getStatistics();
        Page<CustomerWithOrderCount> page2 = customerRepository.findCustomersAndOrderCount(PageRequest.of(2, 10));
        // hibernate may (depending on implementation) optimise last page and not bother running the SELECT count as it can infer from pages being less than 10
        assertThat(statistics.getPrepareStatementCount()).isBetween(1L, 2L);

        assertThat(page0.getTotalElements()).isEqualTo(25);
        assertThat(page0.getTotalPages()).isEqualTo(3);

        assertThat(page0.getContent()).hasSize(10);
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(5);
    }

    @Test
    void findWithOrdersAndProductsById_loadsGraph() {
        Customer c = new Customer("Jones", "Belinda");
        Order o = new Order("o1");
        c.addOrder(o);

        entityManager.persist(c);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        Customer loaded = customerRepository.findWithOrdersById(c.getId()).orElseThrow();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2L);

        // Within the test transaction, LAZY would still load, but this at least ensures the query runs
        assertThat(loaded.getOrders()).hasSize(1);
    }
}
