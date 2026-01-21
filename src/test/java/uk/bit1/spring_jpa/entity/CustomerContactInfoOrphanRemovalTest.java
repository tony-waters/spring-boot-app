package uk.bit1.spring_jpa.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerContactInfoOrphanRemovalTest {

    @Autowired TestEntityManager tem;
    @Autowired EntityManager em;

    @Test
    void setContactInfoNull_deletesOrphanRow() {
        Customer c = new Customer("Gamma", "Gina");

        ContactInfo ci = new ContactInfo();
        ci.setEmail("gina@example.com");
        ci.setPhone("07000000000");

        c.setContactInfo(ci);

        tem.persist(c);
        tem.flush();
        tem.clear();

        Long customerId = c.getId();

        // With @MapsId, ContactInfo.id == Customer.id
        assertThat(em.find(ContactInfo.class, customerId)).isNotNull();

        Customer managed = em.find(Customer.class, customerId);
        managed.setContactInfo(null);

        tem.flush();
        tem.clear();

        assertThat(em.find(ContactInfo.class, customerId)).isNull();
    }
}
