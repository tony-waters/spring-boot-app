package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.bit1.spring_jpa.repository.CustomerRepository;
import uk.bit1.spring_jpa.repository.TicketRepository;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CustomerTicketOrphanRemovalDataJpaTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired TicketRepository ticketRepository;

    @Test
    void raiseTicketPersistsViaCascade() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        Customer saved = customerRepository.saveAndFlush(c);

        // Ticket should be persisted because Customer.tickets has cascade ALL
        assertThat(t.getId()).isNotNull();
        assertThat(ticketRepository.findById(t.getId())).isPresent();

        Ticket reloaded = ticketRepository.findById(t.getId()).orElseThrow();
        assertThat(reloaded.getCustomer().getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void removingTicketDeletesOrphanRow() {
        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        Customer saved = customerRepository.saveAndFlush(c);
        Long ticketId = t.getId();
        assertThat(ticketId).isNotNull();
        assertThat(ticketRepository.findById(ticketId)).isPresent();

        saved.removeTicket(t);
        customerRepository.saveAndFlush(saved);

        assertThat(ticketRepository.findById(ticketId)).isNotPresent();
    }
}
