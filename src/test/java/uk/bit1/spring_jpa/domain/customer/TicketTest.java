package uk.bit1.spring_jpa.domain.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bit1.spring_jpa.domain.tag.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketTest {

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        ticket = customer.getTicketsInternal().iterator().next();
    }

    @Test
    void new_ticket_starts_open() {
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void change_description_updates_value() {
        ticket.changeDescription("This is another valid description");

        assertThat(ticket.getDescription()).isEqualTo("This is another valid description");
    }

    @Test
    void change_description_strips_value() {
        ticket.changeDescription("   This is another valid description   ");

        assertThat(ticket.getDescription()).isEqualTo("This is another valid description");
    }

    @Test
    void change_description_rejects_null() {
        assertThatThrownBy(() -> ticket.changeDescription(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description cannot be null");
    }

    @Test
    void change_description_rejects_blank() {
        assertThatThrownBy(() -> ticket.changeDescription("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description must be at least 10 characters");
    }

    @Test
    void change_description_rejects_short_value() {
        assertThatThrownBy(() -> ticket.changeDescription("short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description must be at least 10 characters");
    }

    @Test
    void start_work_moves_open_to_in_progress() {
        ticket.startWork();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void resolve_moves_open_to_resolved() {
        ticket.resolve();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void resolve_moves_in_progress_to_resolved() {
        ticket.startWork();

        ticket.resolve();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void reopen_moves_resolved_to_open() {
        ticket.resolve();

        ticket.reopen();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void close_moves_resolved_to_closed() {
        ticket.resolve();

        ticket.close();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void cannot_start_work_when_already_in_progress() {
        ticket.startWork();

        assertThatThrownBy(ticket::startWork)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot startWork when ticket is IN_PROGRESS");
    }

    @Test
    void cannot_reopen_when_open() {
        assertThatThrownBy(ticket::reopen)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot reopen when ticket is OPEN");
    }

    @Test
    void cannot_close_when_open() {
        assertThatThrownBy(ticket::close)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot close when ticket is OPEN");
    }

    @Test
    void cannot_reopen_when_closed() {
        ticket.resolve();
        ticket.close();

        assertThatThrownBy(ticket::reopen)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot reopen when ticket is CLOSED");
    }

    @Test
    void cannot_change_description_when_resolved() {
        ticket.resolve();

        assertThatThrownBy(() -> ticket.changeDescription("This should be rejected"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot changeDescription when ticket is RESOLVED");
    }

    @Test
    void cannot_change_description_when_closed() {
        ticket.resolve();
        ticket.close();

        assertThatThrownBy(() -> ticket.changeDescription("This should be rejected"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot changeDescription when ticket is CLOSED");
    }

    @Test
    void add_tag_adds_once_only() {
        Tag tag = new Tag("bug");

        ticket.addTag(tag);
        ticket.addTag(tag);

        assertThat(ticket.getTags()).containsExactly(tag);
    }

    @Test
    void add_tag_ignores_null() {
        ticket.addTag(null);

        assertThat(ticket.getTags()).isEmpty();
    }

    @Test
    void remove_tag_removes_existing_tag() {
        Tag tag = new Tag("bug");
        ticket.addTag(tag);

        ticket.removeTag(tag);

        assertThat(ticket.getTags()).isEmpty();
    }

    @Test
    void remove_tag_ignores_null() {
        ticket.removeTag(null);

        assertThat(ticket.getTags()).isEmpty();
    }

    @Test
    void clear_tags_removes_all_tags() {
        ticket.addTag(new Tag("bug"));
        ticket.addTag(new Tag("urgent"));

        ticket.clearTags();

        assertThat(ticket.getTags()).isEmpty();
    }

    @Test
    void cannot_add_tag_when_resolved() {
        ticket.resolve();

        assertThatThrownBy(() -> ticket.addTag(new Tag("bug")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot addTag when ticket is RESOLVED");
    }

    @Test
    void cannot_remove_tag_when_resolved() {
        Tag tag = new Tag("bug");
        ticket.addTag(tag);
        ticket.resolve();

        assertThatThrownBy(() -> ticket.removeTag(tag))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot removeTag when ticket is RESOLVED");
    }

    @Test
    void cannot_clear_tags_when_closed() {
        ticket.addTag(new Tag("bug"));
        ticket.resolve();
        ticket.close();

        assertThatThrownBy(ticket::clearTags)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot clearTags when ticket is CLOSED");
    }

    @Test
    void tag_collection_is_unmodifiable() {
        ticket.addTag(new Tag("bug"));

        assertThatThrownBy(() -> ticket.getTags().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}