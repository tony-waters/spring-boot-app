package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

class CustomerTicketCannotMoveTest {

    @Test
    void ticketCannotBeMovedBetweenCustomers() throws Exception {
        Customer a = new Customer("tonyW");
        Customer b = new Customer("johnS");

        Ticket t = a.raiseTicket("This is a valid description.");

        // attempt to attach the same Ticket to another Customer (simulate illegal move)
        Method addTicketInternal = Customer.class.getDeclaredMethod("addTicketInternal", Ticket.class);
        addTicketInternal.setAccessible(true);

        assertThatThrownBy(() -> addTicketInternal.invoke(b, t))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Cannot move Ticket between Customers");
    }
}
