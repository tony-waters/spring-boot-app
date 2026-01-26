package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerOrderConsistencyTest {

    @Autowired
    TestEntityManager em;

    @Test
    void addOrder_setsCustomerOnOrder() {
//        Customer c = new Customer("Beta", "Bob");
//        Order o = new Order("O1");
//
//        c.addOrder(o);
//
//        em.persist(c);
//        em.flush();
//        em.clear();
//
//        Order loaded = em.createQuery(
//                "select o from CustomerOrder o", Order.class
//        ).getSingleResult();
//
//        assertThat(loaded.getCustomer()).isNotNull();
//        assertThat(loaded.getCustomer().getLastName()).isEqualTo("Beta");
    }

    @Test
    void removeOrder_clearsCustomerOnOrder() {
//        Customer c = new Customer("Gamma", "Gina");
//        Order o = new Order("O1");
//
//        c.addOrder(o);
//        em.persist(c);
//        em.flush();
//        em.clear();
//
//        Customer managed = em.find(Customer.class, c.getId());
//        Order managedOrder = managed.getOrders().iterator().next();
//
//        managed.removeOrder(managedOrder);
//        em.flush();
//        em.clear();
//
//        assertThat(
//                em.createQuery("select count(o) from CustomerOrder o", Long.class)
//                        .getSingleResult()
//        ).isZero();
    }
}

