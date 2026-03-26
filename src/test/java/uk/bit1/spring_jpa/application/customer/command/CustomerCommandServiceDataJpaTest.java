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
        Long id = service.createCustomer(new CreateCustomerCommand("Tony"));

        entityManager.clear();

        Customer customer = customerRepository.findById(id).orElseThrow();
        assertThat(customer.getDisplayName()).isEqualTo("Tony");
    }

    @Test
    void change_display_name_updates_customer() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));

        service.changeDisplayName(new ChangeDisplayNameCommand(customer.getId(), "Anthony"));

        entityManager.clear();

        Customer reloaded = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(reloaded.getDisplayName()).isEqualTo("Anthony");
    }

    @Test
    void create_profile_updates_customer() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));

        service.createProfile(new CreateProfileCommand(customer.getId(), "tony@example.com", true));

        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customer.getId()).orElseThrow();
        assertThat(reloaded.hasProfile()).isTrue();
        assertThat(reloaded.profileEmailAddress()).isEqualTo("tony@example.com");
    }

    @Test
    void change_profile_email_updates_customer_profile() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", false);
        customer = customerRepository.saveAndFlush(customer);

        service.changeProfileEmail(new ChangeProfileEmailCommand(customer.getId(), "anthony@example.com"));

        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customer.getId()).orElseThrow();
        assertThat(reloaded.profileEmailAddress()).isEqualTo("anthony@example.com");
    }

    @Test
    void raise_ticket_creates_ticket() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));

        service.raiseTicket(new RaiseTicketCommand(customer.getId(), "This is a valid ticket"));

        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customer.getId()).orElseThrow();
        assertThat(reloaded.ticketCount()).isEqualTo(1);
    }

    @Test
    void change_ticket_description_updates_ticket() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = entityManager.createQuery("""
                select t.id
                from Customer c
                join c.tickets t
                where c.id = :customerId
                """, Long.class)
                .setParameter("customerId", customer.getId())
                .getSingleResult();

        service.changeTicketDescription(
                new ChangeTicketDescriptionCommand(customer.getId(), ticketId, "This is another valid description")
        );

        entityManager.clear();

        String description = entityManager.createQuery("""
                select t.description
                from Customer c
                join c.tickets t
                where c.id = :customerId and t.id = :ticketId
                """, String.class)
                .setParameter("customerId", customer.getId())
                .setParameter("ticketId", ticketId)
                .getSingleResult();

        assertThat(description).isEqualTo("This is another valid description");
    }

    @Test
    void resolve_ticket_updates_status() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = entityManager.createQuery("""
                select t.id
                from Customer c
                join c.tickets t
                where c.id = :customerId
                """, Long.class)
                .setParameter("customerId", customer.getId())
                .getSingleResult();

        service.resolveTicket(new ResolveTicketCommand(customer.getId(), ticketId));

        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customer.getId()).orElseThrow();
        assertThat(reloaded.ticketStatus(ticketId)).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void add_tag_to_ticket_creates_tag_when_missing() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = entityManager.createQuery("""
                select t.id from Customer c join c.tickets t where c.id = :id
                """, Long.class)
                .setParameter("id", customer.getId())
                .getSingleResult();

        service.addTagToTicket(
                new AddTagToTicketCommand(customer.getId(), ticketId, "bug")
        );

        entityManager.clear();

        assertThat(tagRepository.findByName("bug")).isPresent();

        Long tagCount = entityManager.createQuery("""
                select count(tag)
                from Customer c
                join c.tickets t
                join t.tags tag
                where c.id = :customerId and t.id = :ticketId
                """, Long.class)
                .setParameter("customerId", customer.getId())
                .setParameter("ticketId", ticketId)
                .getSingleResult();

        assertThat(tagCount).isEqualTo(1L);
    }

    @Test
    void add_tag_to_ticket_reuses_existing_tag() {
        tagRepository.saveAndFlush(new Tag("bug"));

        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = entityManager.createQuery("""
                select t.id from Customer c join c.tickets t where c.id = :id
                """, Long.class)
                .setParameter("id", customer.getId())
                .getSingleResult();

        service.addTagToTicket(
                new AddTagToTicketCommand(customer.getId(), ticketId, "BUG")
        );

        entityManager.clear();

        assertThat(tagRepository.findAll()).hasSize(1);
    }

    @Test
    void remove_tag_from_ticket_removes_tag() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = entityManager.createQuery("""
                select t.id from Customer c join c.tickets t where c.id = :id
                """, Long.class)
                .setParameter("id", customer.getId())
                .getSingleResult();

        service.addTagToTicket(
                new AddTagToTicketCommand(customer.getId(), ticketId, "bug")
        );

        service.removeTagFromTicket(
                new RemoveTagFromTicketCommand(customer.getId(), ticketId, "bug")
        );

        entityManager.clear();

        Long tagCount = entityManager.createQuery("""
                select count(tag)
                from Customer c
                join c.tickets t
                left join t.tags tag
                where c.id = :customerId and t.id = :ticketId
                """, Long.class)
                .setParameter("customerId", customer.getId())
                .setParameter("ticketId", ticketId)
                .getSingleResult();

        assertThat(tagCount).isZero();
    }

    @Test
    void remove_ticket_removes_ticket() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = entityManager.createQuery("""
                select t.id from Customer c join c.tickets t where c.id = :id
                """, Long.class)
                .setParameter("id", customer.getId())
                .getSingleResult();

        service.removeTicket(new RemoveTicketCommand(customer.getId(), ticketId));

        entityManager.clear();

        Customer reloaded = customerRepository.findAggregateById(customer.getId()).orElseThrow();
        assertThat(reloaded.ticketCount()).isZero();
    }

    @Test
    void command_fails_when_customer_not_found() {
        assertThatThrownBy(() ->
                service.changeDisplayName(new ChangeDisplayNameCommand(999L, "X"))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found: 999");
    }

    @Test
    void add_tag_fails_when_tag_name_blank() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        Long ticketId = entityManager.createQuery("""
                select t.id from Customer c join c.tickets t where c.id = :id
                """, Long.class)
                .setParameter("id", customer.getId())
                .getSingleResult();

        Long customerId = customer.getId();

        assertThatThrownBy(() ->
                service.addTagToTicket(
                        new AddTagToTicketCommand(customerId, ticketId, "   ")
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tagName must not be blank");
    }
}