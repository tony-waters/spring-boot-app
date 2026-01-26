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
import uk.bit1.spring_jpa.entity.ContactInfo;
import uk.bit1.spring_jpa.entity.Customer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactInfoRepositoryTest {

    @Autowired ContactInfoRepository contactInfoRepository;
    @Autowired TestEntityManager em;
    @Autowired EntityManagerFactory entityManagerFactory;

    private Statistics getStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        return statistics;
    }

    @Test
    void findByEmailIgnoreCase_findsContactInfo() {
        Customer c = new Customer("Waters", "Tony");
        ContactInfo info = new ContactInfo("tony@example.com", "07123456789");
        c.setContactInfo(info);

        em.persist(c);
        em.flush();
        em.clear();

        assertThat(contactInfoRepository.findByEmailIgnoreCase("TONY@EXAMPLE.COM"))
                .isPresent();

        ContactInfo found =
                contactInfoRepository.findByEmailIgnoreCase("tony@example.com")
                        .orElseThrow();

        assertThat(found.getEmail()).isEqualTo("tony@example.com");
        assertThat(found.getCustomer()).isNotNull();
        assertThat(found.getCustomer().getLastName()).isEqualTo("Waters");
    }

    @Test
    void existsByEmailIgnoreCase_returnsTrueWhenPresent() {
        Customer c = new Customer("Jones", "Belinda");
        c.setContactInfo(new ContactInfo("belinda@example.com", "07000000000"));

        em.persist(c);
        em.flush();
        em.clear();

        assertThat(contactInfoRepository.existsByEmailIgnoreCase("BELINDA@EXAMPLE.COM"))
                .isTrue();

        assertThat(contactInfoRepository.existsByEmailIgnoreCase("missing@example.com"))
                .isFalse();
    }
}