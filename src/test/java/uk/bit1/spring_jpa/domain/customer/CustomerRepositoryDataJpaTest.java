package uk.bit1.spring_jpa.domain.customer;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.bit1.spring_jpa.domain.tag.Tag;
import uk.bit1.spring_jpa.domain.tag.TagRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CustomerRepositoryDataJpaTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saves_customer() {
        Customer customer = new Customer("Tony");

        Customer saved = customerRepository.saveAndFlush(customer);
        entityManager.clear();

        Customer reloaded = customerRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getId()).isNotNull();
        assertThat(reloaded.getDisplayName()).isEqualTo("Tony");
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
    }

    @Test
    void saves_customer_with_profile_by_cascade() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", true);

        Customer saved = customerRepository.saveAndFlush(customer);
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(saved.getId()).orElseThrow();

        assertThat(reloaded.hasProfile()).isTrue();
        assertThat(reloaded.profileEmailAddress()).isEqualTo("tony@example.com");
        assertThat(reloaded.profileMarketingOptIn()).isTrue();
    }

    @Test
    void saves_customer_with_ticket_by_cascade() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");

        Customer saved = customerRepository.saveAndFlush(customer);
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(saved.getId()).orElseThrow();

        assertThat(reloaded.ticketCount()).isEqualTo(1);

        Ticket ticket = reloaded.getTicketsInternal().iterator().next();
        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getDescription()).isEqualTo("This is a valid ticket");
        assertThat(reloaded.ticketStatus(ticket.getId())).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void saves_customer_with_ticket_and_tag_association() {
        Tag bug = tagRepository.saveAndFlush(new Tag("bug"));

        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTicketsInternal().iterator().next();
        customer.addTagToTicket(ticket.getId(), bug);

        Customer saved = customerRepository.saveAndFlush(customer);
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(saved.getId()).orElseThrow();
        Ticket reloadedTicket = reloaded.getTicketsInternal().iterator().next();

        assertThat(reloadedTicket.getTags())
                .extracting(Tag::getName)
                .containsExactly("bug");
    }

    @Test
    void removing_profile_triggers_orphan_removal() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", true);
        customer = customerRepository.saveAndFlush(customer);

        customer.removeProfile();
        customerRepository.saveAndFlush(customer);
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customer.getId()).orElseThrow();

        assertThat(reloaded.hasProfile()).isFalse();
    }

    @Test
    void removing_ticket_triggers_orphan_removal() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = customer.getTicketsInternal().iterator().next().getId();

        customer.removeTicket(ticketId);
        customerRepository.saveAndFlush(customer);
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customer.getId()).orElseThrow();

        assertThat(reloaded.ticketCount()).isZero();
    }

    @Test
    void find_aggregate_by_id_loads_profile_tickets_and_tags() {
        Tag bug = tagRepository.saveAndFlush(new Tag("bug"));

        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", true);
        customer.raiseTicket("This is a valid ticket");
        Ticket ticket = customer.getTicketsInternal().iterator().next();
        customer.addTagToTicket(ticket.getId(), bug);

        Customer saved = customerRepository.saveAndFlush(customer);
        entityManager.clear();

        Customer aggregate = customerRepository.findAggregateById(saved.getId()).orElseThrow();

        assertThat(aggregate.hasProfile()).isTrue();
        assertThat(aggregate.ticketCount()).isEqualTo(1);

        Ticket loadedTicket = aggregate.getTicketsInternal().iterator().next();
        assertThat(loadedTicket.getTags())
                .extracting(Tag::getName)
                .containsExactly("bug");
    }

    @Test
    void unique_profile_email_is_enforced_by_database() {
        Customer first = new Customer("Tony");
        first.createProfile("shared@example.com", true);
        customerRepository.saveAndFlush(first);

        Customer second = new Customer("Alice");
        second.createProfile("shared@example.com", false);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(second))
                .isInstanceOf(Exception.class);
    }
}