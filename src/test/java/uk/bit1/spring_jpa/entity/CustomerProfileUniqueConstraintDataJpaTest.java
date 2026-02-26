package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.bit1.spring_jpa.repository.CustomerRepository;
import uk.bit1.spring_jpa.repository.ProfileRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CustomerProfileUniqueConstraintDataJpaTest {

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ProfileRepository profileRepository;

    @Test
    void uniqueFkPreventsTwoCustomersSharingSameProfile() {
        // persist one profile first
        Profile shared = profileRepository.saveAndFlush(new Profile("shared@example.com", false));
        assertThat(shared.getId()).isNotNull();

        // customer A points at shared profile
        Customer a = new Customer("Alice");
        a.attachProfileInternal(shared);
        customerRepository.saveAndFlush(a);

        // customer B tries to point at same shared profile -> should violate UNIQUE(profile_id)
        Customer b = new Customer("Bob");
        b.attachProfileInternal(shared);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(b))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
