package uk.bit1.spring_jpa.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.entity.Order;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerOrderOrphanRemovalTest {

    @Autowired TestEntityManager tem;
    @Autowired EntityManager em;

    @Test
    void removeOrder_deletesOrphanRow() {
        Customer c = new Customer("Alpha", "Alice");
        Order o1 = new Order("O1");
        Order o2 = new Order("O2");

        c.addOrder(o1);
        c.addOrder(o2);

        tem.persist(c);
        tem.flush();
        tem.clear();

        assertThat(countOrders()).isEqualTo(2);

        Customer managed = em.find(Customer.class, c.getId());
        Order toRemove = managed.getOrders().stream()
                .filter(o -> "O1".equals(o.getDescription()))
                .findFirst()
                .orElseThrow();

        managed.removeOrder(toRemove);

        tem.flush();
        tem.clear();

        assertThat(countOrders()).isEqualTo(1);
        assertThat(orderDescriptions()).containsExactly("O2");
    }

    @Test
    void clearOrders_deletesAllOrphanRows() {
        Customer c = new Customer("Beta", "Bob");
        c.addOrder(new Order("B1"));
        c.addOrder(new Order("B2"));
        c.addOrder(new Order("B3"));

        tem.persist(c);
        tem.flush();
        tem.clear();

        assertThat(countOrders()).isEqualTo(3);

        Customer managed = em.find(Customer.class, c.getId());
        managed.clearOrders();

        tem.flush();
        tem.clear();

        assertThat(countOrders()).isEqualTo(0);
    }

    private long countOrders() {
        // JPQL entity name, not class name
        return em.createQuery("select count(o) from CustomerOrder o", Long.class)
                .getSingleResult();
    }

    private java.util.List<String> orderDescriptions() {
        return em.createQuery("select o.description from CustomerOrder o order by o.description", String.class)
                .getResultList();
    }
}
