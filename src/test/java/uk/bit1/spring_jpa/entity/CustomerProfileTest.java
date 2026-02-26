package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CustomerProfileTest {

    @Test
    void createProfileSetsCustomerSideAndNormalisesEmail() {
        Customer c = new Customer("Tony");

        c.createProfile("  tony@example.com  ", true);

        assertThat(c.getProfile()).isNotNull();
        assertThat(c.getProfile().getEmailAddress()).isEqualTo("tony@example.com");
        assertThat(c.getProfile().isMarketingOptIn()).isTrue();
    }

    @Test
    void cannotCreateSecondProfile() {
        Customer c = new Customer("Tony");
        c.createProfile("tony@example.com", false);

        assertThatThrownBy(() -> c.createProfile("other@example.com", true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has a Profile");
    }

    @Test
    void removeProfileClearsCustomerSide() {
        Customer c = new Customer("Tony");
        c.createProfile("tony@example.com", false);

        assertThat(c.getProfile()).isNotNull();

        c.removeProfile();

        assertThat(c.getProfile()).isNull();
    }

    @Test
    void removeProfileThrowsWhenNone() {
        Customer c = new Customer("Tony");

        assertThatThrownBy(c::removeProfile)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no Profile");
    }

    @Test
    void changeEmailStripsAndRejectsBlank() {
        Profile p = new Profile("a@b.com", false);

        p.changeEmailAddress("  new@b.com  ");
        assertThat(p.getEmailAddress()).isEqualTo("new@b.com");

        assertThatThrownBy(() -> p.changeEmailAddress("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void optInAndOptOutToggle() {
        Profile p = new Profile("a@b.com", false);

        assertThat(p.isMarketingOptIn()).isFalse();

        p.optInToMarketing();
        assertThat(p.isMarketingOptIn()).isTrue();

        p.optOutOfMarketing();
        assertThat(p.isMarketingOptIn()).isFalse();
    }
}