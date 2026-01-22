package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

// Lock in equals/hashCode correctness.

@DataJpaTest
class OrderEqualityTest {

    @Autowired
    TestEntityManager em;

    @Test
    void samePersistedOrder_notAddedTwiceToSet() {
        Customer c = new Customer("Alpha", "Alice");

        Order o = new Order("O1");
        c.addOrder(o);

        em.persist(c);
        em.flush();
        em.clear();

        Customer managed = em.find(Customer.class, c.getId());
        Order sameOrder = managed.getOrders().iterator().next();

        managed.addOrder(sameOrder); // attempt duplicate

        em.flush();
        em.clear();

        Customer reloaded = em.find(Customer.class, c.getId());
        assertThat(reloaded.getOrders()).hasSize(1);
    }
}
