package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TicketStateMachineTest {

    @Test
    void newTicketStartsOpen() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        assertThat(t.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(t.getDescription()).isEqualTo("This is a valid description.");
    }

    @Test
    void startWorkTransitionsOpenToInProgress() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        t.startWork();

        assertThat(t.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void resolveTransitionsOpenOrInProgressToResolved() {
        Customer c = new Customer("tonyW");

        Ticket t1 = c.raiseTicket("This is a valid description.");
        t1.resolve();
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.RESOLVED);

        Ticket t2 = c.raiseTicket("This is a valid description.");
        t2.startWork();
        t2.resolve();
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void reopenTransitionsResolvedToOpen() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        t.resolve();
        t.reopen();

        assertThat(t.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void closeTransitionsResolvedToClosed() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        t.resolve();
        t.close();

        assertThat(t.getStatus()).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void cannotCloseUnlessResolved() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        assertThatThrownBy(t::close)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot close");
    }

    @Test
    void cannotStartWorkUnlessOpen() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        t.resolve();

        assertThatThrownBy(t::startWork)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot startWork");
    }

    @Test
    void cannotResolveWhenClosed() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        t.resolve();
        t.close();

        assertThatThrownBy(t::resolve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void changeDescriptionStripsAndBlocksBlank() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        t.changeDescription("   Updated description.   ");

        assertThat(t.getDescription()).isEqualTo("Updated description.");

        assertThatThrownBy(() -> t.changeDescription("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be at least 10 characters");
    }

    @Test
    void changeDescriptionBlockedWhenResolvedAndClosed() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        t.resolve();

        assertThatThrownBy(() -> t.changeDescription("Another valid description."))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESOLVED");

        t.close();

        assertThatThrownBy(() -> t.changeDescription("Another valid description."))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void descriptionMustMeetMinLengthIfYouEnforceIt() {
        Customer c = new Customer("tonyW");

        // If you implement min length check (recommended to match @Size(min=10)):
        assertThatThrownBy(() -> c.raiseTicket("Too short"))
                .isInstanceOf(IllegalArgumentException.class);

        Ticket t = c.raiseTicket("This is a valid description.");
        assertThatThrownBy(() -> t.changeDescription("short"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
