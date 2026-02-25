
package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.bit1.spring_jpa.repository.CustomerRepository;
import uk.bit1.spring_jpa.repository.ProfileRepository;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CustomerProfileMappingDataJpaTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired ProfileRepository profileRepository;

    @Test
    void creatingProfilePersistsAndSharesPrimaryKey() {
        Customer c = new Customer("Waters", "Tony");
        c.createProfile("TonyW", true);

        Customer saved = customerRepository.saveAndFlush(c);
        Long customerId = saved.getId();

        assertThat(customerId).isNotNull();

        // Profile uses @MapsId so Profile.id == Customer.id
        assertThat(profileRepository.findById(customerId)).isPresent();
        Profile p = profileRepository.findById(customerId).orElseThrow();

        assertThat(p.getId()).isEqualTo(customerId);
        assertThat(p.getEmailAddress()).isEqualTo("TonyW");
        assertThat(p.isMarketingOptIn()).isTrue();
    }

    @Test
    void removingProfileDeletesOrphanRow() {
        Customer c = new Customer("Waters", "Tony");
        c.createProfile("TonyW", false);

        Customer saved = customerRepository.saveAndFlush(c);
        Long customerId = saved.getId();

        assertThat(profileRepository.findById(customerId)).isPresent();

        // remove + flush => orphanRemoval should delete profile row
        saved.removeProfile();
        customerRepository.saveAndFlush(saved);

        assertThat(profileRepository.findById(customerId)).isNotPresent();
    }
}
