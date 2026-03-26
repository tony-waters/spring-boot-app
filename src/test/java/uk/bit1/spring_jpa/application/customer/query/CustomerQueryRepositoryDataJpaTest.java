package uk.bit1.spring_jpa.application.customer.query;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.bit1.spring_jpa.domain.customer.Customer;
import uk.bit1.spring_jpa.domain.customer.CustomerRepository;
import uk.bit1.spring_jpa.domain.customer.TicketStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerQueryRepositoryDataJpaTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerQueryRepository customerQueryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void find_all_summaries_returns_customers() {
        customerRepository.saveAndFlush(new Customer("Alice"));
        customerRepository.saveAndFlush(new Customer("Bob"));
        entityManager.clear();

        assertThat(customerQueryRepository.findAllSummaries())
                .extracting(CustomerSummaryView::displayName)
                .containsExactly("Alice", "Bob");
    }

    @Test
    void find_detail_by_id_returns_profile_data_when_present() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", true);
        customer = customerRepository.saveAndFlush(customer);
        entityManager.clear();

        CustomerDetailView view = customerQueryRepository.findDetailById(customer.getId()).orElseThrow();

        assertThat(view.id()).isEqualTo(customer.getId());
        assertThat(view.displayName()).isEqualTo("Tony");
        assertThat(view.emailAddress()).isEqualTo("tony@example.com");
        assertThat(view.marketingOptIn()).isTrue();
        assertThat(view.version()).isNotNull();
    }

    @Test
    void find_detail_by_id_returns_null_profile_fields_when_missing() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));
        entityManager.clear();

        CustomerDetailView view = customerQueryRepository.findDetailById(customer.getId()).orElseThrow();

        assertThat(view.id()).isEqualTo(customer.getId());
        assertThat(view.displayName()).isEqualTo("Tony");
        assertThat(view.emailAddress()).isNull();
        assertThat(view.marketingOptIn()).isNull();
        assertThat(view.version()).isNotNull();
    }

    @Test
    void find_tickets_by_customer_id_returns_ticket_views() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer.raiseTicket("This is another valid ticket");
        customer = customerRepository.saveAndFlush(customer);
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketsByCustomerId(customer.getId()))
                .hasSize(2)
                .extracting(TicketListItemView::description)
                .containsExactlyInAnyOrder("This is a valid ticket", "This is another valid ticket");
    }

    @Test
    void find_tickets_by_customer_id_returns_ticket_statuses() {
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

        customer.startTicketWork(ticketId);
        customer.resolveTicket(ticketId);

        customerRepository.saveAndFlush(customer);
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketsByCustomerId(customer.getId()))
                .singleElement()
                .satisfies(ticket -> {
                    assertThat(ticket.description()).isEqualTo("This is a valid ticket");
                    assertThat(ticket.status()).isEqualTo(TicketStatus.RESOLVED);
                });
    }

    @Test
    void find_tickets_by_customer_id_returns_empty_list_when_customer_has_no_tickets() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Tony"));
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketsByCustomerId(customer.getId())).isEmpty();
    }

    @Test
    void find_detail_by_id_returns_empty_when_customer_missing() {
        assertThat(customerQueryRepository.findDetailById(999L)).isEmpty();
    }
}