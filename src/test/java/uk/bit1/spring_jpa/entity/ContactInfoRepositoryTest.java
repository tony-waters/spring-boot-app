package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import uk.bit1.spring_jpa.entity.ContactInfo;
import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.repository.ContactInfoRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactInfoRepositoryTest {

    @Autowired
    ContactInfoRepository contactInfoRepository;
    @Autowired TestEntityManager em;

    @Test
    void findByEmailIgnoreCase_findsContactInfo() {
        Customer c = new Customer("Alpha", "Alice");

        ContactInfo ci = new ContactInfo();
        ci.setEmail("alice@example.com");
        ci.setPhone("0700000000");

        c.setContactInfo(ci);

        em.persist(c);
        em.flush();
        em.clear();

        assertThat(contactInfoRepository.findByEmailIgnoreCase("ALICE@EXAMPLE.COM"))
                .isPresent();

        assertThat(contactInfoRepository.findByEmailIgnoreCase("missing@example.com"))
                .isEmpty();
    }

    @Test
    void existsByEmailIgnoreCase_returnsCorrectValue() {
        Customer c = new Customer("Beta", "Bob");

        ContactInfo ci = new ContactInfo();
        ci.setEmail("bob@example.com");
        c.setContactInfo(ci);

        em.persist(c);
        em.flush();
        em.clear();

        assertThat(contactInfoRepository.existsByEmailIgnoreCase("bob@example.com")).isTrue();
        assertThat(contactInfoRepository.existsByEmailIgnoreCase("BOB@EXAMPLE.COM")).isTrue();
        assertThat(contactInfoRepository.existsByEmailIgnoreCase("nope@example.com")).isFalse();
    }

    @Test
    void findById_usesCustomerIdBecauseOfMapsId() {
        Customer c = new Customer("Gamma", "Gina");

        ContactInfo ci = new ContactInfo();
        ci.setEmail("gina@example.com");
        c.setContactInfo(ci);

        em.persist(c);
        em.flush();
        em.clear();

        assertThat(contactInfoRepository.findById(c.getId())).isPresent();
    }
}
