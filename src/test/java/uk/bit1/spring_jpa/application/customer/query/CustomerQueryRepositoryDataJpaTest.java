package uk.bit1.spring_jpa.application.customer.query;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.bit1.spring_jpa.domain.customer.Customer;
import uk.bit1.spring_jpa.domain.customer.CustomerRepository;
import uk.bit1.spring_jpa.domain.customer.TicketStatus;
import uk.bit1.spring_jpa.domain.tag.Tag;
import uk.bit1.spring_jpa.domain.tag.TagRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerQueryRepositoryDataJpaTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerQueryRepository customerQueryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void find_customer_summaries_returns_first_page() {
        customerRepository.saveAndFlush(new Customer("Alice"));
        customerRepository.saveAndFlush(new Customer("Bob"));
        customerRepository.saveAndFlush(new Customer("Charlie"));
        entityManager.clear();

        Page<CustomerSummaryView> page = customerQueryRepository.findCustomerSummaries(
                null,
                PageRequest.of(0, 2, Sort.by("displayName").ascending())
        );

        assertThat(page.getContent())
                .extracting(CustomerSummaryView::displayName)
                .containsExactly("Alice", "Bob");

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void find_customer_summaries_filters_by_name_case_insensitively() {
        customerRepository.saveAndFlush(new Customer("Tony"));
        customerRepository.saveAndFlush(new Customer("Anthony"));
        customerRepository.saveAndFlush(new Customer("Bob"));
        entityManager.clear();

        Page<CustomerSummaryView> page = customerQueryRepository.findCustomerSummaries(
                "ony",
                PageRequest.of(0, 10, Sort.by("displayName").ascending())
        );

        assertThat(page.getContent())
                .extracting(CustomerSummaryView::displayName)
                .containsExactly("Anthony", "Tony");
    }

    @Test
    void find_customer_summaries_supports_descending_sort() {
        customerRepository.saveAndFlush(new Customer("Alice"));
        customerRepository.saveAndFlush(new Customer("Bob"));
        customerRepository.saveAndFlush(new Customer("Charlie"));

        entityManager.flush();
        entityManager.clear();

        Page<CustomerSummaryView> page = customerQueryRepository.findCustomerSummaries(
                null,
                PageRequest.of(0, 3, Sort.by("displayName").descending())
        );

        assertThat(page.getContent())
                .extracting(CustomerSummaryView::displayName)
                .containsExactly("Charlie", "Bob", "Alice");
    }

    @Test
    void find_detail_by_id_returns_profile_data_when_present() {
        Customer customer = new Customer("Tony");
        customer.createProfile("tony@example.com", true);
        customer = customerRepository.saveAndFlush(customer);

        entityManager.flush();
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

        entityManager.flush();
        entityManager.clear();

        CustomerDetailView view = customerQueryRepository.findDetailById(customer.getId()).orElseThrow();

        assertThat(view.id()).isEqualTo(customer.getId());
        assertThat(view.displayName()).isEqualTo("Tony");
        assertThat(view.emailAddress()).isNull();
        assertThat(view.marketingOptIn()).isNull();
    }

    @Test
    void find_tickets_by_customer_id_returns_ticket_views() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer.raiseTicket("This is another valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        entityManager.flush();
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketsByCustomerId(customer.getId()))
                .hasSize(2)
                .extracting(TicketListItemView::description)
                .containsExactlyInAnyOrder("This is a valid ticket", "This is another valid ticket");
    }

    @Test
    void find_tickets_by_customer_id_and_status_filters_correctly() {
        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer.raiseTicket("This is another valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        var ticketIds = entityManager.createQuery("""
                select t.id
                from Customer c
                join c.tickets t
                where c.id = :customerId
                order by t.id
                """, Long.class)
                .setParameter("customerId", customer.getId())
                .getResultList();

        customer.startTicketWork(ticketIds.get(0));
        customer.resolveTicket(ticketIds.get(0));
        customerRepository.saveAndFlush(customer);

        entityManager.flush();
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketsByCustomerIdAndStatus(customer.getId(), TicketStatus.RESOLVED))
                .singleElement()
                .satisfies(ticket -> {
                    assertThat(ticket.description()).isEqualTo("This is a valid ticket");
                    assertThat(ticket.status()).isEqualTo(TicketStatus.RESOLVED);
                });
    }

    @Test
    void find_tickets_by_customer_id_and_tag_name_filters_correctly() {
        Tag bug = tagRepository.saveAndFlush(new Tag("bug"));
        Tag urgent = tagRepository.saveAndFlush(new Tag("urgent"));

        Customer customer = new Customer("Tony");
        customer.raiseTicket("This is a valid ticket");
        customer.raiseTicket("This is another valid ticket");
        customer = customerRepository.saveAndFlush(customer);

        var ticketIds = entityManager.createQuery("""
                select t.id
                from Customer c
                join c.tickets t
                where c.id = :customerId
                order by t.id
                """, Long.class)
                .setParameter("customerId", customer.getId())
                .getResultList();

        customer.addTagToTicket(ticketIds.get(0), bug);
        customer.addTagToTicket(ticketIds.get(1), urgent);
        customerRepository.saveAndFlush(customer);

        entityManager.flush();
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketsByCustomerIdAndTagName(customer.getId(), "BUG"))
                .singleElement()
                .satisfies(ticket -> {
                    assertThat(ticket.description()).isEqualTo("This is a valid ticket");
                    assertThat(ticket.status()).isEqualTo(TicketStatus.OPEN);
                });
    }

    @Test
    void find_ticket_detail_rows_returns_ticket_and_tag_names() {
        Tag bug = tagRepository.saveAndFlush(new Tag("bug"));
        Tag urgent = tagRepository.saveAndFlush(new Tag("urgent"));

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

        customer.addTagToTicket(ticketId, urgent);
        customer.addTagToTicket(ticketId, bug);
        customerRepository.saveAndFlush(customer);

        entityManager.flush();
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketDetailRows(customer.getId(), ticketId))
                .hasSize(2)
                .extracting(TicketDetailRow::tagName)
                .containsExactly("bug", "urgent");
    }

    @Test
    void find_ticket_detail_rows_returns_single_row_with_null_tag_when_ticket_has_no_tags() {
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

        entityManager.flush();
        entityManager.clear();

        assertThat(customerQueryRepository.findTicketDetailRows(customer.getId(), ticketId))
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.ticketId()).isEqualTo(ticketId);
                    assertThat(row.description()).isEqualTo("This is a valid ticket");
                    assertThat(row.status()).isEqualTo(TicketStatus.OPEN);
                    assertThat(row.tagName()).isNull();
                });
    }
}