package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TicketTagMutabilityRulesTest {

    @Test
    void tagsCanBeEditedWhenOpenOrInProgress() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        Tag urgent = new Tag("urgent");
        Tag bug = new Tag("bug");

        t.addTag(urgent);
        assertThat(t.getTags()).containsExactly(urgent);

        t.startWork();
        t.addTag(bug);
        assertThat(t.getTags()).containsExactlyInAnyOrder(urgent, bug);

        t.removeTag(urgent);
        assertThat(t.getTags()).containsExactly(bug);

        t.clearTags();
        assertThat(t.getTags()).isEmpty();
    }

    @Test
    void tagsCannotBeEditedWhenResolved() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");
        Tag urgent = new Tag("urgent");

        t.resolve();

        assertThatThrownBy(() -> t.addTag(urgent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESOLVED");

        assertThatThrownBy(() -> t.removeTag(urgent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESOLVED");

        assertThatThrownBy(t::clearTags)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESOLVED");
    }

    @Test
    void tagsCannotBeEditedWhenClosed() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");
        Tag urgent = new Tag("urgent");

        t.resolve();
        t.close();

        assertThatThrownBy(() -> t.addTag(urgent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOSED");

        assertThatThrownBy(() -> t.removeTag(urgent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOSED");

        assertThatThrownBy(t::clearTags)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void reopeningMakesTagsEditableAgain() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");
        Tag urgent = new Tag("urgent");

        t.resolve();
        t.reopen();

        t.addTag(urgent);
        assertThat(t.getTags()).containsExactly(urgent);
    }
}
