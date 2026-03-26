package uk.bit1.spring_jpa.application.customer.command;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import uk.bit1.spring_jpa.domain.customer.Customer;
import uk.bit1.spring_jpa.domain.customer.CustomerRepository;
import uk.bit1.spring_jpa.domain.customer.TicketStatus;
import uk.bit1.spring_jpa.domain.tag.Tag;
import uk.bit1.spring_jpa.domain.tag.TagRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(CustomerCommandService.class)
class CustomerCommandServiceDataJpaTest {

    @Autowired
    private CustomerCommandService service;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void create_customer_persists_customer() {
        Long customerId = service.createCustomer(new CreateCustomerCommand("Tony"));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findById(customerId).orElseThrow();
        assertThat(reloaded.getDisplayName()).isEqualTo("Tony");
    }

    @Test
    void change_display_name_updates_customer() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));
        Long customerId = customer.getId();

        service.changeDisplayName(new ChangeDisplayNameCommand(customerId, "Anthony"));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findById(customerId).orElseThrow();
        assertThat(reloaded.getDisplayName()).isEqualTo("Anthony");
    }

    @Test
    void create_profile_updates_customer() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));
        Long customerId = customer.getId();

        service.createProfile(new CreateProfileCommand(customerId, "tony@example.com", true));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.hasProfile()).isTrue();
        assertThat(reloaded.profileEmailAddress()).isEqualTo("tony@example.com");
        assertThat(reloaded.profileMarketingOptIn()).isTrue();
    }

    @Test
    void change_profile_email_updates_customer_profile() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", false);
        customer = customerRepository.saveAndFlush(customer);
        Long customerId = customer.getId();

        service.changeProfileEmail(new ChangeProfileEmailCommand(customerId, "anthony@example.com"));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.hasProfile()).isTrue();
        assertThat(reloaded.profileEmailAddress()).isEqualTo("anthony@example.com");
        assertThat(reloaded.profileMarketingOptIn()).isFalse();
    }

    @Test
    void raise_ticket_creates_ticket() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));
        Long customerId = customer.getId();

        service.raiseTicket(new RaiseTicketCommand(customerId, "This is a valid ticket"));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketCount()).isEqualTo(1);
    }

    @Test
    void change_ticket_description_updates_ticket() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.changeTicketDescription(
                new ChangeTicketDescriptionCommand(customerId, ticketId, "This is another valid description")
        );

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketDescription(ticketId)).isEqualTo("This is another valid description");
    }

    @Test
    void start_ticket_work_updates_status() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.startTicketWork(new StartTicketWorkCommand(customerId, ticketId));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketStatus(ticketId)).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void resolve_ticket_updates_status() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.resolveTicket(new ResolveTicketCommand(customerId, ticketId));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketStatus(ticketId)).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void reopen_ticket_updates_status() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.resolveTicket(new ResolveTicketCommand(customerId, ticketId));
        service.reopenTicket(new ReopenTicketCommand(customerId, ticketId));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketStatus(ticketId)).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void close_ticket_updates_status() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.resolveTicket(new ResolveTicketCommand(customerId, ticketId));
        service.closeTicket(new CloseTicketCommand(customerId, ticketId));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketStatus(ticketId)).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void add_tag_to_ticket_creates_tag_when_missing() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.addTagToTicket(new AddTagToTicketCommand(customerId, ticketId, "bug"));

        entityManager.flush();
        entityManager.clear();

        assertThat(tagRepository.findByName("bug")).isPresent();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        Tag bug = tagRepository.findByName("bug").orElseThrow();

        assertThat(reloaded.ticketTagCount(ticketId)).isEqualTo(1);
        assertThat(reloaded.ticketHasTag(ticketId, bug)).isTrue();
    }

    @Test
    void add_tag_to_ticket_reuses_existing_tag() {
        tagRepository.saveAndFlush(new Tag("bug"));

        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.addTagToTicket(new AddTagToTicketCommand(customerId, ticketId, "BUG"));

        entityManager.flush();
        entityManager.clear();

        assertThat(tagRepository.findAll()).hasSize(1);

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        Tag bug = tagRepository.findByName("bug").orElseThrow();

        assertThat(reloaded.ticketTagCount(ticketId)).isEqualTo(1);
        assertThat(reloaded.ticketHasTag(ticketId, bug)).isTrue();
    }

    @Test
    void remove_tag_from_ticket_removes_tag() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.addTagToTicket(new AddTagToTicketCommand(customerId, ticketId, "bug"));
        service.removeTagFromTicket(new RemoveTagFromTicketCommand(customerId, ticketId, "bug"));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketTagCount(ticketId)).isZero();
    }

    @Test
    void remove_ticket_removes_ticket() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        service.removeTicket(new RemoveTicketCommand(customerId, ticketId));

        entityManager.flush();
        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customerId).orElseThrow();
        assertThat(reloaded.ticketCount()).isZero();
        assertThat(reloaded.hasTicket(ticketId)).isFalse();
    }

    @Test
    void change_display_name_fails_when_customer_not_found() {
        assertThatThrownBy(() ->
                service.changeDisplayName(new ChangeDisplayNameCommand(999L, "X"))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found: 999");
    }

    @Test
    void create_profile_fails_when_customer_not_found() {
        assertThatThrownBy(() ->
                service.createProfile(new CreateProfileCommand(999L, "tony@example.com", true))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found: 999");
    }

    @Test
    void add_tag_fails_when_tag_name_blank() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        assertThatThrownBy(() ->
                service.addTagToTicket(new AddTagToTicketCommand(customerId, ticketId, "   "))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tagName must not be blank");
    }

    @Test
    void remove_tag_fails_when_tag_not_found() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long customerId = customer.getId();
        Long ticketId = findSingleTicketId(customerId);

        assertThatThrownBy(() ->
                service.removeTagFromTicket(new RemoveTagFromTicketCommand(customerId, ticketId, "missing"))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag not found: missing");
    }

    private Long findSingleTicketId(Long customerId) {
        entityManager.flush();
        return entityManager.createQuery("""
                select t.id
                from Customer c
                join c.tickets t
                where c.id = :customerId
                """, Long.class)
                .setParameter("customerId", customerId)
                .getSingleResult();
    }
}