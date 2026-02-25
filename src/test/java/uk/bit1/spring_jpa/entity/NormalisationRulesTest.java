package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NormalisationRulesTest {

    @Test
    void tagNameIsStrippedAndLowercased() {
        Tag t = new Tag("  UrGent  ");
        assertThat(t.getName()).isEqualTo("urgent");
    }

    @Test
    void customerNamesAreStripped() {
        Customer c = new Customer("  Waters  ", "  Tony  ");
        assertThat(c.getDisplayName()).isEqualTo("Waters");
//        assertThat(c.getFirstName()).isEqualTo("Tony");

        c.changeName("  Tobes  ", "  Waters  ");
//        assertThat(c.getFirstName()).isEqualTo("Tobes");
        assertThat(c.getDisplayName()).isEqualTo("Waters");
    }

    @Test
    void ticketDescriptionIsStripped() {
        Customer c = new Customer("Waters", "Tony");
        Ticket t = c.raiseTicket("  This is a valid description.  ");

        assertThat(t.getDescription()).isEqualTo("This is a valid description.");
    }

    @Test
    void profileDisplayNameIsStripped() {
        Customer c = new Customer("Waters", "Tony");
        c.createProfile("  TonyW  ", false);

        assertThat(c.getProfile().getEmailAddress()).isEqualTo("TonyW");

        c.getProfile().changeEmailAddress("  NewName  ");
        assertThat(c.getProfile().getEmailAddress()).isEqualTo("NewName");
    }
}
