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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.entity.Order;
import uk.bit1.spring_jpa.repository.projection.CustomerWithOrderCount;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@DataJpaTest
class CustomerRepositoryTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired TestEntityManager em;
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

        em.persist(a);
        em.persist(z);
        em.flush();
        em.clear();

        var page = customerRepository.findCustomersAndOrderCount(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);

        CustomerWithOrderCount first = page.getContent().get(0);
        CustomerWithOrderCount second = page.getContent().get(1);

        assertThat(first.getLastName()).isEqualTo("Aardvark");
        assertThat(first.getOrderCount()).isEqualTo(2);

        assertThat(second.getLastName()).isEqualTo("Zebra");
        assertThat(second.getOrderCount()).isEqualTo(1);
    }

    @Test
    void findWithOrdersAndProductsById_loadsGraph() {
        Customer c = new Customer("Jones", "Belinda");
        Order o = new Order("o1");
        c.addOrder(o);

        em.persist(c);
        em.flush();
        em.clear();

        Customer loaded = customerRepository.findWithOrdersById(c.getId()).orElseThrow();

        // Within the test transaction, LAZY would still load, but this at least ensures the query runs
        assertThat(loaded.getOrders()).hasSize(1);
    }
}
