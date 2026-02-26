package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TicketTagRelationshipTest {

    @Test
    void addTagIsIdempotentAndMaintainsBothSides() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        Tag tag = new Tag("  Urgent  "); // normalises to "urgent"
        assertThat(tag.getName()).isEqualTo("urgent");

        t.addTag(tag);
        t.addTag(tag); // idempotent

        assertThat(t.getTags()).containsExactly(tag);
        assertThat(tag.getTickets()).containsExactly(t);
    }

    @Test
    void removeTagIsIdempotentAndMaintainsBothSides() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        Tag tag = new Tag("urgent");
        t.addTag(tag);

        assertThat(t.getTags()).contains(tag);
        assertThat(tag.getTickets()).contains(t);

        t.removeTag(tag);
        t.removeTag(tag); // idempotent

        assertThat(t.getTags()).doesNotContain(tag);
        assertThat(tag.getTickets()).doesNotContain(t);
    }

    @Test
    void clearTagsRemovesAllAndMaintainsBothSides() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        Tag a = new Tag("a");
        Tag b = new Tag("b");

        t.addTag(a);
        t.addTag(b);

        assertThat(t.getTags()).containsExactlyInAnyOrder(a, b);
        assertThat(a.getTickets()).contains(t);
        assertThat(b.getTickets()).contains(t);

        t.clearTags();

        assertThat(t.getTags()).isEmpty();
        assertThat(a.getTickets()).doesNotContain(t);
        assertThat(b.getTickets()).doesNotContain(t);
    }
}
