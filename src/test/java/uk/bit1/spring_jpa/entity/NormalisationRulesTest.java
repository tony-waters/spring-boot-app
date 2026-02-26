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
        Customer c = new Customer("  tonyW  ");
        assertThat(c.getDisplayName()).isEqualTo("tonyW");

        c.changeDisplayName("  johnW  ");
        assertThat(c.getDisplayName()).isEqualTo("johnW");
    }

    @Test
    void ticketDescriptionIsStripped() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("  This is a valid description.  ");

        assertThat(t.getDescription()).isEqualTo("This is a valid description.");
    }

    @Test
    void profileDisplayNameIsStripped() {
        Customer c = new Customer("tonyW");
        c.createProfile("  TonyW  ", false);

        assertThat(c.getProfile().getEmailAddress()).isEqualTo("TonyW");

        c.getProfile().changeEmailAddress("  NewName  ");
        assertThat(c.getProfile().getEmailAddress()).isEqualTo("NewName");
    }
}
