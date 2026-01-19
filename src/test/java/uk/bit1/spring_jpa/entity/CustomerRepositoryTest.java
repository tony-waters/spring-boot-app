package uk.bit1.spring_jpa.entity;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.TestPropertySource;
import uk.bit1.spring_jpa.repository.CustomerRepository;
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
        // check only one SELECT statement was issued
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
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
        Page<CustomerWithOrderCount> page1 = customerRepository.findCustomersAndOrderCount(PageRequest.of(1, 10));
        Page<CustomerWithOrderCount> page2 = customerRepository.findCustomersAndOrderCount(PageRequest.of(2, 10));
        // check only one SELECT statement was issued for each page
//        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);


        assertThat(page0.getTotalElements()).isEqualTo(25);
        assertThat(page0.getTotalPages()).isEqualTo(3);

        assertThat(page0.getContent()).hasSize(10);
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(5);
    }

    @Test
    void findWithContactInfoById_fetchesContactInfo() {
        Customer c = new Customer("Gamma", "Gina");

        ContactInfo ci = new ContactInfo();
        ci.setEmail("gina@example.com");
        ci.setPhone("07000000000");
        c.setContactInfo(ci);

        entityManager.persist(c);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        Customer loaded = customerRepository.findWithContactInfoById(c.getId())
                .orElseThrow();
        // check only one SELECT statement issued
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // Prove the graph loaded it (no lazy init required)
        assertThat(Hibernate.isInitialized(loaded.getContactInfo())).isTrue();
        assertThat(loaded.getContactInfo().getEmail()).isEqualTo("gina@example.com");
    }

    @Test
    void findWithOrdersAndProductsById_fetchesOrdersAndTheirProducts() {
        // Products
        Product p1 = entityManager.persist(new Product("P1", "D1"));
        Product p2 = entityManager.persist(new Product("P2", "D2"));

        Customer c = new Customer("Delta", "Dan");

        Order o1 = new Order("O1");
        o1.addProduct(p1);
        o1.addProduct(p2);

        Order o2 = new Order("O2");
        o2.addProduct(p2);

        c.addOrder(o1);
        c.addOrder(o2);

        entityManager.persist(c);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = getStatistics();
        Customer loaded = customerRepository.findWithOrdersAndProductsById(c.getId())
                .orElseThrow();
        // check only one SELECT statement issued
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        assertThat(Hibernate.isInitialized(loaded.getOrders())).isTrue();
        assertThat(loaded.getOrders()).hasSize(2);

        // Ensure products are loaded for each order
        for (Order o : loaded.getOrders()) {
            assertThat(Hibernate.isInitialized(o.getProducts())).isTrue();
        }

        // Optional correctness checks
        Order loadedO1 = loaded.getOrders().stream()
                .filter(o -> o.getDescription().equals("O1"))
                .findFirst()
                .orElseThrow();

        assertThat(loadedO1.getProducts())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("P1", "P2");
    }
}
