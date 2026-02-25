//package uk.bit1.spring_jpa.entity;
//
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.*;
//
//class CustomerProfileTest {
//
//    @Test
//    void createProfileSetsBothSidesAndNormalises() {
//        Customer c = new Customer("Waters", "Tony");
//
//        c.createProfile("  TonyW  ", true);
//
//        assertThat(c.getProfile()).isNotNull();
//        assertThat(c.getProfile().getDisplayName()).isEqualTo("TonyW");
//        assertThat(c.getProfile().isMarketingOptIn()).isTrue();
//
//        // package getter on Profile.customer
//        assertThat(c.getProfile().getCustomer()).isEqualTo(c);
//    }
//
//    @Test
//    void cannotCreateSecondProfile() {
//        Customer c = new Customer("Waters", "Tony");
//        c.createProfile("TonyW", false);
//
//        assertThatThrownBy(() -> c.createProfile("OtherName", true))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("already has a Profile");
//    }
//
//    @Test
//    void removeProfileClearsCustomerSideAndProfileSide() {
//        Customer c = new Customer("Waters", "Tony");
//        c.createProfile("TonyW", false);
//
//        Profile p = c.getProfile();
//        assertThat(p.getCustomer()).isEqualTo(c);
//
//        c.removeProfile();
//
//        assertThat(c.getProfile()).isNull();
//        assertThat(p.getCustomer()).isNull();
//    }
//
//    @Test
//    void removeProfileThrowsWhenNone() {
//        Customer c = new Customer("Waters", "Tony");
//
//        assertThatThrownBy(c::removeProfile)
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("no Profile");
//    }
//
//    @Test
//    void changeDisplayNameStripsAndRejectsBlank() {
//        Customer c = new Customer("Waters", "Tony");
//        c.createProfile("TonyW", false);
//
//        Profile p = c.getProfile();
//
//        p.changeDisplayName("  NewName  ");
//        assertThat(p.getDisplayName()).isEqualTo("NewName");
//
//        assertThatThrownBy(() -> p.changeDisplayName("   "))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("must not be blank");
//    }
//
//    @Test
//    void optInAndOptOutToggle() {
//        Customer c = new Customer("Waters", "Tony");
//        c.createProfile("TonyW", false);
//
//        Profile p = c.getProfile();
//        assertThat(p.isMarketingOptIn()).isFalse();
//
//        p.optInToMarketing();
//        assertThat(p.isMarketingOptIn()).isTrue();
//
//        p.optOutOfMarketing();
//        assertThat(p.isMarketingOptIn()).isFalse();
//    }
//}
