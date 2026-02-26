package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CustomerTicketRelationshipTest {

    @Test
    void raiseTicketSetsCustomerAndAddsToCustomerCollection() {
        Customer c = new Customer("tonyW");

        Ticket t = c.raiseTicket("This is a valid description.");

        assertThat(t.getCustomer()).isEqualTo(c);
        assertThat(c.getTickets()).contains(t);
    }

    @Test
    void removeTicketRemovesFromCollectionAndRejectsWrongOwner() {
        Customer c1 = new Customer("tonyW");
        Customer c2 = new Customer("johnS");

        Ticket t = c1.raiseTicket("This is a valid description.");

        assertThatThrownBy(() -> c2.removeTicket(t))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");

        c1.removeTicket(t);
        assertThat(c1.getTickets()).doesNotContain(t);
    }

    @Test
    void removeAllTicketsClearsCollection() {
        Customer c = new Customer("tonyW");
        c.raiseTicket("This is a valid description.");
        c.raiseTicket("This is another valid description.");

        assertThat(c.getTickets()).hasSize(2);

        c.removeAllTickets();

        assertThat(c.getTickets()).isEmpty();
    }
}
