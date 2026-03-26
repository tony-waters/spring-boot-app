package uk.bit1.spring_jpa.domain.customer;

import org.junit.jupiter.api.Test;
import uk.bit1.spring_jpa.domain.tag.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Test
    void constructor_strips_display_name() {
        Customer customer = new Customer("  Tony Waters  ");

        assertThat(customer.getDisplayName()).isEqualTo("Tony Waters");
    }

    @Test
    void constructor_rejects_null_display_name() {
        assertThatThrownBy(() -> new Customer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("displayName must have a value");
    }

    @Test
    void constructor_rejects_blank_display_name() {
        assertThatThrownBy(() -> new Customer("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("displayName must have a value");
    }

    @Test
    void change_display_name_updates_value() {
        Customer customer = new Customer("Tony");

        customer.changeDisplayName("  Anthony Waters  ");

        assertThat(customer.getDisplayName()).isEqualTo("Anthony Waters");
    }

    @Test
    void change_display_name_rejects_blank_value() {
        Customer customer = new Customer("Tony");

        assertThatThrownBy(() -> customer.changeDisplayName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("displayName must have a value");
    }

    @Test
    void customer_has_no_profile_initially() {
        Customer customer = new Customer("Tony");

        assertThat(customer.hasProfile()).isFalse();
    }

    @Test
    void create_profile_creates_profile_when_missing() {
        Customer customer = new Customer("Tony");

        customer.createProfile("tony@example.com", true);

        assertThat(customer.hasProfile()).isTrue();
        assertThat(customer.profileEmailAddress()).isEqualTo("tony@example.com");
        assertThat(customer.profileMarketingOptIn()).isTrue();
    }

    @Test
    void create_profile_rejects_second_profile() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", false);

        assertThatThrownBy(() -> customer.createProfile("other@example.com", true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Customer already has a profile");
    }

    @Test
    void change_profile_email_updates_existing_profile() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", false);

        customer.changeProfileEmailAddress("  anthony@example.com  ");

        assertThat(customer.profileEmailAddress()).isEqualTo("anthony@example.com");
    }

    @Test
    void change_profile_email_rejects_when_profile_missing() {
        Customer customer = new Customer("Tony");

        assertThatThrownBy(() -> customer.changeProfileEmailAddress("tony@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Customer has no profile");
    }

    @Test
    void opt_in_to_marketing_updates_existing_profile() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", false);

        customer.optInToMarketing();

        assertThat(customer.profileMarketingOptIn()).isTrue();
    }

    @Test
    void opt_out_of_marketing_updates_existing_profile() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", true);

        customer.optOutOfMarketing();

        assertThat(customer.profileMarketingOptIn()).isFalse();
    }

    @Test
    void remove_profile_sets_profile_to_null() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", true);

        customer.removeProfile();

        assertThat(customer.hasProfile()).isFalse();
    }

    @Test
    void remove_profile_rejects_when_missing() {
        Customer customer = new Customer("Tony");

        assertThatThrownBy(customer::removeProfile)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Customer has no profile");
    }

    @Test
    void profile_email_address_rejects_when_profile_missing() {
        Customer customer = new Customer("Tony");

        assertThatThrownBy(customer::profileEmailAddress)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Customer has no profile");
    }

    @Test
    void profile_marketing_opt_in_rejects_when_profile_missing() {
        Customer customer = new Customer("Tony");

        assertThatThrownBy(customer::profileMarketingOptIn)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Customer has no profile");
    }

    @Test
    void raise_ticket_adds_ticket_to_customer() {
        Customer customer = new Customer("Tony");

        customer.raiseTicket("This is a valid ticket");

        assertThat(customer.ticketCount()).isEqualTo(1);

        Ticket ticket = customer.getTickets().iterator().next();
        assertThat(ticket.getDescription()).isEqualTo("This is a valid ticket");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(customer.hasTicket(ticket.getId())).isTrue();
        assertThat(customer.ticketStatus(ticket.getId())).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void raise_ticket_strips_description() {
        Customer customer = new Customer("Tony");

        customer.raiseTicket("   This is a valid ticket   ");

        Ticket ticket = customer.getTickets().iterator().next();
        assertThat(ticket.getDescription()).isEqualTo("This is a valid ticket");
    }

    @Test
    void change_ticket_description_updates_ticket_inside_customer() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();

        customer.changeTicketDescription(ticket.getId(), "This is another valid description");

        assertThat(ticket.getDescription()).isEqualTo("This is another valid description");
    }

    @Test
    void start_ticket_work_changes_ticket_state() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();

        customer.startTicketWork(ticket.getId());

        assertThat(customer.ticketStatus(ticket.getId())).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void resolve_ticket_changes_ticket_state() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();

        customer.resolveTicket(ticket.getId());

        assertThat(customer.ticketStatus(ticket.getId())).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void reopen_ticket_changes_ticket_state() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();
        customer.resolveTicket(ticket.getId());

        customer.reopenTicket(ticket.getId());

        assertThat(customer.ticketStatus(ticket.getId())).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void close_ticket_changes_ticket_state() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();
        customer.resolveTicket(ticket.getId());

        customer.closeTicket(ticket.getId());

        assertThat(customer.ticketStatus(ticket.getId())).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void add_tag_to_ticket_adds_tag() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();
        Tag tag = new Tag("bug");

        customer.addTagToTicket(ticket.getId(), tag);

        assertThat(ticket.getTags()).containsExactly(tag);
    }

    @Test
    void remove_tag_from_ticket_removes_tag() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();
        Tag tag = new Tag("bug");
        customer.addTagToTicket(ticket.getId(), tag);

        customer.removeTagFromTicket(ticket.getId(), tag);

        assertThat(ticket.getTags()).isEmpty();
    }

    @Test
    void clear_tags_from_ticket_removes_all_tags() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();
        customer.addTagToTicket(ticket.getId(), new Tag("bug"));
        customer.addTagToTicket(ticket.getId(), new Tag("urgent"));

        customer.clearTagsFromTicket(ticket.getId());

        assertThat(ticket.getTags()).isEmpty();
    }

    @Test
    void remove_ticket_removes_existing_ticket() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTickets().iterator().next();

        customer.removeTicket(ticket.getId());

        assertThat(customer.ticketCount()).isZero();
        assertThat(customer.hasTicket(ticket.getId())).isFalse();
    }

    @Test
    void remove_ticket_rejects_unknown_ticket_id() {
        Customer customer = new Customer("Tony");

        assertThatThrownBy(() -> customer.removeTicket(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ticket does not belong to customer: 999");
    }

    @Test
    void ticket_status_rejects_unknown_ticket_id() {
        Customer customer = new Customer("Tony");

        assertThatThrownBy(() -> customer.ticketStatus(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ticket does not belong to customer: 999");
    }

    @Test
    void ticket_collection_is_unmodifiable() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");

        assertThatThrownBy(() -> customer.getTickets().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}