package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import uk.bit1.spring_jpa.repository.CustomerRepository;
import uk.bit1.spring_jpa.repository.ProfileRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CustomerProfileUniqueConstraintDataJpaTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired ProfileRepository profileRepository;

    @Test
    void uniqueFkPreventsTwoCustomersSharingSameProfile() {
        Profile shared = profileRepository.saveAndFlush(new Profile("shared@example.com", false));
        assertThat(shared.getId()).isNotNull();

        Customer a = new Customer("Alice");
        a.attachProfile(shared);
        customerRepository.saveAndFlush(a);

        Customer b = new Customer("Bob");
        b.attachProfile(shared);

        assertThatThrownBy(() -> {
            customerRepository.save(b);
            customerRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}