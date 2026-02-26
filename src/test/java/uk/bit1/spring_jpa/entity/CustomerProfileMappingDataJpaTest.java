
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
    void creatingProfilePersistsViaCascade_withIndependentIds() {
        Customer c = new Customer("Tony");
        Profile p = c.createProfile("tony@example.com", true);

        Customer saved = customerRepository.saveAndFlush(c);

        assertThat(saved.getId()).isNotNull();
        assertThat(p.getId()).isNotNull();

        // NOT shared PK anymore
        assertThat(p.getId()).isNotEqualTo(saved.getId());

        Customer reloaded = customerRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getProfile()).isNotNull();
        assertThat(reloaded.getProfile().getEmailAddress()).isEqualTo("tony@example.com");
        assertThat(reloaded.getProfile().isMarketingOptIn()).isTrue();
    }

    @Test
    void removingProfileDeletesOrphanRow() {
        Customer c = new Customer("Tony");
        Profile p = c.createProfile("tony@example.com", false);

        Customer saved = customerRepository.saveAndFlush(c);
        Long profileId = p.getId();

        assertThat(profileRepository.findById(profileId)).isPresent();

        saved.removeProfile();
        customerRepository.saveAndFlush(saved);

        assertThat(profileRepository.findById(profileId)).isNotPresent();
    }
}