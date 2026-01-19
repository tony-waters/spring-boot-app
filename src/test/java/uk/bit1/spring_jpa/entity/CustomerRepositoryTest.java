package uk.bit1.spring_jpa.entity;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import uk.bit1.spring_jpa.repository.CustomerRepository;
import uk.bit1.spring_jpa.repository.projection.CustomerWithOrderCount;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;
    @Autowired TestEntityManager em;

    @Test
    void findCustomersAndOrderCount_includesCustomersWithZeroOrders_andCountsCorrectly() {
        // Customer A: 0 orders
        Customer a = new Customer("Alpha", "Alice");
        em.persist(a);

        // Customer B: 2 orders
        Customer b = new Customer("Beta", "Bob");
        b.addOrder(new Order("B-1"));
        b.addOrder(new Order("B-2"));
        em.persist(b);

        em.flush();
        em.clear();

        Page<CustomerWithOrderCount> page =
                customerRepository.findCustomersAndOrderCount(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);

        // Use the projection values; order of rows is undefined unless you ORDER BY in the query.
        List<CustomerWithOrderCount> rows = page.getContent();

        CustomerWithOrderCount rowA = rows.stream()
                .filter(r -> r.getLastName().equals("Alpha"))
                .findFirst()
                .orElseThrow();
        assertThat(rowA.getOrderCount()).isEqualTo(0);

        CustomerWithOrderCount rowB = rows.stream()
                .filter(r -> r.getLastName().equals("Beta"))
                .findFirst()
                .orElseThrow();
        assertThat(rowB.getOrderCount()).isEqualTo(2);
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
            em.persist(c);
        }

        em.flush();
        em.clear();

        Page<CustomerWithOrderCount> page0 =
                customerRepository.findCustomersAndOrderCount(PageRequest.of(0, 10));
        Page<CustomerWithOrderCount> page1 =
                customerRepository.findCustomersAndOrderCount(PageRequest.of(1, 10));
        Page<CustomerWithOrderCount> page2 =
                customerRepository.findCustomersAndOrderCount(PageRequest.of(2, 10));

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

        em.persist(c);
        em.flush();
        em.clear();

        Customer loaded = customerRepository.findWithContactInfoById(c.getId())
                .orElseThrow();

        // Prove the graph loaded it (no lazy init required)
        assertThat(Hibernate.isInitialized(loaded.getContactInfo())).isTrue();
        assertThat(loaded.getContactInfo().getEmail()).isEqualTo("gina@example.com");
    }

    @Test
    void findWithOrdersAndProductsById_fetchesOrdersAndTheirProducts() {
        // Products
        Product p1 = em.persist(new Product("P1", "D1"));
        Product p2 = em.persist(new Product("P2", "D2"));

        Customer c = new Customer("Delta", "Dan");

        Order o1 = new Order("O1");
        o1.addProduct(p1);
        o1.addProduct(p2);

        Order o2 = new Order("O2");
        o2.addProduct(p2);

        c.addOrder(o1);
        c.addOrder(o2);

        em.persist(c);
        em.flush();
        em.clear();

        Customer loaded = customerRepository.findWithOrdersAndProductsById(c.getId())
                .orElseThrow();

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
